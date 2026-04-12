import BudgetBar from '../components/BudgetBar';
import Dashboard from '../components/Dashboard';
import RiskCard from '../components/RiskCard';
import StockSearch from '../components/StockSearch';
import PortfolioTable from '../components/PortfolioTable';

export default function PortfolioPage({ userId, setUserId, form, portfolio, setPortfolio, saved, setSaved, valueData, valueLoading, analytics, analyticsLoading, fetchPortfolioValue, setPage }) {
  const depositAmount = Number(form.depositAmount) || 0;
  const totalCost = portfolio.reduce((sum, s) => sum + s.quantity * s.purchasePrice, 0);
  const overBudget = totalCost > depositAmount;

  const handleAdd = (stock) => {
    if (portfolio.find(s => s.symbol === stock.symbol)) return;
    setPortfolio([...portfolio, stock]);
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
    if (!pRes.ok) {
      const err = await pRes.json();
      alert(err.error || 'Failed to save portfolio.');
      return;
    }
    setSaved(true);
    fetchPortfolioValue(uid);
  };

  return (
    <div className="app-container">
      <h2>Portfolio Builder</h2>
      <BudgetBar depositAmount={depositAmount} totalCost={totalCost} />
      <Dashboard valueData={valueData} valueLoading={valueLoading} userId={userId} portfolioLength={portfolio.length} />
      <RiskCard analytics={analytics} analyticsLoading={analyticsLoading} />
      <StockSearch onAdd={handleAdd} existingSymbols={portfolio.map(s => s.symbol)} />
      <h3>My Portfolio</h3>
      <PortfolioTable portfolio={portfolio} setPortfolio={setPortfolio} />
      <button disabled={overBudget} onClick={handleSave}>Save</button>
      {saved && <span> Saved!</span>}
      <button onClick={() => setPage('landing')}>Back</button>
    </div>
  );
}
