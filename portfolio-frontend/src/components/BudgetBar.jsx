export default function BudgetBar({ depositAmount, totalCost }) {
  const remaining = depositAmount - totalCost;
  const overBudget = totalCost > depositAmount;
  const pct = depositAmount > 0 ? Math.min((totalCost / depositAmount) * 100, 100) : 0;

  return (
    <div className="mb-10">
      <div className="flex justify-between items-baseline mb-6">
        <div>
          <p className="text-xs text-gray-400 uppercase tracking-widest mb-1">Remaining</p>
          <p className={`text-4xl font-semibold ${overBudget ? 'text-red-500' : 'text-gray-900'}`}>
            ${remaining.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
          </p>
        </div>
        <div className="text-right">
          <p className="text-xs text-gray-400 mb-1">Deposit</p>
          <p className="text-sm font-medium text-gray-700">${depositAmount.toLocaleString('en-US', { minimumFractionDigits: 2 })}</p>
          <p className="text-xs text-gray-400 mt-1">Invested</p>
          <p className="text-sm font-medium text-gray-700">${totalCost.toLocaleString('en-US', { minimumFractionDigits: 2 })}</p>
        </div>
      </div>

      <div className="h-1 bg-gray-100 rounded-full overflow-hidden">
        <div
          className={`h-full rounded-full ${overBudget ? 'bg-red-400' : 'bg-gray-900'}`}
          style={{ width: `${pct}%` }}
        />
      </div>
      {overBudget && <p className="text-xs text-red-500 mt-2">Exceeds deposit amount</p>}
    </div>
  );
}
