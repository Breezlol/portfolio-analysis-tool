export default function BudgetBar({ depositAmount, totalCost }) {
  const remaining = depositAmount - totalCost;
  const overBudget = totalCost > depositAmount;

  return (
    <div style={{ background: overBudget ? '#ffe0e0' : '#f0f4ff', padding: '10px', marginBottom: '10px', borderRadius: '6px', fontSize: '0.95em' }}>
      <span>Deposit: <strong>${depositAmount.toFixed(2)}</strong></span>
      {' · '}
      <span>Invested: <strong>${totalCost.toFixed(2)}</strong></span>
      {' · '}
      <span style={{ color: overBudget ? 'red' : 'green' }}>
        Remaining: <strong>${remaining.toFixed(2)}</strong>
      </span>
      {overBudget && <span style={{ color: 'red', marginLeft: '10px' }}>⚠ Exceeds your deposit!</span>}
    </div>
  );
}
