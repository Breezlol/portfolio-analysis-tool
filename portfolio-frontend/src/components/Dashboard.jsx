import { PieChart, Pie, Cell, Tooltip } from 'recharts';

const COLORS = ['#3a86ff', '#ff006e', '#fb5607', '#ffbe0b', '#8338ec', '#06d6a0', '#118ab2', '#ef476f'];

export default function Dashboard({ valueData, valueLoading, userId, portfolioLength }) {
  if (valueLoading) return <p><em>Calculating portfolio value...</em></p>;

  if (!valueData) {
    if (userId && portfolioLength > 0) return <p style={{ color: 'gray' }}>Current portfolio value is unavailable right now.</p>;
    return null;
  }

  return (
    <>
      <div style={{ background: '#f0f4ff', padding: '12px', marginBottom: '10px', borderRadius: '6px' }}>
        <strong>Total Portfolio Value: ${valueData.totalValue.toFixed(2)}</strong>
        {valueData.warnings && valueData.warnings.length > 0 && (
          <p style={{ color: 'orange', fontSize: '0.9em' }}>Some holdings could not be priced and were excluded from the total.</p>
        )}
      </div>

      {valueData.holdings && valueData.holdings.length > 0 && (
        <div style={{ textAlign: 'center', margin: '16px 0' }}>
          <h3>Allocation</h3>
          <PieChart width={340} height={260} style={{ margin: '0 auto' }}>
            <Pie
              data={valueData.holdings.map(h => ({ name: h.symbol, value: h.marketValue }))}
              cx="50%" cy="50%" outerRadius={90}
              dataKey="value"
              label={({ name, percent }) => `${name} ${(percent * 100).toFixed(1)}%`}
            >
              {valueData.holdings.map((_, i) => (
                <Cell key={i} fill={COLORS[i % COLORS.length]} />
              ))}
            </Pie>
            <Tooltip formatter={(val) => '$' + val.toFixed(2)} />
          </PieChart>
        </div>
      )}

      {valueData.concentrationLabel && (
        <div style={{ background: '#f0f4ff', padding: '12px', marginBottom: '10px', borderRadius: '6px' }}>
          <strong>Diversification: {valueData.concentrationLabel}</strong>
          <p style={{ margin: '4px 0 0', fontSize: '0.9em' }}>{valueData.concentrationExplanation}</p>
        </div>
      )}
    </>
  );
}
