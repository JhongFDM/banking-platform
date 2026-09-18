import { fileURLToPath } from "node:url";
import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";

const testSetupFile = fileURLToPath(
  new URL("./src/test/setup.js", import.meta.url),
);

// A hard refresh, direct URL entry, or bookmark on a client-side route like
// /accounts/:id sends a real browser navigation to this exact path - which
// also matches the API proxy rules below, since the frontend and backend
// both use bare /accounts, /customers, /standing-orders paths (see
// nginx.conf for the production equivalent of this same fix). Without this
// check, that navigation gets proxied straight to the backend instead of
// falling through to index.html, so React Router never mounts - and since
// the JWT (stored in localStorage) is only ever attached by the axios
// request interceptor, which needs the app's own JS to have already
// loaded, the backend sees no Authorization header and returns a raw 401
// the browser renders as-is.
//
// Browser navigations send `Accept: text/html`; this app's own API calls
// (axios) don't. Route those specifically to the SPA shell instead of the
// backend, so React Router boots normally and makes its own, correctly
// authenticated request for the same data.
function bypassBrowserNavigation(req) {
  if (req.headers.accept && req.headers.accept.includes("text/html")) {
    return "/index.html";
  }
}

export default defineConfig(({ mode }) => {
  loadEnv(mode, process.cwd(), "");
  const backendTarget =
    process.env.VITE_DEV_BACKEND_TARGET ||
    "http://localhost:8080/";
  return {
    plugins: [react()],
    test: {
      environment: "jsdom",
      globals: true,
      setupFiles: testSetupFile,
    },
    preview: {
      allowedHosts: ["frontend-524103119199.northamerica-northeast2.run.app"],
      host: "0.0.0.0",
      port: 8080,
    },
    server: {
      port: 5173,
      proxy: {
        "/api": {
          target: backendTarget,
          changeOrigin: true,
        },
        "/accounts": {
          target: backendTarget,
          changeOrigin: true,
          bypass: bypassBrowserNavigation,
        },
        "/customers": {
          target: backendTarget,
          changeOrigin: true,
          bypass: bypassBrowserNavigation,
        },
        "/standing-orders": {
          target: backendTarget,
          changeOrigin: true,
          bypass: bypassBrowserNavigation,
        },
      },
    },
  };
});
