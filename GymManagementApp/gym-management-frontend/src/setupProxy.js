// src/setupProxy.js
const { createProxyMiddleware } = require("http-proxy-middleware");

module.exports = function (app) {
  app.use(
    [
      "/api",
      "/login",              
      "/logout",
      "/csrf",           
      "/oauth2",
      "/oauth2/authorization",
      "/login/oauth2/code"
    ],
    createProxyMiddleware({
      target: "http://localhost:8080",
      changeOrigin: true,
      xfwd: true,
      cookieDomainRewrite: "localhost", 
      logLevel: "silent",
    })
  );
};
