export default function LandingPage({ setPage }) {
  return (
    <div className="app-container">
      <h1>Portfolio Analysis Tool</h1>
      <button onClick={() => setPage('create')}>Create New User</button>
      <button onClick={() => setPage('load')}>Load Existing User</button>
    </div>
  );
}
