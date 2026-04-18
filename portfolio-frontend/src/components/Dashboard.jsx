import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer } from 'recharts';
import { fmt } from '../utils/format';

const COLORS = ['#f97316', '#3b82f6', '#10b981', '#8b5cf6', '#ec4899', '#f59e0b', '#06b6d4'];

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

  return (
    <div className="mb-10">
      <div className="mb-6">
        <p className="text-xs text-gray-400 uppercase tracking-widest mb-1">Portfolio value</p>
        <p className="text-3xl font-semibold text-gray-900">{fmt(valueData.totalValue)}</p>
      </div>

      {valueData.holdings?.length > 0 && (
        <div className="flex flex-col sm:flex-row gap-8 items-start">
          <div className="w-48 h-48 shrink-0">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={valueData.holdings.map(h => ({ name: h.symbol, value: h.marketValue }))}
                  cx="50%" cy="50%"
                  innerRadius={0}
                  outerRadius={90}
                  dataKey="value"
                  stroke="#fff"
                  strokeWidth={2}
                  isAnimationActive={false}
                >
                  {valueData.holdings.map((_, i) => (
                    <Cell key={i} fill={COLORS[i % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip formatter={(val) => fmt(val)} contentStyle={{ fontSize: 12, border: '1px solid #e5e7eb', borderRadius: 8 }} />
              </PieChart>
            </ResponsiveContainer>
          </div>

          <div className="flex-1 min-w-0">
            {valueData.holdings.map((h, i) => (
              <div key={h.symbol} className="flex items-center justify-between py-2 border-b border-gray-100 last:border-0">
                <div className="flex items-center gap-2">
                  <span className="w-2 h-2 rounded-full shrink-0" style={{ background: COLORS[i % COLORS.length] }} />
                  <span className="text-sm font-medium text-gray-900">{h.symbol}</span>
                </div>
                <span className="text-sm text-gray-500">{h.allocationPercentage}%</span>
              </div>
            ))}

            {valueData.concentrationLabel && (
              <div className="mt-4 pt-4 border-t border-gray-100">
                <p className="text-xs font-medium text-gray-900">{valueData.concentrationLabel}</p>
                <p className="text-xs text-gray-400 mt-0.5">{valueData.concentrationExplanation}</p>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
