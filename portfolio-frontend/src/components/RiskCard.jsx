import { useState } from 'react';
import { fmt } from '../utils/format';

const riskColor = {
  'Low': 'text-gray-400',
  'Moderate': 'text-yellow-600',
  'High': 'text-orange-500',
  'Very High': 'text-red-500',
};

const INFO = {
  volatility: {
    what: 'Measures how much your portfolio value fluctuates day to day. Higher volatility means more unpredictable price swings.',
    how: 'Daily returns are calculated for each holding, weighted by portfolio share, then annualised: daily standard deviation × √252 trading days.',
  },
  sharpe: {
    what: 'How much return you earn per unit of risk taken. Above 1 is considered good. Below 0 means a risk-free savings account would have served you better.',
    how: '(Annualised return − 4% risk-free rate) ÷ annualised volatility.',
  },
  var: {
    what: 'On 95% of trading days your portfolio should not lose more than this amount. Roughly 1 bad day per month could exceed it.',
    how: 'Portfolio value × daily volatility × 1.645 (the z-score for 95% confidence), assuming normally distributed returns.',
  },
};

function InfoToggle({ id, active, onToggle }) {
  return (
    <button
      onClick={() => onToggle(id)}
      className={`ml-1.5 text-xs w-4 h-4 rounded-full border inline-flex items-center justify-center leading-none ${active ? 'bg-gray-900 text-white border-gray-900' : 'text-gray-300 border-gray-300'}`}
    >?</button>
  );
}

function InfoBox({ info }) {
  return (
    <div className="mt-3 p-3 bg-gray-50 rounded-lg border border-gray-100 col-span-3">
      <p className="text-xs text-gray-600 mb-1">{info.what}</p>
      <p className="text-xs text-gray-400"><span className="font-medium text-gray-500">How it's calculated: </span>{info.how}</p>
    </div>
  );
}

export default function RiskCard({ analytics, analyticsLoading }) {
  const [open, setOpen] = useState(null);
  const toggle = (id) => setOpen(prev => prev === id ? null : id);

  if (analyticsLoading) return (
    <div className="mb-8">
      <p className="text-sm text-gray-400">Calculating risk analytics...</p>
    </div>
  );
  if (!analytics || analytics.error || analytics.volatility == null) return null;

  return (
    <div className="mb-10">
      <p className="text-xs text-gray-400 uppercase tracking-widest mb-4">Risk analytics</p>

      <div className="grid grid-cols-3 gap-6 mb-2">
        <div>
          <div className="flex items-center mb-1">
            <p className="text-xs text-gray-400">Volatility</p>
            <InfoToggle id="volatility" active={open === 'volatility'} onToggle={toggle} />
          </div>
          <p className="text-2xl font-semibold text-gray-900">{analytics.volatility}%</p>
          <p className={`text-xs font-medium mt-0.5 ${riskColor[analytics.riskLabel] || 'text-gray-500'}`}>{analytics.riskLabel}</p>
        </div>

        {analytics.sharpeRatio != null && (
          <div>
            <div className="flex items-center mb-1">
              <p className="text-xs text-gray-400">Sharpe ratio</p>
              <InfoToggle id="sharpe" active={open === 'sharpe'} onToggle={toggle} />
            </div>
            <p className="text-2xl font-semibold text-gray-900">{analytics.sharpeRatio}</p>
            <p className="text-xs text-gray-400 mt-0.5">Return per unit risk</p>
          </div>
        )}

        {analytics.var95 != null && (
          <div>
            <div className="flex items-center mb-1">
              <p className="text-xs text-gray-400">1-day 95% VaR</p>
              <InfoToggle id="var" active={open === 'var'} onToggle={toggle} />
            </div>
            <p className="text-2xl font-semibold text-gray-900">{fmt(analytics.var95)}</p>
            <p className="text-xs text-gray-400 mt-0.5">Max expected loss</p>
          </div>
        )}
      </div>

      {open && <InfoBox info={INFO[open]} />}

      <p className="text-xs text-gray-400 mt-4">{analytics.riskExplanation}</p>
    </div>
  );
}
