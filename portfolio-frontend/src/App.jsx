import { useState } from 'react';

export default function App() {
  const [page, setPage] = useState('landing');
  const [form, setForm] = useState({ name: '', age: '', sex: '', employmentStatus: '', incomeRange: '', depositAmount: '' });

  const set = (key, val) => setForm({ ...form, [key]: val });

  const handleCreate = (e) => {
    e.preventDefault();
    setPage('portfolio');
  };

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

  return (
    <div>
      <h1>Portfolio Analysis Tool</h1>
      <button onClick={() => setPage('create')}>Create New User</button>
      <button onClick={() => setPage('load')}>Load Existing User</button>
    </div>
  );
}
