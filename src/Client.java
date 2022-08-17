import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {
        while(true) {
            System.out.println("Enter the proposer port you want to connect to: ");
            Scanner sc = new Scanner(System.in);
            String proposerPort = sc.nextLine();

            try {
                Registry serverRegistry =
                        LocateRegistry.getRegistry(Integer.parseInt(proposerPort));
                BotServerInterface s = (BotServerInterface) serverRegistry.lookup("ServerService");
                ChatHistoryInterface c = (ChatHistoryInterface) serverRegistry.lookup("HistoryService");


                System.out.println("Enter the ticker symbol whose price you want to query ");
                System.out.println("or enter GET to see the history :");
                String clientTicker = sc.nextLine();
                Long time = System.currentTimeMillis();

                if(isTickerValid(clientTicker)){
                     FinnHubService f = new FinnHubService();
                     String price = f.getStockPrice(clientTicker);
                     System.out.println("Current Price : "+price);
                     String response = s.createHistory(time,clientTicker,price);
                    if(response.equalsIgnoreCase("success")) {
                        System.out.println("Chat History saved for this operation.");
                    }else {
                        System.out.println("Chat History not saved for this operation.");
                    }

                }else{
                    System.out.println("Invalid ticker or operation.");
                }

            } catch(RemoteException e){
                System.out.println("Remote call failed.");
            } catch (NotBoundException e) {
                System.out.println("Service not hosted.");
            }

        }
    }

    private static boolean isTickerValid(String clientTicker) {
        if(clientTicker.equalsIgnoreCase("MSFT")
            || clientTicker.equalsIgnoreCase("AAPL")
            || clientTicker.equalsIgnoreCase("TSLA")) {
            return true;
        }
        return false;
    }
}
