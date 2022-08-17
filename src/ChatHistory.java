import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ChatHistory extends UnicastRemoteObject implements ChatHistoryInterface{

    ConcurrentMap<Long, String> history;
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
