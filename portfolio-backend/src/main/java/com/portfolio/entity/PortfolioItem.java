package com.portfolio.entity;

public class PortfolioItem {
    private String symbol;
    private int quantity;
    private double purchasePrice;

    public PortfolioItem(String symbol, int quantity, double purchasePrice) {
        if (symbol == null || symbol.trim().isEmpty())
            throw new IllegalArgumentException("Symbol cannot be null or empty");
        if (quantity <= 0)
            throw new IllegalArgumentException("Quantity must be positive");
        if (purchasePrice < 0)
            throw new IllegalArgumentException("Purchase price cannot be negative");

        this.symbol = symbol.trim().toUpperCase();
        this.quantity = quantity;
        this.purchasePrice = purchasePrice;
    }

    public String getSymbol() { return symbol; }
    public int getQuantity() { return quantity; }
    public double getPurchasePrice() { return purchasePrice; }

    public double getTotalCost() {
        return purchasePrice * quantity;
    }

    @Override
    public String toString() {
        return String.format("PortfolioItem[%s x%d @ $%.2f = $%.2f]",
                symbol, quantity, purchasePrice, getTotalCost());
    }
}
