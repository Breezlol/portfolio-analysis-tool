import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Apitest {
    
    public static void main(String[] args) {
        System.out.println("Testing Alpha Vantage API connection...");
        System.out.println("=========================================\n");
        
        String apiKey = "0IQFSDF7L42PY5SM";
        String symbol = "IBM";
        
        String url = String.format(
            "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=%s&apikey=%s",
            symbol, apiKey
        );
        
        try {
            HttpClient client = HttpClient.newHttpClient();
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
            
            System.out.println("Sending request to Alpha Vantage API...");
            HttpResponse<String> response = client.send(request, 
                HttpResponse.BodyHandlers.ofString());
            
            System.out.println("Response Status Code: " + response.statusCode());
            System.out.println("\nResponse Body (first 500 characters):");
            System.out.println(response.body().substring(0, Math.min(500, response.body().length())));
            System.out.println("...");
            
            System.out.println("\n=========================================");
            System.out.println("API test completed successfully!");
            System.out.println("Next step: Parse JSON response");
            
        } catch (Exception e) {
            System.err.println("Error during API test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}