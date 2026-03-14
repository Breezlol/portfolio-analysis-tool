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
      setPortfolio([...portfolio, { symbol, name, quantity: 1 }]);
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
        const res = await fetch('/users/' + id);
        const u = await res.json();
        setUserId(u.id);
        setForm({ name: u.name, age: u.age, sex: u.sex, employmentStatus: u.employmentStatus, incomeRange: u.incomeRange, depositAmount: u.depositAmount });
        setPage('portfolio');
      }} defaultValue="">
        <option value="">-- Select --</option>
        {users.map(u => <option key={u.id} value={u.id}>{u.name}</option>)}
      </select>
      <br/><button onClick={() => setPage('landing')}>Back</button>
    </div>
  );

  if (page === 'portfolio') return (
    <div>
      <h2>Portfolio Builder</h2>
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
      <ul>
        {portfolio.map((s, i) => (
          <li key={i}>{s.symbol} - {s.name} <button onClick={() => setPortfolio(portfolio.filter((_, j) => j !== i))}>Remove</button></li>
        ))}
      </ul>
      <button onClick={async () => {
        const url = userId ? '/users/' + userId : '/users';
        const method = userId ? 'PUT' : 'POST';
        const res = await fetch(url, {
          method,
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ ...form, age: Number(form.age), depositAmount: Number(form.depositAmount) })
        });
        if (!userId) { const u = await res.json(); setUserId(u.id); }
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
