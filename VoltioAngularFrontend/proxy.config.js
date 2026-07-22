const backendTarget = process.env.VITE_DEV_BACKEND_TARGET || "http://localhost:8080/";

console.log(`\n[Angular Proxy] Forwarding API route families to: ${backendTarget}\n`);

const PROXY_CONFIG = {
  "/api": {
    "target": backendTarget,
    "secure": false,
    "changeOrigin": true
  },
  "/accounts": {
    "target": backendTarget,
    "secure": false,
    "changeOrigin": true
  },
  "/customers": {
    "target": backendTarget,
    "secure": false,
    "changeOrigin": true
  },
  "/standing-orders": {
    "target": backendTarget,
    "secure": false,
    "changeOrigin": true
  }
};

module.exports = PROXY_CONFIG;