import { useState } from 'react';
import { fmt } from '../utils/format';

export default function BudgetBar({ depositAmount, totalCost, onAddFunds }) {
  const [funds, setFunds] = useState('');
  const remaining = depositAmount - totalCost;
  const overBudget = totalCost > depositAmount;
  const pct = depositAmount > 0 ? Math.min((totalCost / depositAmount) * 100, 100) : 0;

  const handleAdd = () => {
    const amount = parseFloat(funds);
    if (!amount || amount <= 0) return;
    onAddFunds(amount);
    setFunds('');
  };

  return (
    <div className="mb-10">
      <div className="flex justify-between items-baseline mb-6">
        <div>
          <p className="text-xs text-gray-400 uppercase tracking-widest mb-1">Remaining</p>
          <p className={`text-4xl font-semibold ${overBudget ? 'text-red-500' : 'text-gray-900'}`}>
            {fmt(remaining)}
          </p>
        </div>
        <div className="text-right">
          <p className="text-xs text-gray-400 mb-1">Deposit</p>
          <p className="text-sm font-medium text-gray-700">{fmt(depositAmount)}</p>
          <p className="text-xs text-gray-400 mt-1">Invested</p>
          <p className="text-sm font-medium text-gray-700">{fmt(totalCost)}</p>
        </div>
      </div>

      <div className="h-1 bg-gray-100 rounded-full overflow-hidden">
        <div
          className={`h-full rounded-full ${overBudget ? 'bg-red-400' : 'bg-gray-900'}`}
          style={{ width: `${pct}%` }}
        />
      </div>

      {overBudget && (
        <>
          <p className="text-xs text-red-500 mt-2">Exceeds deposit amount</p>
          <div className="flex gap-2 mt-3">
            <input
              type="number"
              min="0"
              step="0.01"
              placeholder="Add funds..."
              value={funds}
              onChange={e => setFunds(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && handleAdd()}
              className="flex-1 text-sm text-gray-900 border border-red-200 rounded-lg px-3 py-2 focus:outline-none focus:border-red-400"
            />
            <button
              onClick={handleAdd}
              className="px-4 py-2 bg-red-500 text-white text-sm font-medium rounded-lg hover:bg-red-600"
            >
              Add
            </button>
          </div>
        </>
      )}
    </div>
  );
}
