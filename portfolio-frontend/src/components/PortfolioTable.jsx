export default function PortfolioTable({ portfolio, setPortfolio, valueData }) {
  const priceMap = {};
  if (valueData?.holdings) {
    for (const h of valueData.holdings) priceMap[h.symbol] = h;
  }
  const hasLive = Object.keys(priceMap).length > 0;

  if (portfolio.length === 0) return (
    <div className="mb-8">
      <p className="text-sm text-gray-400">No holdings yet. Search above to add a stock.</p>
    </div>
  );

  const cols = hasLive
    ? 'grid-cols-[1fr_60px_90px_90px_100px_40px]'
    : 'grid-cols-[1fr_80px_110px_40px]';

  return (
    <div className="mb-8">
      <div className={`border-b border-gray-100 pb-2 mb-1 grid ${cols} gap-4`}>
        <span className="text-xs text-gray-400 uppercase tracking-widest">Symbol</span>
        <span className="text-xs text-gray-400 uppercase tracking-widest">Qty</span>
        <span className="text-xs text-gray-400 uppercase tracking-widest">Buy price</span>
        {hasLive && <span className="text-xs text-gray-400 uppercase tracking-widest">Current</span>}
        {hasLive && <span className="text-xs text-gray-400 uppercase tracking-widest">Gain/Loss</span>}
        <span />
      </div>

      {portfolio.map((s, i) => {
        const live = priceMap[s.symbol];
        const gainPct = live ? ((live.currentPrice - live.purchasePrice) / live.purchasePrice * 100) : null;
        const gainDollars = live ? ((live.currentPrice - live.purchasePrice) * s.quantity) : null;
        const isPositive = gainPct !== null && gainPct >= 0;

        return (
          <div key={i} className={`grid ${cols} gap-4 items-center py-3 border-b border-gray-100 last:border-0`}>
            <span className="text-sm font-medium text-gray-900">{s.symbol}</span>

            <input
              type="number"
              min="1"
              value={s.quantity}
              onChange={e => {
                const updated = [...portfolio];
                updated[i] = { ...updated[i], quantity: Math.max(1, Number(e.target.value)) };
                setPortfolio(updated);
              }}
              className="w-full text-sm text-gray-900 border border-gray-200 rounded-md px-2 py-1 focus:outline-none focus:border-gray-400"
            />

            <input
              type="number"
              min="0"
              step="0.01"
              value={s.purchasePrice}
              onChange={e => {
                const updated = [...portfolio];
                updated[i] = { ...updated[i], purchasePrice: Math.max(0, Number(e.target.value)) };
                setPortfolio(updated);
              }}
              className="w-full text-sm text-gray-900 border border-gray-200 rounded-md px-2 py-1 focus:outline-none focus:border-gray-400"
            />

            {hasLive && (
              <span className="text-sm text-gray-700">
                {live ? `$${live.currentPrice.toFixed(2)}` : '—'}
              </span>
            )}

            {hasLive && (
              <span className={`text-sm font-medium ${gainPct === null ? 'text-gray-300' : isPositive ? 'text-green-600' : 'text-red-500'}`}>
                {gainPct === null ? '—' : `${isPositive ? '+' : ''}${gainPct.toFixed(1)}% ($${gainDollars.toFixed(2)})`}
              </span>
            )}

            <button
              onClick={() => setPortfolio(portfolio.filter((_, j) => j !== i))}
              className="text-gray-300 hover:text-gray-600 text-lg leading-none"
              title="Remove"
            >
              ×
            </button>
          </div>
        );
      })}
    </div>
  );
}
