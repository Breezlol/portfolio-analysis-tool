public class Stock {
    private String symbol;
    private String companyName;
    private double currentPrice;
    
    public Stock(String symbol, String companyName, double currentPrice) {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Stock symbol cannot be null or empty");
        }
        if (currentPrice < 0) {
            throw new IllegalArgumentException("Stock price cannot be negative");
        }
        
        this.symbol = symbol.trim().toUpperCase();
        this.companyName = companyName != null ? companyName.trim() : "";
        this.currentPrice = currentPrice;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public String getCompanyName() {
        return companyName;
    }
    
    public double getCurrentPrice() {
        return currentPrice;
    }
    
    public void setCurrentPrice(double newPrice) {
        if (newPrice < 0) {
            throw new IllegalArgumentException("Stock price cannot be negative");
        }
        this.currentPrice = newPrice;
          }
    
    @Override
    public String toString() {
        return String.format("Stock[%s - %s, Price: $%.2f]", 
                           symbol, companyName, currentPrice);
    }
}