const express = require('express');
const morganBody = require('morgan-body');
const bodyParser = require('body-parser');
const apiMocker = require('connect-api-mocker');
const xmlparser = require('express-xml-bodyparser');
const cors = require('cors');

const port = 9000;
const app = express();

app.use(cors());
app.use(xmlparser());
app.use(bodyParser.json());
morganBody(app, { maxBodyLength: 100000 });

app.use('/', apiMocker('src/api'));

console.log(`Mock API Server is up and running at: http://localhost:${port}`);
app.listen(port);
