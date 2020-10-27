const { createProxyMiddleware } = require('http-proxy-middleware');
module.exports = function(app) {
  app.use(
    ['/api', '/organisaatio-ui/j_spring_cas_security_check', '/organisaatio-ui/api'],
    createProxyMiddleware({
      target: 'http://localhost:8081',
      //changeOrigin: true,
      pathRewrite: {
        '^/api' : '/organisaatio-ui',
        '^/organisaatio-ui/api' : '/organisaatio-ui',
      },
      logLevel: "debug",
      xfwd: true,
    })
  );
};