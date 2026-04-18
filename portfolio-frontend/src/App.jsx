import { useState, useEffect } from 'react';
import './App.css';
import LandingPage from './pages/LandingPage';
import CreateUserPage from './pages/CreateUserPage';
import LoadUserPage from './pages/LoadUserPage';
import PortfolioPage from './pages/PortfolioPage';

export default function App() {
  const [page, setPage] = useState('landing');
  const [userId, setUserId] = useState(null);
  const [users, setUsers] = useState([]);
  const [form, setForm] = useState({ name: '', age: '', sex: '', employmentStatus: '', incomeRange: '', depositAmount: '' });
  const [portfolio, setPortfolio] = useState([]);
  const [saved, setSaved] = useState(false);
  const [valueData, setValueData] = useState(null);
  const [valueLoading, setValueLoading] = useState(false);
  const [analytics, setAnalytics] = useState(null);
  const [analyticsLoading, setAnalyticsLoading] = useState(false);
  const [topMovers, setTopMovers] = useState(null);

  const set = (key, val) => setForm({ ...form, [key]: val });

  const fetchAnalytics = async (uid) => {
    setAnalyticsLoading(true);
    try {
      const res = await fetch('/users/' + uid + '/portfolio/analytics');
      if (!res.ok) throw new Error('Analytics request failed: ' + res.status);
      setAnalytics(await res.json());
    } finally {
      setAnalyticsLoading(false);
    }
  };

  const fetchPortfolioValue = async (uid) => {
    if (!uid) return;
    setValueLoading(true);
    try {
      const res = await fetch('/users/' + uid + '/portfolio/value');
      if (!res.ok) throw new Error('Portfolio value request failed: ' + res.status);
      setValueData(await res.json());
    } finally {
      setValueLoading(false);
    }
    fetchAnalytics(uid);
    fetch('/users/' + uid + '/portfolio/top-movers?k=5')
      .then(r => {
        if (!r.ok) throw new Error('Top movers request failed: ' + r.status);
        return r.json();
      })
      .then(setTopMovers);
  };

  useEffect(() => {
    if (page === 'load') fetch('/users').then(r => r.json()).then(setUsers);
  }, [page]);

  const exitToLanding = () => {
    setUserId(null);
    setForm({ name: '', age: '', sex: '', employmentStatus: '', incomeRange: '', depositAmount: '' });
    setPortfolio([]);
    setSaved(false);
    setValueData(null);
    setAnalytics(null);
    setTopMovers(null);
    setPage('landing');
  };

  if (page === 'create') return <CreateUserPage form={form} set={set} setPage={setPage} />;
  if (page === 'load') return <LoadUserPage users={users} setUserId={setUserId} setForm={setForm} setPortfolio={setPortfolio} fetchPortfolioValue={fetchPortfolioValue} setPage={setPage} />;
  if (page === 'portfolio') return (
    <PortfolioPage
      userId={userId} setUserId={setUserId}
      form={form} set={set}
      portfolio={portfolio} setPortfolio={setPortfolio}
      saved={saved} setSaved={setSaved}
      valueData={valueData} valueLoading={valueLoading}
      analytics={analytics} analyticsLoading={analyticsLoading}
      topMovers={topMovers}
      fetchPortfolioValue={fetchPortfolioValue}
      setPage={setPage}
      exitToLanding={exitToLanding}
    />
  );

  return <LandingPage setPage={setPage} />;
}
