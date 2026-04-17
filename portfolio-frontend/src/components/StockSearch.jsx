import { useState } from 'react';

export default function StockSearch({ onSelect, existingSymbols }) {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState([]);
  const [searching, setSearching] = useState(false);
  const [loadingSymbol, setLoadingSymbol] = useState(null);

  const search = async () => {
    if (!query.trim()) return;
    setSearching(true);
    try {
      const res = await fetch('/stocks/search?q=' + query.trim());
      const data = await res.json();
      setResults(data.bestMatches || []);
    } finally {
      setSearching(false);
    }
  };

  const handleKey = (e) => { if (e.key === 'Enter') search(); };

  const handleSelect = async (symbol, name) => {
    setLoadingSymbol(symbol);
    try {
      const res = await fetch('/stocks/quote?symbol=' + symbol);
      const price = res.ok ? await res.json() : null;
      if (price == null) { alert('Could not fetch price for ' + symbol + '. Try again in a moment.'); return; }
      onSelect({ symbol, name, currentPrice: price });
      setResults([]);
      setQuery('');
    } finally {
      setLoadingSymbol(null);
    }
  };

  return (
    <div className="mb-6">
      <p className="text-xs text-gray-400 uppercase tracking-widest mb-3">Add holding</p>
      <div className="flex gap-2 mb-3">
        <input
          className="flex-1 text-sm text-gray-900 border border-gray-200 rounded-lg px-3 py-2.5 focus:outline-none focus:border-gray-400"
          placeholder="Search ticker or company name..."
          value={query}
          onChange={e => setQuery(e.target.value)}
          onKeyDown={handleKey}
        />
        <button
          onClick={search}
          disabled={searching}
          className="px-4 py-2.5 bg-gray-900 text-white text-sm font-medium rounded-lg hover:bg-gray-700 disabled:opacity-40"
        >
          Search
        </button>
      </div>

      {results.length > 0 && (
        <div className="border border-gray-100 rounded-lg overflow-hidden">
          {results.map((r, i) => {
            const symbol = r['1. symbol'];
            const name = r['2. name'];
            const already = existingSymbols.includes(symbol);
            const loading = loadingSymbol === symbol;
            return (
              <div key={symbol} className={`flex items-center justify-between px-4 py-3 ${i !== results.length - 1 ? 'border-b border-gray-100' : ''}`}>
                <div>
                  <span className="text-sm font-medium text-gray-900">{symbol}</span>
                  <span className="text-xs text-gray-400 ml-2">{name}</span>
                </div>
                <button
                  onClick={() => handleSelect(symbol, name)}
                  disabled={loading}
                  className="text-xs font-medium px-3 py-1.5 rounded-md border border-gray-200 text-gray-600 hover:bg-gray-50 disabled:opacity-40"
                >
                  {loading ? '…' : already ? 'Buy more' : 'Add'}
                </button>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
