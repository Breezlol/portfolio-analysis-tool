import { useState, useEffect } from 'react';
import { PieChart, Pie, Cell, Tooltip } from 'recharts';
import './App.css';

export default function App() {
  const [page, setPage] = useState('landing');
  const [userId, setUserId] = useState(null);
  const [users, setUsers] = useState([]);
  const [form, setForm] = useState({ name: '', age: '', sex: '', employmentStatus: '', incomeRange: '', depositAmount: '' });
  const [query, setQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [portfolio, setPortfolio] = useState([]);
  const [saved, setSaved] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [valueData, setValueData] = useState(null);
  const [valueLoading, setValueLoading] = useState(false);
  const [analytics, setAnalytics] = useState(null);
  const [analyticsLoading, setAnalyticsLoading] = useState(false);

  const fetchPortfolioValue = async (uid) => {
    if (!uid) return;
    setValueLoading(true);
    try {
      const res = await fetch('/users/' + uid + '/portfolio/value');
      if (res.ok) setValueData(await res.json());
    } catch (e) {
      setValueData(null);
    } finally {
      setValueLoading(false);
    }
    fetchAnalytics(uid);
  };

  const fetchAnalytics = async (uid) => {
    setAnalyticsLoading(true);
    try {
      const res = await fetch('/users/' + uid + '/portfolio/analytics');
      if (res.ok) setAnalytics(await res.json());
    } catch (e) {
      setAnalytics(null);
    } finally {
      setAnalyticsLoading(false);
    }
  };

  const COLORS = ['#3a86ff', '#ff006e', '#fb5607', '#ffbe0b', '#8338ec', '#06d6a0', '#118ab2', '#ef476f'];

  const set = (key, val) => setForm({ ...form, [key]: val });

  const handleCreate = (e) => {
    e.preventDefault();
    setPage('portfolio');
  };

  const searchStocks = async () => {
    if (!query.trim()) return;
    const res = await fetch('/stocks/search?q=' + query.trim());
    const data = await res.json();
    setSearchResults(data.bestMatches || []);
  };

  const addStock = async (symbol, name) => {
    if (portfolio.find(s => s.symbol === symbol)) return;
    // fetch the current price from the backend
    const res = await fetch('/stocks/quote?symbol=' + symbol);
    const price = res.ok ? await res.json() : null;
    if (price == null) { alert('Could not fetch price for ' + symbol + '. Try again in a moment.'); return; }
    setPortfolio([...portfolio, { symbol, name, quantity: 1, purchasePrice: price }]);
    setSearchResults([]);
    setQuery('');
  };

  useEffect(() => {
    if (page === 'load') fetch('/users').then(r => r.json()).then(setUsers);
  }, [page]);

  if (page === 'create') return (
    <div className="app-container">
      <h2>Create New User</h2>
      <form onSubmit={handleCreate}>
        <input placeholder="Name" value={form.name} onChange={e => set('name', e.target.value)} required /><br/>
        <input placeholder="Age" type="number" min="0" value={form.age} onChange={e => set('age', e.target.value)} required /><br/>
        <select value={form.sex} onChange={e => set('sex', e.target.value)} required>
          <option value="">-- Sex --</option><option>Male</option><option>Female</option><option>Other</option>
        </select><br/>
        <select value={form.employmentStatus} onChange={e => set('employmentStatus', e.target.value)} required>
          <option value="">-- Employment Status --</option>
          <option>Unemployed</option>
          <option>Student</option>
          <option>Part-time employed</option>
          <option>Full-time employed</option>
          <option>Self-employed</option>
          <option>Retired</option>
          <option>Other</option>
        </select><br/>
        <select value={form.incomeRange} onChange={e => set('incomeRange', e.target.value)} required>
          <option value="">-- Yearly Income Range --</option>
          <option>{'< 10,000'}</option>
          <option>10,000 - 20,000</option>
          <option>20,000 - 50,000</option>
          <option>50,000 - 100,000</option>
          <option>100,000 - 200,000</option>
          <option>{'> 200,000'}</option>
        </select><br/>
        <input placeholder="Deposit Amount" type="number" min="0" value={form.depositAmount} onChange={e => set('depositAmount', e.target.value)} required /><br/>
        <button type="submit">Create</button> <button type="button" onClick={() => setPage('landing')}>Back</button>
      </form>
    </div>
  );

  if (page === 'load') return (
    <div className="app-container">
      <h2>Select User</h2>
      <select onChange={async (e) => {
        const id = e.target.value;
        if (!id) return;
        setLoading(true);
        setError(null);
        try {
          const res = await fetch('/users/' + id);
          if (!res.ok) throw new Error('User not found');
          const u = await res.json();
          setUserId(u.id);
          setForm({ name: u.name, age: u.age, sex: u.sex, employmentStatus: u.employmentStatus, incomeRange: u.incomeRange, depositAmount: u.depositAmount });
          const pRes = await fetch('/users/' + id + '/portfolio');
          const pItems = pRes.ok ? await pRes.json() : [];
          setPortfolio(pItems.map(p => ({ symbol: p.symbol, name: p.symbol, quantity: p.quantity, purchasePrice: p.purchasePrice })));
          fetchPortfolioValue(u.id);
          setPage('portfolio');
        } catch (err) {
          setError(err.message);
        } finally {
          setLoading(false);
        }
      }} defaultValue="">
        <option value="">-- Select --</option>
        {users.map(u => <option key={u.id} value={u.id}>{u.name}</option>)}
      </select>
      {loading && <p>Loading...</p>}
      {error && <p style={{color:'red'}}>{error}</p>}
      <br/><button onClick={() => setPage('landing')}>Back</button>
    </div>
  );

  if (page === 'portfolio') return (
    <div className="app-container">
      <h2>Portfolio Builder</h2>
      {valueLoading && <p><em>Calculating portfolio value...</em></p>}
      {valueData && !valueLoading && (
        <div style={{background:'#f0f4ff', padding:'12px', marginBottom:'10px', borderRadius:'6px'}}>
          <strong>Total Portfolio Value: ${valueData.totalValue.toFixed(2)}</strong>
          {valueData.warnings && valueData.warnings.length > 0 && (
            <p style={{color:'orange', fontSize:'0.9em'}}>Some holdings could not be priced and were excluded from the total.</p>
          )}
        </div>
      )}
      {!valueLoading && !valueData && userId && portfolio.length > 0 && (
        <p style={{color:'gray'}}>Current portfolio value is unavailable right now.</p>
      )}
      {valueData && !valueLoading && valueData.holdings && valueData.holdings.length > 0 && (
        <div style={{textAlign:'center', margin:'16px 0'}}>
          <h3>Allocation</h3>
          <PieChart width={340} height={260} style={{margin:'0 auto'}}>
            <Pie
              data={valueData.holdings.map(h => ({ name: h.symbol, value: h.marketValue }))}
              cx="50%" cy="50%" outerRadius={90}
              dataKey="value" label={({ name, percent }) => `${name} ${(percent * 100).toFixed(1)}%`}
            >
              {valueData.holdings.map((_, i) => (
                <Cell key={i} fill={COLORS[i % COLORS.length]} />
              ))}
            </Pie>
            <Tooltip formatter={(val) => '$' + val.toFixed(2)} />
          </PieChart>
        </div>
      )}
      {valueData && !valueLoading && valueData.concentrationLabel && (
        <div style={{background:'#f0f4ff', padding:'12px', marginBottom:'10px', borderRadius:'6px'}}>
          <strong>Diversification: {valueData.concentrationLabel}</strong>
          <p style={{margin:'4px 0 0', fontSize:'0.9em'}}>{valueData.concentrationExplanation}</p>
        </div>
      )}
      {analyticsLoading && <p><em>Calculating risk analytics...</em></p>}
      {analytics && !analyticsLoading && analytics.volatility != null && (
        <div style={{background:'#f0f4ff', padding:'12px', marginBottom:'10px', borderRadius:'6px'}}>
          <strong>Portfolio Volatility: {analytics.volatility}%</strong>
          <p style={{margin:'4px 0 0', fontSize:'0.9em'}}>Volatility shows how much your portfolio tends to move up and down over time.</p>
          <div style={{marginTop:'8px'}}>
            <strong>Risk Level: {analytics.riskLabel}</strong>
            <p style={{margin:'4px 0 0', fontSize:'0.9em'}}>{analytics.riskExplanation}</p>
          </div>
          {analytics.skippedSymbols && analytics.skippedSymbols.length > 0 && (
            <p style={{color:'orange', fontSize:'0.85em', marginTop:'8px'}}>Some holdings could not be included because market data was unavailable.</p>
          )}
        </div>
      )}
      {analytics && !analyticsLoading && analytics.error && (
        <p style={{color:'gray'}}>{analytics.error}</p>
      )}
      <input placeholder="Search stock (e.g. AAPL)" value={query} onChange={e => setQuery(e.target.value)} />
      <button onClick={searchStocks}>Search</button>
      {searchResults.length > 0 && (
        <ul>
          {searchResults.map(r => (
            <li key={r['1. symbol']}>{r['1. symbol']} - {r['2. name']} <button onClick={() => addStock(r['1. symbol'], r['2. name'])}>Add</button></li>
          ))}
        </ul>
      )}
      <h3>My Portfolio</h3>
      {portfolio.length === 0 ? (
        <p>No holdings yet. Search for a stock above to get started.</p>
      ) : (
        <table>
          <thead>
            <tr><th>Symbol</th><th>Quantity</th><th>Purchase Price</th><th></th></tr>
          </thead>
          <tbody>
            {portfolio.map((s, i) => (
              <tr key={i}>
                <td>{s.symbol}</td>
                <td><input type="number" min="1" value={s.quantity} style={{width:'60px'}} onChange={e => {
                  const updated = [...portfolio];
                  updated[i] = { ...updated[i], quantity: Math.max(1, Number(e.target.value)) };
                  setPortfolio(updated);
                }} /></td>
                <td>${s.purchasePrice.toFixed(2)}</td>
                <td><button onClick={() => setPortfolio(portfolio.filter((_, j) => j !== i))}>Remove</button></td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
      <button onClick={async () => {
        const url = userId ? '/users/' + userId : '/users';
        const method = userId ? 'PUT' : 'POST';
        const res = await fetch(url, {
          method,
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ ...form, age: Number(form.age), depositAmount: Number(form.depositAmount) })
        });
        let uid = userId;
        if (!userId) { const u = await res.json(); uid = u.id; setUserId(u.id); }
        await fetch('/users/' + uid + '/portfolio', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(portfolio.map(s => ({ symbol: s.symbol, quantity: s.quantity, purchasePrice: s.purchasePrice || 0 })))
        });
        setSaved(true);
        fetchPortfolioValue(uid);
      }}>Save</button>
      {saved && <span> Saved!</span>}
      <button onClick={() => setPage('landing')}>Back</button>
    </div>
  );

  return (
    <div className="app-container">
      <h1>Portfolio Analysis Tool</h1>
      <button onClick={() => setPage('create')}>Create New User</button>
      <button onClick={() => setPage('load')}>Load Existing User</button>
    </div>
  );
}
