export default function PortfolioTable({ portfolio, setPortfolio }) {
  if (portfolio.length === 0) return <p>No holdings yet. Search for a stock above to get started.</p>;

  return (
    <table>
      <thead>
        <tr><th>Symbol</th><th>Quantity</th><th>Purchase Price</th><th></th></tr>
      </thead>
      <tbody>
        {portfolio.map((s, i) => (
          <tr key={i}>
            <td>{s.symbol}</td>
            <td>
              <input type="number" min="1" value={s.quantity} style={{ width: '60px' }} onChange={e => {
                const updated = [...portfolio];
                updated[i] = { ...updated[i], quantity: Math.max(1, Number(e.target.value)) };
                setPortfolio(updated);
              }} />
            </td>
            <td>${s.purchasePrice.toFixed(2)}</td>
            <td><button onClick={() => setPortfolio(portfolio.filter((_, j) => j !== i))}>Remove</button></td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
