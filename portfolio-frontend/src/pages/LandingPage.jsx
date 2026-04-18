export default function LandingPage({ setPage }) {
  return (
    <div className="min-h-screen bg-white flex flex-col items-center justify-center px-6">
      <div className="w-full max-w-sm">
        <p className="text-xs tracking-widest uppercase text-gray-400 mb-6">Portfolio Analysis</p>
        <h1 className="text-4xl font-semibold text-gray-900 mb-2">Johan Avramov's<br />Senior Project</h1>
        <p className="text-gray-400 text-sm mb-10">Track holdings, analyse risk, and see what your money is doing.</p>
        <div className="flex flex-col gap-3">
          <button
            onClick={() => setPage('create')}
            className="w-full bg-white text-gray-900 text-sm font-medium py-3 rounded-lg border border-gray-200"
          >
            New user
          </button>
          <button
            onClick={() => setPage('load')}
            className="w-full bg-white text-gray-900 text-sm font-medium py-3 rounded-lg border border-gray-200"
          >
            Load existing user
          </button>
        </div>
      </div>
    </div>
  );
}
