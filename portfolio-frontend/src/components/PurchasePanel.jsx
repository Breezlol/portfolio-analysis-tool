import { useState } from 'react';

const fmt = (v) => '$' + Number(v).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

export default function PurchasePanel({ stock, onConfirm, onCancel }) {
  const [qty, setQty] = useState(1);

  if (!stock) return null;

  const total = qty * stock.currentPrice;

  return (
    <div className="border border-gray-200 rounded-xl p-5 mb-8 bg-gray-50">
      <div className="flex items-start justify-between mb-4">
        <div>
          <p className="text-sm font-semibold text-gray-900">{stock.symbol}</p>
          <p className="text-xs text-gray-400 mt-0.5">{stock.name}</p>
        </div>
        <button onClick={onCancel} className="text-gray-300 hover:text-gray-500 text-lg leading-none">×</button>
      </div>

      <div className="flex items-center justify-between mb-4">
        <span className="text-xs text-gray-400 uppercase tracking-widest">Current price</span>
        <span className="text-sm font-medium text-gray-900">{fmt(stock.currentPrice)}</span>
      </div>

      <div className="flex items-center justify-between mb-4">
        <span className="text-xs text-gray-400 uppercase tracking-widest">Quantity</span>
        <div className="flex items-center gap-2">
          <button
            onClick={() => setQty(q => Math.max(1, q - 1))}
            className="w-7 h-7 rounded-md border border-gray-200 text-gray-600 hover:bg-gray-100 text-sm font-medium"
          >−</button>
          <input
            type="number"
            min="1"
            value={qty}
            onChange={e => setQty(Math.max(1, Number(e.target.value)))}
            className="w-16 text-center text-sm text-gray-900 border border-gray-200 rounded-md px-2 py-1 focus:outline-none focus:border-gray-400"
          />
          <button
            onClick={() => setQty(q => q + 1)}
            className="w-7 h-7 rounded-md border border-gray-200 text-gray-600 hover:bg-gray-100 text-sm font-medium"
          >+</button>
        </div>
      </div>

      <div className="flex items-center justify-between mb-5 pt-3 border-t border-gray-200">
        <span className="text-xs text-gray-400 uppercase tracking-widest">Total cost</span>
        <span className="text-sm font-semibold text-gray-900">{fmt(total)}</span>
      </div>

      <div className="flex gap-2">
        <button
          onClick={() => onConfirm(qty)}
          className="flex-1 py-2.5 bg-gray-900 text-white text-sm font-medium rounded-lg hover:bg-gray-700"
        >
          Confirm Purchase
        </button>
        <button
          onClick={onCancel}
          className="px-4 py-2.5 border border-gray-200 text-gray-600 text-sm font-medium rounded-lg hover:bg-gray-100"
        >
          Cancel
        </button>
      </div>
    </div>
  );
}
