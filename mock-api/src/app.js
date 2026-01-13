const express = require('express');
const morganBody = require('morgan-body');
const bodyParser = require('body-parser');
const apiMocker = require('connect-api-mocker');
const xmlparser = require('express-xml-bodyparser');

const port = 9000;
const app = express();
const cors = require('cors');
const debug = false;
if (debug) {
    app.use(xmlparser());
    app.use(bodyParser.json());
    morganBody(app);
}
app.use(cors());
app.use('/', apiMocker('src/api'));

// endpoints used by rekisterointi-ui
app.post('/varda-rekisterointi/api/rekisterointi', (_, res) => res.send('ok'));
app.post('/oauth2/token', (_, res) => res.json({ access_token: 'token', expires_in: 12346, token_type: 'Bear' }));
app.get('/lokalisointi/cxf/rest/v1/localisation', (_, res) => res.send(localisation));

console.log(`Mock API Server is up and running at: http://localhost:${port}`);
app.listen(port);
