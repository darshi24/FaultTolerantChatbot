import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Date;
import java.util.Scanner;

/**
 * A class that represents the client using the servers' chat-bot services.
 */
public class Client {

  /**
   * The main method. It executes when the client is started
   * @param args the command line arguments, if any, passed while starting a client program
   */
  public static void main(String[] args) {
    while (true) {
      Client.multicast();
      System.out.println("Enter the proposer port you want to connect to: ");
      Scanner sc = new Scanner(System.in);
      String proposerPort = sc.nextLine();

      try {
        Registry serverRegistry =
            LocateRegistry.getRegistry(Integer.parseInt(proposerPort));
        BotServerInterface s = (BotServerInterface) serverRegistry.lookup("ServerService");
        ChatHistoryInterface c = (ChatHistoryInterface) serverRegistry.lookup("HistoryService");

        System.out.println("Enter the ticker symbol whose price you want to query ");
        String clientTicker = sc.nextLine();
        Long time = System.currentTimeMillis();

        if (isTickerValid(clientTicker)) {
          String price = FinnHubService.getStockPrice(clientTicker);
          System.out.println("Current Price : " + price);
          String response = s.createHistory(time, clientTicker, price);
          if (response.equalsIgnoreCase("success")) {
            System.out.println("Chat History saved for this operation.");
          } else {
            System.out.println("Chat History not saved for this operation.");
          }

        } else {
          System.out.println("Invalid ticker or operation.");
        }

      } catch (RemoteException e) {
        System.out.println("Remote call failed.");
      } catch (NotBoundException e) {
        System.out.println("Service not hosted.");
      }

    }
  }

  private static void multicast() {
    try {
      InetAddress group = InetAddress.getByName("224.0.0.1");
      int port = 2048;
      MulticastSocket mss = new MulticastSocket(port);
      mss.joinGroup(group);
      String message = "A new client is on line at" + new Date().toString() + "!\n";
      byte[] buffer = message.getBytes(StandardCharsets.UTF_8);
      DatagramPacket dp = new DatagramPacket(buffer, buffer.length, group, port);
      mss.send(dp);
      mss.leaveGroup(group);
      mss.close();
      System.out.println("Successfully send out multicast message!\n");
    } catch (UnknownHostException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    new Thread("Thread" + new Date().getTime()) {
      @Override
      public void run() {
        while(true){
          try {
            InetAddress group = InetAddress.getByName("224.0.0.1");
            MulticastSocket sock = new MulticastSocket(2048);
            sock.joinGroup(group);

            byte[] msg = new byte[256];
            DatagramPacket packet = new DatagramPacket(msg, msg.length);

            sock.receive(packet);
            System.out.println(
                new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8));
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }.start();
  }

  /**
   * A helper method that checks the validity of the ticker symbol entered by the user. It allows three
   * ticker symbols and if anything else (other ticker symbols, numbers, arbitrary characters) is entered
   * then an error message is displayed and the user is prompted again to add a valid ticker symbol.
   * @param clientTicker the ticker symbol
   * @return a boolean value of true or false. true if the ticker symbol is valid, otherwise false
   */
  private static boolean isTickerValid(String clientTicker) {
    if (clientTicker.equalsIgnoreCase("MSFT")
        || clientTicker.equalsIgnoreCase("AAPL")
        || clientTicker.equalsIgnoreCase("TSLA")) {
      return true;
    }
    return false;
  }
}
