const { createProxyMiddleware } = require('http-proxy-middleware');

module.exports = function (app) {
  app.use(
    ['/api', '/login', '/logout', '/auth/csrf', '/oauth2', '/v3/api-docs', '/swagger-ui'],
    createProxyMiddleware({
      target: 'http://localhost:8080',
      changeOrigin: true,
      xfwd: true,
      
    })
  );
};
