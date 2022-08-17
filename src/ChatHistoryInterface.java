import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentMap;

public interface ChatHistoryInterface extends Remote {

    void put(long timestamp, String ticker, String price) throws RemoteException;
    ConcurrentMap<Long,String> getHistory() throws RemoteException;


    // Multiple users asking a bot of a price
    // multiple users requested AAPL but what timestamp did the bot consider the request to be at ?
    // What timestamp does the bot consider when giving out the price of that stock ?
    // In finance applications the price changes continuously- For many stocks,
    // transactions are occurring every second the stock market is open. Investors
    // trade an average of 90 million shares of Apple (NASDAQ:AAPL) each day.
    // Every time a block of shares is bought and sold, the stock price changes to reflect
    // the latest transaction price.
}
