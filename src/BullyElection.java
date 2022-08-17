import java.util.Arrays;
import java.util.List;
import java.util.Random;

/** The Proposer server. */
class ProposerServer {
  /** Unique identifier of server. */
  public int id;
  /** The status of server. */
  public boolean active;

  /**
   * Instantiates a new Proposer server.
   *
   * @param id the identifier
   */
  public ProposerServer(int id) {
    this.id = id;
    active = true;
  }
}

/** The Bully election algorithm. */
public class BullyElection {
  int numberOfProposers = 5;
  /** The Failed proposer port number. */
  int failedProposerNumber;

  ProposerServer[] proposers;
  /** The Proposer ports. */
  int[] proposerPorts = {3200, 3201, 3202, 3203, 3204};

  public BullyElection(List<String> serverPortsList, String failedProposerNumber) {
    initialiseProposers(serverPortsList);
    startElection(Integer.parseInt(failedProposerNumber));
  }

  public void initialiseServerPorts(List<String> serverPortsList) {
    proposerPorts[0] = Integer.parseInt(serverPortsList.get(0));
    proposerPorts[1] = Integer.parseInt(serverPortsList.get(1));
    proposerPorts[2] = Integer.parseInt(serverPortsList.get(2));
    proposerPorts[3] = Integer.parseInt(serverPortsList.get(3));
    proposerPorts[4] = Integer.parseInt(serverPortsList.get(4));
  }

  /** Initialise proposers. */
  public void initialiseProposers(List<String> serverPortsList) {
    System.out.println("Initialize bully election");
    initialiseServerPorts(serverPortsList);
    proposers = new ProposerServer[numberOfProposers];
    for (int i = 0; i < proposers.length; i++) {
      proposers[i] = new ProposerServer(i);
      // System.out.println("Proposer on port " + proposerPorts[i] + " has ID " + proposers[i].id);
    }
  }

  /**
   * Get the index of alive proposer with max identifier.
   *
   * @return the index of proposer
   */
  public int getMaxIndex() {
    int maxId = Integer.MIN_VALUE;
    int maxIdIndex = 0;
    for (int i = 0; i < proposers.length; i++) {
      if (proposers[i].active && proposers[i].id > maxId) {
        maxId = proposers[i].id;
        maxIdIndex = i;
      }
    }
    return maxIdIndex;
  }

  /**
   * Find index of failed proposer.
   *
   * @param proposers the proposer ports array
   * @param failedPort the failed proposer port number
   * @return the index
   */
  public static int findIndex(int[] proposers, int failedPort) {
    int index = Arrays.binarySearch(proposers, failedPort);
    return (index < 0) ? -1 : index;
  }

  /** Start election. */
  public void startElection(int failedProposerNumber) {

    Random random = new Random();
    try {
      int failedProposerIndex = findIndex(proposerPorts, failedProposerNumber);
      System.out.println("Proposer on port " + proposerPorts[failedProposerIndex] + " failed");
      proposers[failedProposerIndex].active = false;

    } catch (Exception exception) {
      System.out.println("No matched proposer port number, please try again");
      System.exit(1);
    }

    int[] excludedFailedProposer = {failedProposerNumber};

    // random select alive proposer to initiate the election
    int initiatorProposer = getRandomPortWithFailedExcluded(random, 0, 4, excludedFailedProposer);

    System.out.println("Election Initiated by proposer " + proposerPorts[initiatorProposer]);

    int prev = initiatorProposer;
    int next = prev + 1;

    do {
      if (proposers[next].active) {
        System.out.println(
            "Proposer "
                + proposerPorts[prev]
                + " pass Election ID: "
                + proposers[prev].id
                + " "
                + "to Proposer "
                + proposerPorts[next]);
        prev = next;
      }

      next = (next + 1) % numberOfProposers;
    } while (next != initiatorProposer);

    System.out.println(
        "Proposer " + proposerPorts[proposers[getMaxIndex()].id] + " becomes leader");
    int leader = proposers[getMaxIndex()].id;

    prev = leader;
    next = (prev + 1) % numberOfProposers;

    while (true) {

      if (proposers[next].active) {
        System.out.println(
            "Proposer "
                + proposerPorts[prev]
                + " pass Leader ID: "
                + leader
                + " message to Proposer "
                + proposerPorts[next]);
        prev = next;
      }
      next = (next + 1) % numberOfProposers;
      if (next == leader) {
        System.out.println(
            "Election complete, client can try connect to new server with port number: "
                + proposerPorts[leader]);
        break;
      }
    }
  }

  /**
   * Get a random proposer port to initiate the bully election with failed proposer excluded.
   *
   * @param rnd the random
   * @param start the start index
   * @param end the end index
   * @param exclude the excluded ports
   * @return the random port
   */
  public int getRandomPortWithFailedExcluded(Random rnd, int start, int end, int[] exclude) {
    int random = start + rnd.nextInt(end - start + 1 - exclude.length);
    for (int ex : exclude) {
      if (random < ex) {
        break;
      }
      random++;
    }
    return random;
  }
}
