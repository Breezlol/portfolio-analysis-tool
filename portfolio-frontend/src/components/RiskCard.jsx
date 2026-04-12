export default function RiskCard({ analytics, analyticsLoading }) {
  if (analyticsLoading) return <p><em>Calculating risk analytics...</em></p>;
  if (!analytics) return null;
  if (analytics.error) return <p style={{ color: 'gray' }}>{analytics.error}</p>;
  if (analytics.volatility == null) return null;

  return (
    <div style={{ background: '#f0f4ff', padding: '12px', marginBottom: '10px', borderRadius: '6px' }}>
      <strong>Portfolio Volatility: {analytics.volatility}%</strong>
      <p style={{ margin: '4px 0 0', fontSize: '0.9em' }}>Volatility shows how much your portfolio tends to move up and down over time.</p>
      <div style={{ marginTop: '8px' }}>
        <strong>Risk Level: {analytics.riskLabel}</strong>
        <p style={{ margin: '4px 0 0', fontSize: '0.9em' }}>{analytics.riskExplanation}</p>
      </div>
      {analytics.skippedSymbols && analytics.skippedSymbols.length > 0 && (
        <p style={{ color: 'orange', fontSize: '0.85em', marginTop: '8px' }}>Some holdings could not be included because market data was unavailable.</p>
      )}
    </div>
  );
}
