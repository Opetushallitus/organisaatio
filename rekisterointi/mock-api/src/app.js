const express = require("express");
const morganBody = require("morgan-body");
const bodyParser = require("body-parser");
const cors = require("cors");

const localisation = require("../../../mock-api/src/api/lokalisointi/cxf/rest/v1/localisation/GET.json");

const port = 9000;
const app = express();

app.use(bodyParser.json());

morganBody(app, { maxBodyLength: 100000 });

app.use(cors());
app.post("/varda-rekisterointi/api/rekisterointi", (_, res) => res.send("ok"));
app.post("/oauth2/token", (_, res) =>
  res.json({ access_token: "token", expires_in: 12346, token_type: "Bear" })
);
app.get("/lokalisointi/cxf/rest/v1/localisation", (_, res) =>
  res.send(localisation)
);

console.info(`Mock API Server is up and running at: http://localhost:${port}`);
app.listen(port);
