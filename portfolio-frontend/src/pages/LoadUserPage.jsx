import { useState } from 'react';

export default function LoadUserPage({ users, setUserId, setForm, setPortfolio, fetchPortfolioValue, setPage }) {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleSelect = async (e) => {
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
  };

  return (
    <div className="app-container">
      <h2>Select User</h2>
      <select onChange={handleSelect} defaultValue="">
        <option value="">-- Select --</option>
        {users.map(u => <option key={u.id} value={u.id}>{u.name}</option>)}
      </select>
      {loading && <p>Loading...</p>}
      {error && <p style={{color: 'red'}}>{error}</p>}
      <br/>
      <button onClick={() => setPage('landing')}>Back</button>
    </div>
  );
}
