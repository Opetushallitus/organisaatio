const express = require("express");
const morganBody = require("morgan-body");
const bodyParser = require("body-parser");
const cors = require("cors");

const port = 9000;
const app = express();

morganBody(app, { maxBodyLength: 100000 });

app.use(bodyParser.json());
app.use(cors());
app.post("/varda-rekisterointi/api/rekisterointi", (_, res) => res.send("ok"));

console.info(`Mock API Server is up and running at: http://localhost:${port}`);
app.listen(port);
