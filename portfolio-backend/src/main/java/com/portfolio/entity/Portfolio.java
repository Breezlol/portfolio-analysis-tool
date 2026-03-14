package com.portfolio.entity;

import com.portfolio.datastructure.AVLTree;
import com.portfolio.model.Stock;
import java.util.List;

public class Portfolio {
    private double accountBalance;
    private AVLTree holdings = new AVLTree();

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
        holdings.insert(new PortfolioItem(stock.getSymbol(), quantity, stock.getCurrentPrice()));
    }

    public double getTotalValue() {
        double total = 0;
        for (PortfolioItem item : holdings.getItemsSorted()) {
            total += item.getTotalCost();
        }
        return total;
    }

    public double getAccountBalance() {
        return accountBalance;
    }

    public List<PortfolioItem> getHoldings() {
        return holdings.getItemsSorted();
    }

    @Override
    public String toString() {
        return String.format("Portfolio[balance=$%.2f, holdings=%d stock(s), totalValue=$%.2f]",
                accountBalance, holdings.size(), getTotalValue());
    }
}
