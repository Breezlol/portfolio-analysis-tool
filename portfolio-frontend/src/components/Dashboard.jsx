export default function Dashboard({ valueData, valueLoading, userId, portfolioLength }) {
  if (valueLoading) return (
    <div className="mb-8">
      <p className="text-sm text-gray-400">Calculating portfolio value...</p>
    </div>
  );

  if (!valueData) {
    if (userId && portfolioLength > 0) return (
      <div className="mb-8">
        <p className="text-sm text-gray-400">Portfolio value unavailable right now.</p>
      </div>
    );
    return null;
  }

  const fmt = (v) => '$' + Number(v).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

  return (
    <div className="mb-10">
      <div className="mb-6">
        <p className="text-xs text-gray-400 uppercase tracking-widest mb-1">Portfolio value</p>
        <p className="text-3xl font-semibold text-gray-900">{fmt(valueData.totalValue)}</p>
        {valueData.warnings?.length > 0 && (
          <p className="text-xs text-gray-400 mt-1">Some holdings excluded — price unavailable</p>
        )}
      </div>
    </div>
  );
}
