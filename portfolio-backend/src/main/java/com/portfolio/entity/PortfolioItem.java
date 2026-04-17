package com.portfolio.entity;

/**
 * Immutable value object representing a single holding in a portfolio.
 * The symbol is normalised to upper-case on construction.
 */
public class PortfolioItem {
    private final String symbol;
    private final int quantity;
    private final double purchasePrice;

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
}
