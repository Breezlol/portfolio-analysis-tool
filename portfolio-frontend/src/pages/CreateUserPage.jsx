const inputClass = "w-full text-sm text-gray-900 border border-gray-200 rounded-lg px-3 py-2.5 focus:outline-none focus:border-gray-400 bg-white";
const selectClass = inputClass + " text-gray-500";

export default function CreateUserPage({ form, set, setPage }) {
  const handleSubmit = (e) => {
    e.preventDefault();
    setPage('portfolio');
  };

  return (
    <div className="min-h-screen bg-white flex flex-col items-center justify-center px-6">
      <div className="w-full max-w-sm">
        <button onClick={() => setPage('landing')} className="text-xs text-gray-600 border border-gray-200 rounded-md px-3 py-1.5 mb-8">
          Back
        </button>
        <h2 className="text-2xl font-semibold text-gray-900 mb-8">Create profile</h2>

        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <input className={inputClass} placeholder="Full name" value={form.name} onChange={e => set('name', e.target.value)} required />
          <input className={inputClass} placeholder="Age" type="number" min="0" value={form.age} onChange={e => set('age', e.target.value)} required />

          <select className={selectClass} value={form.sex} onChange={e => set('sex', e.target.value)} required>
            <option value="">Sex</option>
            <option>Male</option>
            <option>Female</option>
            <option>Other</option>
          </select>

          <select className={selectClass} value={form.employmentStatus} onChange={e => set('employmentStatus', e.target.value)} required>
            <option value="">Employment status</option>
            <option>Unemployed</option>
            <option>Student</option>
            <option>Part-time employed</option>
            <option>Full-time employed</option>
            <option>Self-employed</option>
            <option>Retired</option>
            <option>Other</option>
          </select>

          <select className={selectClass} value={form.incomeRange} onChange={e => set('incomeRange', e.target.value)} required>
            <option value="">Yearly income range</option>
            <option>{'< 10,000'}</option>
            <option>10,000 - 20,000</option>
            <option>20,000 - 50,000</option>
            <option>50,000 - 100,000</option>
            <option>100,000 - 200,000</option>
            <option>{'> 200,000'}</option>
          </select>

          <input className={inputClass} placeholder="Deposit amount ($)" type="number" min="0" value={form.depositAmount} onChange={e => set('depositAmount', e.target.value)} required />

          <button type="submit" className="w-full bg-white text-gray-900 text-sm font-medium py-3 rounded-lg border border-gray-200 mt-2">
            Continue
          </button>
        </form>
      </div>
    </div>
  );
}
