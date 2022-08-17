import java.util.Random;

public class FinnHubService {
    public String getStockPrice(String symbol) {
        Random r = new Random();
        return String.valueOf(r.nextFloat());
    }
}
