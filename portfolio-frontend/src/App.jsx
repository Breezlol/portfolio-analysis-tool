import { useState, useEffect } from 'react';

export default function App() {
  const [page, setPage] = useState('landing');
  const [users, setUsers] = useState([]);
  const [form, setForm] = useState({ name: '', age: '', sex: '', employmentStatus: '', incomeRange: '', depositAmount: '' });
  const [query, setQuery] = useState('');
  const [portfolio, setPortfolio] = useState([]);

  const set = (key, val) => setForm({ ...form, [key]: val });

  const handleCreate = (e) => {
    e.preventDefault();
    setPage('portfolio');
  };

  const addStock = () => {
    if (query.trim()) {
      setPortfolio([...portfolio, { symbol: query.trim().toUpperCase(), quantity: 1 }]);
      setQuery('');
    }
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
        <input placeholder="Employment Status" value={form.employmentStatus} onChange={e => set('employmentStatus', e.target.value)} required /><br/>
        <input placeholder="Income Range" value={form.incomeRange} onChange={e => set('incomeRange', e.target.value)} required /><br/>
        <input placeholder="Deposit Amount" type="number" min="0" value={form.depositAmount} onChange={e => set('depositAmount', e.target.value)} required /><br/>
        <button type="submit">Create</button> <button type="button" onClick={() => setPage('landing')}>Back</button>
      </form>
    </div>
  );

  if (page === 'load') return (
    <div>
      <h2>Select User</h2>
      <select onChange={e => { setPage('portfolio'); }} defaultValue="">
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
      <button onClick={addStock}>Add</button>
      <ul>
        {portfolio.map((s, i) => (
          <li key={i}>{s.symbol} <button onClick={() => setPortfolio(portfolio.filter((_, j) => j !== i))}>Remove</button></li>
        ))}
      </ul>
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
