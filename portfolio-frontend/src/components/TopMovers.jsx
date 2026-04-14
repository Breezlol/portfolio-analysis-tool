export default function TopMovers({ topMovers }) {
  if (!topMovers) return null;
  if (!topMovers.topGainers?.length && !topMovers.topLosers?.length) return null;

  return (
    <div className="mb-10">
      <p className="text-xs text-gray-400 uppercase tracking-widest mb-4">Top movers</p>

      <div className="grid grid-cols-2 gap-8">
        <div>
          <p className="text-xs text-gray-400 mb-3">Gainers</p>
          {topMovers.topGainers.length === 0
            ? <p className="text-xs text-gray-300">None</p>
            : topMovers.topGainers.map(m => (
              <div key={m.symbol} className="flex items-center justify-between py-2 border-b border-gray-100 last:border-0">
                <span className="text-sm font-medium text-gray-900">{m.symbol}</span>
                <span className="text-sm text-gray-500">+{m.gainPercent}%</span>
              </div>
            ))
          }
        </div>

        <div>
          <p className="text-xs text-gray-400 mb-3">Losers</p>
          {topMovers.topLosers.length === 0
            ? <p className="text-xs text-gray-300">None</p>
            : topMovers.topLosers.map(m => (
              <div key={m.symbol} className="flex items-center justify-between py-2 border-b border-gray-100 last:border-0">
                <span className="text-sm font-medium text-gray-900">{m.symbol}</span>
                <span className="text-sm text-red-400">{m.gainPercent}%</span>
              </div>
            ))
          }
        </div>
      </div>
    </div>
  );
}
