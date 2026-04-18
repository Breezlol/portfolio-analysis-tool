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
    <div className="min-h-screen bg-white flex flex-col items-center justify-center px-6">
      <div className="w-full max-w-sm">
        <button onClick={() => setPage('landing')} className="text-xs text-gray-600 border border-gray-200 rounded-md px-3 py-1.5 mb-8">
          Back
        </button>
        <h2 className="text-2xl font-semibold text-gray-900 mb-8">Load profile</h2>

        <select
          onChange={handleSelect}
          defaultValue=""
          className="w-full text-sm text-gray-900 border border-gray-200 rounded-lg px-3 py-2.5 focus:outline-none focus:border-gray-400 bg-white"
        >
          <option value="">Select a user...</option>
          {users.map(u => <option key={u.id} value={u.id}>{u.name}</option>)}
        </select>

        {loading && <p className="text-sm text-gray-400 mt-4">Loading...</p>}
        {error && <p className="text-sm text-red-500 mt-4">{error}</p>}
      </div>
    </div>
  );
}
