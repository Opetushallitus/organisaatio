const express = require('express');
const apiMocker = require('connect-api-mocker');

const port = 9000;
const app = express();
const cors = require('cors');
app.use(cors());
app.use('/', apiMocker('src/api'));

console.log(`Mock API Server is up and running at: http://localhost:${port}`);
app.listen(port);
