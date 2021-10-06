const express = require('express');
const morgan = require('morgan')
const apiMocker = require('connect-api-mocker');

const port = 9000;
const app = express();
const cors = require('cors');
app.use(morgan('combined'))
app.use(cors());
app.use('/', apiMocker('src/api'));

console.log(`Mock API Server is up and running at: http://localhost:${port}`);
app.listen(port);
