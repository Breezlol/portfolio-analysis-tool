public class MainApp {
    public static void main(String[] args) {
        System.out.println("Portfolio Analyzer started!");
        
        // Create some stock objects
        Stock apple = new Stock("aapl", "Apple Inc.", 150.25);
        Stock google = new Stock("GOOGL", "Alphabet Inc.", 2800.50);
        Stock microsoft = new Stock("msft", "Microsoft Corporation", 380.75);
        
        // Print them out
        System.out.println(apple);
        System.out.println(google);
        System.out.println(microsoft);
        
        // Update a price
        apple.setCurrentPrice(155.00);
        System.out.println("\nAfter price update:");
        System.out.println(apple);
        
        System.out.println("\nStock class testing complete!");
    }
}