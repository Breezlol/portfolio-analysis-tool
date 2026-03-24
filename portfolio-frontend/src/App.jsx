import { useState, useEffect } from 'react';

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
  };

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

  const addStock = (symbol, name) => {
    if (!portfolio.find(s => s.symbol === symbol)) {
      setPortfolio([...portfolio, { symbol, name, quantity: 1, purchasePrice: 0 }]);
    }
    setSearchResults([]);
    setQuery('');
  };

  useEffect(() => {
    if (page === 'load') fetch('/users').then(r => r.json()).then(setUsers);
  }, [page]);

  if (page === 'create') return (
    <div>
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
    <div>
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
    <div>
      <h2>Portfolio Builder</h2>
      {valueLoading && <p><em>Calculating portfolio value...</em></p>}
      {valueData && !valueLoading && (
        <div style={{border:'1px solid #ccc', padding:'10px', marginBottom:'10px'}}>
          <strong>Total Portfolio Value: ${valueData.totalValue.toFixed(2)}</strong>
          {valueData.warnings && valueData.warnings.length > 0 && (
            <p style={{color:'orange', fontSize:'0.9em'}}>Some holdings could not be priced and were excluded from the total.</p>
          )}
        </div>
      )}
      {!valueLoading && !valueData && userId && portfolio.length > 0 && (
        <p style={{color:'gray'}}>Current portfolio value is unavailable right now.</p>
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
        <table border="1" cellPadding="6" cellSpacing="0">
          <thead>
            <tr><th>Symbol</th><th>Quantity</th><th>Purchase Price</th><th></th></tr>
          </thead>
          <tbody>
            {portfolio.map((s, i) => (
              <tr key={i}>
                <td>{s.symbol}</td>
                <td>{s.quantity}</td>
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
      }}>Save</button>
      {saved && <span> Saved!</span>}
      <button onClick={() => setPage('landing')}>Back</button>
    </div>
  );

  return (
    <div>
      <h1>Portfolio Analysis Tool</h1>
      <button onClick={() => setPage('create')}>Create New User</button>
      <button onClick={() => setPage('load')}>Load Existing User</button>
    </div>
  );
}
