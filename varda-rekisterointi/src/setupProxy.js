const proxy = require('http-proxy-middleware');

module.exports = function(app) {
  app.use(proxy('/varda-rekisterointi/api', { target: 'http://localhost:8081/', xfwd: true }));
  app.use(proxy('/varda-rekisterointi/hakija/login', { target: 'http://localhost:8081/', xfwd: true }));
  app.use(proxy('/varda-rekisterointi/hakija/logout', { target: 'http://localhost:8081/', xfwd: true }));
  app.use(proxy('/varda-rekisterointi/hakija/valtuudet', { target: 'http://localhost:8081/', xfwd: true }));
  app.use(proxy('/varda-rekisterointi/hakija/api', { target: 'http://localhost:8081/', xfwd: true }));
  app.use(proxy('/varda-rekisterointi/virkailija/api', { target: 'http://localhost:8081/', xfwd: true }));
  app.use(proxy('/varda-rekisterointi/virkailija/j_spring_cas_security_check', { target: 'http://localhost:8081/', xfwd: true }));
};
