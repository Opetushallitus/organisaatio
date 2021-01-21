const { createProxyMiddleware } = require('http-proxy-middleware');
module.exports = function(app) {
  app.use(
    ['/api', '/organisaatio/j_spring_cas_security_check', '/organisaatio/api', '/organisaatio/login'],
    createProxyMiddleware({
      target: 'http://localhost:8080',
      //changeOrigin: true,
      pathRewrite: {
        '^/api' : '/organisaatio',
        '^/organisaatio/api' : '/organisaatio',
      },
      logLevel: "debug",
      xfwd: true,
    })
  );
};