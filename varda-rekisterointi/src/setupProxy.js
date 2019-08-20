const proxy = require('http-proxy-middleware');

module.exports = function(app) {
  app.use(proxy('/varda-rekisterointi/hakija/login', { target: 'http://localhost:8081/' }));
  app.use(proxy('/varda-rekisterointi/hakija/valtuudet', { target: 'http://localhost:8081/' }));
  app.use(proxy('/varda-rekisterointi/hakija/api', { target: 'http://localhost:8081/' }));
};
