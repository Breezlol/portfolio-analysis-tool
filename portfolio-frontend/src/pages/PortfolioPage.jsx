import { useState } from 'react';
import BudgetBar from '../components/BudgetBar';
import Dashboard from '../components/Dashboard';
import RiskCard from '../components/RiskCard';
import TopMovers from '../components/TopMovers';
import StockSearch from '../components/StockSearch';
import PortfolioTable from '../components/PortfolioTable';
import PurchasePanel from '../components/PurchasePanel';

export default function PortfolioPage({ userId, setUserId, form, set, portfolio, setPortfolio, saved, setSaved, valueData, valueLoading, analytics, analyticsLoading, topMovers, fetchPortfolioValue, setPage }) {
  const depositAmount = Number(form.depositAmount) || 0;
  const totalCost = portfolio.reduce((sum, s) => sum + s.quantity * s.purchasePrice, 0);
  const overBudget = totalCost > depositAmount;

  const [pendingStock, setPendingStock] = useState(null);

  const handleSelect = (stock) => setPendingStock(stock);

  const handleConfirmPurchase = (qty) => {
    const existing = portfolio.find(s => s.symbol === pendingStock.symbol);
    if (existing) {
      const totalQty = existing.quantity + qty;
      const avgPrice = (existing.quantity * existing.purchasePrice + qty * pendingStock.currentPrice) / totalQty;
      setPortfolio(portfolio.map(s =>
        s.symbol === pendingStock.symbol ? { ...s, quantity: totalQty, purchasePrice: avgPrice } : s
      ));
    } else {
      setPortfolio([...portfolio, { symbol: pendingStock.symbol, name: pendingStock.name, quantity: qty, purchasePrice: pendingStock.currentPrice }]);
    }
    setPendingStock(null);
  };

  const handleSave = async () => {
    const url = userId ? '/users/' + userId : '/users';
    const method = userId ? 'PUT' : 'POST';
    const res = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ ...form, age: Number(form.age), depositAmount: Number(form.depositAmount) })
    });
    let uid = userId;
    if (!userId) { const u = await res.json(); uid = u.id; setUserId(u.id); }
    const pRes = await fetch('/users/' + uid + '/portfolio', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(portfolio.map(s => ({ symbol: s.symbol, quantity: s.quantity, purchasePrice: s.purchasePrice || 0 })))
    });
    if (!pRes.ok) { const err = await pRes.json(); alert(err.error || 'Failed to save portfolio.'); return; }
    setSaved(true);
    fetchPortfolioValue(uid);
  };

  return (
    <div className="min-h-screen bg-white">
      <div className="max-w-2xl mx-auto px-6 py-12">

        <div className="flex items-center justify-between mb-12">
          <div>
            <p className="text-xs text-gray-400 uppercase tracking-widest mb-1">Portfolio</p>
            <h1 className="text-2xl font-semibold text-gray-900">{form.name}</h1>
          </div>
          <button
            onClick={() => setPage('landing')}
            className="text-xs text-gray-400 hover:text-gray-600"
          >
            ← Exit
          </button>
        </div>

        <BudgetBar depositAmount={depositAmount} totalCost={totalCost} onAddFunds={amount => set('depositAmount', depositAmount + amount)} />

        <StockSearch onSelect={handleSelect} existingSymbols={portfolio.map(s => s.symbol)} />
        <PurchasePanel stock={pendingStock} onConfirm={handleConfirmPurchase} onCancel={() => setPendingStock(null)} />

        <PortfolioTable portfolio={portfolio} setPortfolio={setPortfolio} valueData={valueData} userId={userId} />

        <div className="flex items-center gap-4 pt-2 mb-10">
          <button
            disabled={overBudget}
            onClick={handleSave}
            className="px-5 py-2.5 bg-gray-900 text-white text-sm font-medium rounded-lg hover:bg-gray-700 disabled:opacity-30"
          >
            Save & Analyse
          </button>
          {userId && (
            <button
              onClick={() => fetchPortfolioValue(userId)}
              disabled={valueLoading}
              className="px-5 py-2.5 border border-gray-200 text-gray-600 text-sm font-medium rounded-lg hover:bg-gray-50 disabled:opacity-40"
            >
              {valueLoading ? 'Refreshing…' : 'Refresh Prices'}
            </button>
          )}
          {saved && !valueLoading && <span className="text-xs text-gray-400">Saved</span>}
        </div>

        <div className="border-t border-gray-100 my-8" />

        <Dashboard valueData={valueData} valueLoading={valueLoading} userId={userId} portfolioLength={portfolio.length} />

        <RiskCard analytics={analytics} analyticsLoading={analyticsLoading} />

        <TopMovers topMovers={topMovers} />

      </div>
    </div>
  );
}
