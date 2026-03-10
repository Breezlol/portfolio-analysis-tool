package com.portfolio.entity;

import com.portfolio.model.Stock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Portfolio {
    private double accountBalance;
    private List<PortfolioItem> holdings = new ArrayList<>();

    public Portfolio(double accountBalance) {
        if (accountBalance < 0)
            throw new IllegalArgumentException("Balance cannot be negative");
        this.accountBalance = accountBalance;
    }

    public void addStock(Stock stock, int quantity) {
        double cost = stock.getCurrentPrice() * quantity;
        if (cost > accountBalance)
            throw new IllegalArgumentException("Insufficient balance");
        accountBalance -= cost;
        holdings.add(new PortfolioItem(stock.getSymbol(), quantity, stock.getCurrentPrice()));
    }

    public double getTotalValue() {
        return holdings.stream()
                .mapToDouble(PortfolioItem::getTotalCost)
                .sum();
    }

    public double getAccountBalance() {
        return accountBalance;
    }

    public List<PortfolioItem> getHoldings() {
        return Collections.unmodifiableList(holdings);
    }

    @Override
    public String toString() {
        return String.format("Portfolio[balance=$%.2f, holdings=%d stock(s), totalValue=$%.2f]",
                accountBalance, holdings.size(), getTotalValue());
    }
}
