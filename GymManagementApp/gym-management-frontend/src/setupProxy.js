const { createProxyMiddleware } = require('http-proxy-middleware');

module.exports = function (app) {
  app.use(
    '/api',
    createProxyMiddleware({
      target: 'http://localhost:8080',
      changeOrigin: true
    })
  );


  app.use(
    ['/oauth2/authorization', '/login/oauth2/code'],
    createProxyMiddleware({
      target: 'http://localhost:8080',
      changeOrigin: true,
      xfwd: false 
    })
  );
};
