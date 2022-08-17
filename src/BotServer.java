import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class BotServer extends UnicastRemoteObject implements BotServerInterface {

  List<String> serverPortsList;
  String serverPort;
  private final int MAJORITY = 3;
  private Map<Long, Long> maxProposalIdMap;
  private Map<Long, String> lastAcceptedValues;
  private ChatHistoryInterface history;
  private String crashTag;
  private String crashPort1;
  private String crashPort2;
  private boolean isActive;

  public BotServer(
      List<String> portsList, int index, String crashTag, String crashPort1, String crashPort2)
      throws RemoteException {
    this.serverPortsList = portsList;
    this.serverPort = portsList.get(index - 1);
    this.lastAcceptedValues = new HashMap<>();
    this.maxProposalIdMap = new HashMap<>();
    this.crashTag = crashTag;
    this.crashPort1 = crashPort1;
    this.crashPort2 = crashPort2;
    this.isActive = true;
    Registry serverRegistry = LocateRegistry.createRegistry(Integer.parseInt(serverPort));

    try {
      serverRegistry.bind("ServerService", this);
      history = new ChatHistory();
      serverRegistry.bind("HistoryService", history);

    } catch (AlreadyBoundException e) {
      System.out.println(
          "Another service already present on this port. Please start " + "on a different port.");
    }
  }

  public static void main(String[] args) throws RemoteException {
    if (!(args.length == 6 || args.length == 9)) {
      System.out.println("More or less number of arguments.");
      System.exit(0);
    }
    // check whether the index is within 1 and 5
    try {
      Integer.parseInt(args[5]);
    } catch (NumberFormatException e) {
      System.out.println("The argument for index must be a number");
      System.exit(0);
    }
    int indexNumber = Integer.parseInt(args[5]);
    if (indexNumber < 1 || indexNumber > 5) {
      System.out.println("The argument for index must be between 1 and 5, both inclusive.");
    }
    String index = args[5];

    // check validity of the 5 ports entered
    for (int i = 0; i < 5; i++) {
      if (!CommonUtils.isPortValid(args[i])) {
        System.out.println(
            "Port number "
                + i
                + 1
                + "is invalid. Run the program again"
                + " with all valid ports.");
        System.exit(0);
      }
    }
    List<String> portsList = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      portsList.add(args[i]);
    }

    String crashTag = null;
    String crashPort1 = null;
    String crashPort2 = null;

    if (args.length == 9) {
      checkCrashTagValidity(args[6]);
      checkCrashPortsValidity(args[7], args[8]);
      if (!portsList.contains(args[7]) || !portsList.contains(args[8])) {
        System.out.println("The two crash ports must be among the list of 5 ports entered before.");
        System.exit(0);
      }
      crashTag = args[6];
      crashPort1 = args[7];
      crashPort2 = args[8];
      // run leader election
    }

    BotServer server =
        new BotServer(portsList, Integer.parseInt(index), crashTag, crashPort1, crashPort2);
    Scanner sc = new Scanner(System.in);

    while (true) {
      String input = sc.nextLine();
      if (input.equalsIgnoreCase("exit")) {
        BullyElection bullyElection = new BullyElection(portsList, portsList.get(indexNumber-1));
        System.exit(1);
      }
    }
  }

  @Override
  public String createHistory(Long timestamp, String clientTicker, String price)
      throws RemoteException {
    return prepareAndPropose(timestamp, clientTicker, price);
  }

  @Override
  public String prepareAndPropose(Long timestamp, String clientTicker, String price)
      throws RemoteException {
    Long proposalId = Long.parseLong(System.currentTimeMillis() + this.serverPort);

    String message = timestamp + " " + clientTicker + " " + price;

    int promiseResponses = 0;
    int proposalsAccepted = 0;
    String updatedMessage;
    String promiseResponse = null;

    // send the proposal and see if all acceptors promise on that proposal
    List<BotServerInterface> serverList = getServerList();
    for (BotServerInterface a : serverList) {
      String response = a.requestPromise(proposalId, message);
      if (!response.equalsIgnoreCase("fail")) {
        promiseResponses++;
        promiseResponse = response;
      }
    }

    System.out.println("Phase 1 complete.");
    System.out.println("Number of acceptor votes received in Phase 1: " + promiseResponses);

    if (promiseResponses >= MAJORITY) { // send proposal to all acceptors

      createDelay(4);

      String[] promiseResponseParams = promiseResponse.split(" ");
      if (promiseResponseParams.length > 2) { // if it received any piggybacked value

        updatedMessage =
            promiseResponseParams[2]
                + " "
                + promiseResponseParams[3]
                + " "
                + promiseResponseParams[4];

        System.out.println(updatedMessage);
        serverList = getServerList();
        for (BotServerInterface a : serverList) {
          if (a.proposal(proposalId, Long.parseLong(promiseResponseParams[1]), updatedMessage)) {
            proposalsAccepted++;
          }
        }
        System.out.println("Number of acceptor votes received in Phase 2: " + proposalsAccepted);
        if (proposalsAccepted >= this.MAJORITY) {
          createDelay(20);

          serverList = getServerList();
          // figuratively the proposer is calling the accept method of the listeners
          for (BotServerInterface l : serverList) {
            l.accept(updatedMessage);
          }
          return "success";
        } else {
          return "failure";
        }
      } else {
        serverList = getServerList();
        for (BotServerInterface a : serverList) {
          if (a.proposal(proposalId, 0L, message)) {
            proposalsAccepted++;
          }
        }
        System.out.println("Number of acceptor votes received in Phase 2: " + proposalsAccepted);
        if (proposalsAccepted >= this.MAJORITY) {

          createDelay(20);

          serverList = getServerList();
          // figuratively the proposer is calling the accept method of the listeners
          for (BotServerInterface l : serverList) {
            l.accept(message);
          }
          return "success";
        } else {
          return "failure";
        }
      }
    } else {
      return "failure";
    }
  }

  @Override
  public String requestPromise(Long proposalId, String message) throws RemoteException {

    if (this.crashTag != null && this.crashPort1 != null && this.crashPort2 != null) {
      if (this.crashTag.equalsIgnoreCase("crashP1")
          && (this.crashPort1.equals(this.serverPort) || this.crashPort2.equals(this.serverPort))) {
        isActive = false;
        System.out.println("Acceptor Failed in phase 1");
      }
      if (this.crashTag.equalsIgnoreCase("crashP2")
          && (this.crashPort1.equals(this.serverPort) || this.crashPort2.equals(this.serverPort))) {
        isActive = true;
      }
    }

    if (!isActive) {
      createDelay(3);
      return "fail";
    }

    Long key = Long.parseLong(message.split(" ")[0]); // the timestamp is the key
    if (maxProposalIdMap.get(key) != null && proposalId <= maxProposalIdMap.get(key)) {
      createDelay(3);
      return "fail";
    } else {
      maxProposalIdMap.put(key, proposalId);
      if (proposalPreviouslyAccepted(key)) {
        createDelay(3);
        return proposalId + " " + lastAcceptedValues.get(key);
      } else {
        createDelay(3);
        return proposalId + " " + "null";
      }
    }
  }

  @Override
  public boolean proposal(Long proposalId, Long lastAcceptedProposalId, String message)
      throws RemoteException {

    if (this.crashTag != null && this.crashPort1 != null && this.crashPort2 != null) {
      if (this.crashTag.equalsIgnoreCase("crashP2")
          && (this.crashPort1.equals(this.serverPort) || this.crashPort2.equals(this.serverPort))) {
        isActive = false;
        System.out.println("Acceptor Failed in phase 2");
      }
      if (this.crashTag.equalsIgnoreCase("crashP1")
          && (this.crashPort1.equals(this.serverPort) || this.crashPort2.equals(this.serverPort))) {
        isActive = true;
      }
    }

    if (!isActive) {
      return false;
    }

    Long key = Long.parseLong(message.split(" ")[0]);
    if (maxProposalIdMap.get(key) == null || proposalId.equals(maxProposalIdMap.get(key))) {
      // timestamp -> proposalId timestamp ticker price
      lastAcceptedValues.put(key, proposalId + " " + message);

      return true;
    }
    return false;
  }

  @Override
  public void accept(String clientMessage) throws RemoteException {

    String[] params = clientMessage.split(" ");
    history.put(Long.parseLong(params[0]), params[1], params[2]);
    System.out.println("HISTORY:-");
    System.out.println(history.getHistory());

    // resetting the PAXOS run
    this.maxProposalIdMap.put(Long.parseLong(params[0]), Long.MIN_VALUE);
    this.lastAcceptedValues.remove(Long.parseLong(params[0]));
  }

  private boolean proposalPreviouslyAccepted(long key) {

    if (this.lastAcceptedValues.containsKey(key)) {
      return true;
    } else {
      return false;
    }
  }

  private void createDelay(int seconds) {
    try {
      TimeUnit.SECONDS.sleep(seconds);
    } catch (InterruptedException e) {
      System.out.println("something");
    }
  }

  private static void checkCrashTagValidity(String arg) {
    if (!(arg.equalsIgnoreCase("crashP1") || arg.equalsIgnoreCase("crashP2"))) {
      System.out.println("Invalid crash tag. Should be either 'crashP1' or 'crashP2'.");
      System.exit(0);
    }
  }

  private static void checkCrashPortsValidity(String arg, String arg1) {
    if (!CommonUtils.isPortValid(arg)) {
      System.out.println("Invalid port number to crash.");
      System.exit(0);
    }

    if (!CommonUtils.isPortValid(arg1)) {
      System.out.println("Invalid port number to crash.");
      System.exit(0);
    }
  }

  @Override
  public List<BotServerInterface> getServerList() throws RemoteException {
    List<BotServerInterface> serverList = new ArrayList<>();
    for (String serverPort : serverPortsList) {
      try {
        Registry registry = LocateRegistry.getRegistry(Integer.parseInt(serverPort));
        BotServerInterface s = (BotServerInterface) registry.lookup("ServerService");
        serverList.add(s);
      } catch (RemoteException | NotBoundException ignored) {
      }
    }
    return serverList;
  }
}
