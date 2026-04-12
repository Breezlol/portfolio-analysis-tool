export default function CreateUserPage({ form, set, setPage }) {
  const handleSubmit = (e) => {
    e.preventDefault();
    setPage('portfolio');
  };

  return (
    <div className="app-container">
      <h2>Create New User</h2>
      <form onSubmit={handleSubmit}>
        <input placeholder="Name" value={form.name} onChange={e => set('name', e.target.value)} required /><br/>
        <input placeholder="Age" type="number" min="0" value={form.age} onChange={e => set('age', e.target.value)} required /><br/>
        <select value={form.sex} onChange={e => set('sex', e.target.value)} required>
          <option value="">-- Sex --</option>
          <option>Male</option>
          <option>Female</option>
          <option>Other</option>
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
        <button type="submit">Create</button>
        <button type="button" onClick={() => setPage('landing')}>Back</button>
      </form>
    </div>
  );
}
