export default function TopMovers({ topMovers }) {
  if (!topMovers) return null;
  if (!topMovers.topGainers?.length && !topMovers.topLosers?.length) return null;

  const MoverRow = ({ m, positive }) => (
    <div className="flex items-center justify-between py-2 border-b border-gray-100 last:border-0">
      <div>
        <span className="text-sm font-medium text-gray-900">{m.symbol}</span>
        {m.purchasePrice && m.currentPrice && (
          <p className="text-xs text-gray-400 mt-0.5">
            ${m.purchasePrice.toFixed(2)} → ${m.currentPrice.toFixed(2)}
          </p>
        )}
      </div>
      <span className={`text-sm font-medium ${positive ? 'text-green-600' : 'text-red-500'}`}>
        {positive ? '+' : ''}{m.gainPercent}%
      </span>
    </div>
  );

  return (
    <div className="mb-10">
      <p className="text-xs text-gray-400 uppercase tracking-widest mb-4">Top movers</p>

      <div className="grid grid-cols-2 gap-8">
        <div>
          <p className="text-xs text-gray-400 mb-3">Gainers</p>
          {topMovers.topGainers.length === 0
            ? <p className="text-xs text-gray-300">None</p>
            : topMovers.topGainers.map(m => <MoverRow key={m.symbol} m={m} positive={true} />)
          }
        </div>

        <div>
          <p className="text-xs text-gray-400 mb-3">Losers</p>
          {topMovers.topLosers.length === 0
            ? <p className="text-xs text-gray-300">None</p>
            : topMovers.topLosers.map(m => <MoverRow key={m.symbol} m={m} positive={false} />)
          }
        </div>
      </div>
    </div>
  );
}
