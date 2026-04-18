import { useState } from 'react';
import { fmt } from '../utils/format';

export default function PortfolioTable({ portfolio, setPortfolio, valueData, userId }) {
  const [from, setFrom] = useState('');
  const [to, setTo] = useState('');
  const [rangeResult, setRangeResult] = useState(null);
  const [filtering, setFiltering] = useState(false);

  const priceMap = {};
  if (valueData?.holdings) {
    for (const h of valueData.holdings) priceMap[h.symbol] = h;
  }
  const hasLive = Object.keys(priceMap).length > 0;
  const cols = hasLive ? 'grid-cols-[1fr_50px_90px_90px_40px]' : 'grid-cols-[1fr_50px_110px_40px]';

  const totalValue = hasLive
    ? valueData.totalValue
    : portfolio.reduce((sum, s) => sum + s.quantity * s.purchasePrice, 0);

  const handleFilter = async () => {
    if (!from.trim() || !to.trim() || !userId) return;
    setFiltering(true);
    try {
      const res = await fetch(`/users/${userId}/portfolio/range?from=${from.toUpperCase()}&to=${to.toUpperCase()}`);
      if (!res.ok) throw new Error('Range filter failed: ' + res.status);
      setRangeResult(await res.json());
    } finally {
      setFiltering(false);
    }
  };

  const handleClear = () => {
    setRangeResult(null);
    setFrom('');
    setTo('');
  };

  const displayItems = rangeResult
    ? rangeResult.items.map(item => portfolio.find(p => p.symbol === item.symbol) || item)
    : portfolio;

  return (
    <div className="mb-8">
      <div className="flex items-center justify-between mb-4">
        <p className="text-xs text-gray-400 uppercase tracking-widest">My Holdings</p>
        {userId && portfolio.length > 0 && (
          <div className="flex items-center gap-2">
            <input
              value={from}
              onChange={e => setFrom(e.target.value.toUpperCase())}
              placeholder="From"
              maxLength={5}
              className="w-16 text-xs text-gray-900 border border-gray-200 rounded-md px-2 py-1 focus:outline-none focus:border-gray-400 uppercase"
            />
            <span className="text-xs text-gray-400">to</span>
            <input
              value={to}
              onChange={e => setTo(e.target.value.toUpperCase())}
              placeholder="To"
              maxLength={5}
              className="w-16 text-xs text-gray-900 border border-gray-200 rounded-md px-2 py-1 focus:outline-none focus:border-gray-400 uppercase"
            />
            <button
              onClick={handleFilter}
              disabled={filtering || !from || !to}
              className="text-xs px-3 py-1 bg-white text-gray-900 rounded-md border border-gray-200 disabled:opacity-40"
            >
              Filter
            </button>
            {rangeResult && (
              <button onClick={handleClear} className="text-xs text-gray-400">
                Clear
              </button>
            )}
          </div>
        )}
      </div>

      {rangeResult && (
        <div className="mb-4 px-3 py-2 bg-gray-50 rounded-lg border border-gray-100">
          <p className="text-xs text-gray-500">
            AVL tree searched <span className="font-semibold text-gray-900">{rangeResult.nodesVisited}</span> of{' '}
            <span className="font-semibold text-gray-900">{rangeResult.totalNodes}</span> nodes — returned{' '}
            <span className="font-semibold text-gray-900">{rangeResult.items.length}</span> result{rangeResult.items.length !== 1 ? 's' : ''}.{' '}
            <span className="text-gray-400">O(log n + k) vs O(n) for a plain list.</span>
          </p>
        </div>
      )}

      {portfolio.length === 0 ? (
        <p className="text-sm text-gray-400">No holdings yet. Add a stock above.</p>
      ) : displayItems.length === 0 ? (
        <p className="text-sm text-gray-400">No holdings found in range {from} → {to}.</p>
      ) : (
        <>
          <div className={`border-b border-gray-100 pb-2 mb-1 grid gap-4 ${cols}`}>
            <span className="text-xs text-gray-400 uppercase tracking-widest">Symbol</span>
            <span className="text-xs text-gray-400 uppercase tracking-widest">Qty</span>
            <span className="text-xs text-gray-400 uppercase tracking-widest">Buy price</span>
            {hasLive && <span className="text-xs text-gray-400 uppercase tracking-widest">Current</span>}
            <span />
          </div>

          {displayItems.map((s, i) => {
            const live = priceMap[s.symbol];
            const portfolioIndex = portfolio.findIndex(p => p.symbol === s.symbol);

            return (
              <div key={s.symbol} className={`grid gap-4 items-center py-3 border-b border-gray-100 last:border-0 ${cols}`}>
                <span className="text-sm font-medium text-gray-900">{s.symbol}</span>
                <span className="text-sm text-gray-700">{s.quantity}</span>

                <span className="text-sm text-gray-700">{fmt(live ? live.purchasePrice : s.purchasePrice)}</span>

                {hasLive && (
                  <span className="text-sm text-gray-700">{live ? fmt(live.currentPrice) : '—'}</span>
                )}

                <button
                  onClick={() => portfolioIndex !== -1 && setPortfolio(portfolio.filter((_, j) => j !== portfolioIndex))}
                  className="text-gray-300 text-lg leading-none"
                  title="Remove"
                >×</button>
              </div>
            );
          })}

          {!rangeResult && (
            <div className="flex justify-between items-center pt-4 mt-2 border-t border-gray-100">
              <span className="text-xs text-gray-400 uppercase tracking-widest">Total value</span>
              <span className="text-sm font-semibold text-gray-900">{fmt(totalValue)}</span>
            </div>
          )}
        </>
      )}
    </div>
  );
}
