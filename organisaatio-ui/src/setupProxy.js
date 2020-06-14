const proxy = require('http-proxy-middleware');

module.exports = function(app) {
  app.use(proxy('/organisaatio/api', { target: 'http://localhost:8188/', xfwd: true }));
};
