const riskColor = {
  'Low': 'text-gray-400',
  'Moderate': 'text-yellow-600',
  'High': 'text-orange-500',
  'Very High': 'text-red-500',
};

export default function RiskCard({ analytics, analyticsLoading }) {
  if (analyticsLoading) return (
    <div className="mb-8">
      <p className="text-sm text-gray-400">Calculating risk analytics...</p>
    </div>
  );
  if (!analytics || analytics.error || analytics.volatility == null) return null;

  return (
    <div className="mb-10">
      <p className="text-xs text-gray-400 uppercase tracking-widest mb-4">Risk analytics</p>

      <div className="grid grid-cols-3 gap-6 mb-6">
        <div>
          <p className="text-xs text-gray-400 mb-1">Volatility</p>
          <p className="text-2xl font-semibold text-gray-900">{analytics.volatility}%</p>
          <p className={`text-xs font-medium mt-0.5 ${riskColor[analytics.riskLabel] || 'text-gray-500'}`}>{analytics.riskLabel}</p>
        </div>
        {analytics.sharpeRatio != null && (
          <div>
            <p className="text-xs text-gray-400 mb-1">Sharpe ratio</p>
            <p className="text-2xl font-semibold text-gray-900">{analytics.sharpeRatio}</p>
            <p className="text-xs text-gray-400 mt-0.5">Return per unit risk</p>
          </div>
        )}
        {analytics.var95 != null && (
          <div>
            <p className="text-xs text-gray-400 mb-1">1-day 95% VaR</p>
            <p className="text-2xl font-semibold text-gray-900">${analytics.var95.toLocaleString('en-US', { minimumFractionDigits: 2 })}</p>
            <p className="text-xs text-gray-400 mt-0.5">Max expected loss</p>
          </div>
        )}
      </div>

      <p className="text-xs text-gray-400">{analytics.riskExplanation}</p>

      {analytics.skippedSymbols?.length > 0 && (
        <p className="text-xs text-gray-400 mt-2">Some holdings excluded — market data unavailable.</p>
      )}
    </div>
  );
}
