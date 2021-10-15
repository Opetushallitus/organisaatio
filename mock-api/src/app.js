const express = require('express');
<<<<<<< HEAD
const morgan = require('morgan');
=======
const morganBody =require ('morgan-body');
const bodyParser =require ('body-parser');
>>>>>>> 2d1984e1 (implement ytj for uusilomake)
const apiMocker = require('connect-api-mocker');
const xmlparser = require('express-xml-bodyparser');

const port = 9000;
const app = express();
const cors = require('cors');
<<<<<<< HEAD
app.use(morgan('combined'));
=======

app.use(xmlparser());
app.use(bodyParser.json());

// hook morganBody to express app
morganBody(app);
>>>>>>> 2d1984e1 (implement ytj for uusilomake)
app.use(cors());
app.use('/', apiMocker('src/api'));

console.log(`Mock API Server is up and running at: http://localhost:${port}`);
app.listen(port);
