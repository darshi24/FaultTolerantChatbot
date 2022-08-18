import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentMap;

/**
 * An interface that represents the chat history storage for the server.
 */
public interface ChatHistoryInterface extends Remote {

    /**
     * A method to update the chat history with a new client-bot interaction.
     * @param timestamp the timestamp of the interaction
     * @param ticker the ticker entered by the client in the interaction
     * @param price the price returned by the external API during the interaction
     * @throws RemoteException the exception that is thrown if the remote call fails
     */
    void put(long timestamp, String ticker, String price) throws RemoteException;

    /**
     * A method to retrieve the chat history that contains all client-bot interactions.
     * @return all client bot interactions (timestamp of interaction, ticker and the price recorded during
     * the interaction
     * @throws RemoteException the exception that is thrown if the remote call fails
     */
    ConcurrentMap<Long,String> getHistory() throws RemoteException;

}
