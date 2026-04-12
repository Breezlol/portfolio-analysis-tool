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

  const set = (key, val) => setForm({ ...form, [key]: val });

  const fetchAnalytics = async (uid) => {
    setAnalyticsLoading(true);
    try {
      const res = await fetch('/users/' + uid + '/portfolio/analytics');
      if (res.ok) setAnalytics(await res.json());
    } catch (e) {
      setAnalytics(null);
    } finally {
      setAnalyticsLoading(false);
    }
  };

  const fetchPortfolioValue = async (uid) => {
    if (!uid) return;
    setValueLoading(true);
    try {
      const res = await fetch('/users/' + uid + '/portfolio/value');
      if (res.ok) setValueData(await res.json());
    } catch (e) {
      setValueData(null);
    } finally {
      setValueLoading(false);
    }
    fetchAnalytics(uid);
  };

  useEffect(() => {
    if (page === 'load') fetch('/users').then(r => r.json()).then(setUsers);
  }, [page]);

  if (page === 'create') return <CreateUserPage form={form} set={set} setPage={setPage} />;
  if (page === 'load') return <LoadUserPage users={users} setUserId={setUserId} setForm={setForm} setPortfolio={setPortfolio} fetchPortfolioValue={fetchPortfolioValue} setPage={setPage} />;
  if (page === 'portfolio') return (
    <PortfolioPage
      userId={userId} setUserId={setUserId}
      form={form}
      portfolio={portfolio} setPortfolio={setPortfolio}
      saved={saved} setSaved={setSaved}
      valueData={valueData} valueLoading={valueLoading}
      analytics={analytics} analyticsLoading={analyticsLoading}
      fetchPortfolioValue={fetchPortfolioValue}
      setPage={setPage}
    />
  );

  return <LandingPage setPage={setPage} />;
}
