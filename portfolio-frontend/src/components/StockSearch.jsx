import { useState } from 'react';

export default function StockSearch({ onAdd, existingSymbols }) {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState([]);

  const search = async () => {
    if (!query.trim()) return;
    const res = await fetch('/stocks/search?q=' + query.trim());
    const data = await res.json();
    setResults(data.bestMatches || []);
  };

  const handleAdd = async (symbol, name) => {
    if (existingSymbols.includes(symbol)) return;
    const res = await fetch('/stocks/quote?symbol=' + symbol);
    const price = res.ok ? await res.json() : null;
    if (price == null) { alert('Could not fetch price for ' + symbol + '. Try again in a moment.'); return; }
    onAdd({ symbol, name, quantity: 1, purchasePrice: price });
    setResults([]);
    setQuery('');
  };

  return (
    <>
      <input placeholder="Search stock (e.g. AAPL)" value={query} onChange={e => setQuery(e.target.value)} />
      <button onClick={search}>Search</button>
      {results.length > 0 && (
        <ul>
          {results.map(r => (
            <li key={r['1. symbol']}>
              {r['1. symbol']} - {r['2. name']}
              <button onClick={() => handleAdd(r['1. symbol'], r['2. name'])}>Add</button>
            </li>
          ))}
        </ul>
      )}
    </>
  );
}
