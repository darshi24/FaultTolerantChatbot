import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface BotServerInterface extends Remote {
    // initiating paxos
    String createHistory(Long timestamp, String clientTicker, String price) throws RemoteException;
    String prepareAndPropose(Long timestamp, String clientTicker, String price) throws RemoteException;
    List<BotServerInterface> getServerList() throws RemoteException;
    String requestPromise(Long proposalId,String message) throws RemoteException;
    boolean proposal(Long proposalId, Long lastAcceptedProposalId,String message) throws RemoteException;
    void accept(String message) throws RemoteException;


}
