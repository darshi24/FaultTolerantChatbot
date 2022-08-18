import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A class that implements ChatHistoryInterface to provide storage for a chat history of the bot.
 */
public class ChatHistory extends UnicastRemoteObject implements ChatHistoryInterface{

    ConcurrentMap<Long, String> history;

    /**
     * A constructor for the ChatHistory class. It initializes the chat history store.
     * @throws RemoteException the exception that is thrown if the remote call fails
     */
    public ChatHistory() throws RemoteException {
        history = new ConcurrentHashMap<>();
    }

    @Override
    synchronized public void put(long timestamp, String ticker, String price) throws RemoteException{
        history.put(timestamp, ticker + " " +price);
    }

    @Override
    public ConcurrentMap<Long, String> getHistory() throws RemoteException {
        return history;
    }
}
