import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import netscape.javascript.JSObject;

public class FinnHubService {

    public static String getStockPrice(String symbol) throws NoSuchElementException {
        if(symbol==null || symbol.length()<1){
            throw new NullPointerException("Stock symbol is not supplied!");
        }
        String uri = "https://finnhub.io/api/v1/quote?symbol="+symbol;
        try{
            URL url = new URL(uri);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("X-Finnhub-Token","cbnd7gaad3ifu7vks0k0");
            int responseCode = con.getResponseCode();
            if(responseCode!=200){
                throw new NoSuchElementException("FinnHub is down.");
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String res = reader.readLine();
            String price = res.split(",")[0].split(":")[1];
            System.out.println(price);
            return price;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            System.out.println("\nStock symbol is incorrect!");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("\nCannot read response from FinnHub.");
        } catch (IndexOutOfBoundsException e){
            e.printStackTrace();
            System.out.println("\nIncorrect response from FinnHub, check your stock symbol input.");
        }
        return "";
    }
}
