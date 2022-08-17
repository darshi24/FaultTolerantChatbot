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
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {
        while(true) {
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
                System.out.println("or enter GET to see the history :");
                String clientTicker = sc.nextLine();
                Long time = System.currentTimeMillis();

                if(isTickerValid(clientTicker)){
                     String price = FinnHubService.getStockPrice(clientTicker);
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

    private static void multicast() {
        try{
            InetAddress group = InetAddress.getByName("224.0.0.1");
            int port = 2048;
            MulticastSocket mss = new MulticastSocket(port);
            mss.joinGroup(group);
            String message = "Online?";
            byte[] buffer = message.getBytes(StandardCharsets.UTF_8);
            DatagramPacket dp = new DatagramPacket(buffer, buffer.length,group,port);
            mss.send(dp);
            mss.leaveGroup(group);
            mss.close();
            System.out.println("Successfully send out multicast message!");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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
