import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';

export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    proxy: {
      '/users': 'http://localhost:8080',
      '/portfolios': 'http://localhost:8080',
      '/stocks': 'http://localhost:8080'
    }
  }
});
