const url = require("url");
const https = require("https");

const COLOR_GREEN = "#36a64f";
const COLOR_RED = "#a63636";

exports.handler = async (event, context) => {
  const message = JSON.parse(event.Records[0].Sns.Message);
  const alarmName = message.AlarmName;
  const newState = message.NewStateValue;
  const reason = message.NewStateReason;

  const slackMessage = `${alarmName} state is now ${newState}: ${reason}`;

  console.log("Posting message to Slack:", slackMessage);
  const slackWebhook = await getWebhook();
  const color = newState === "OK" ? COLOR_GREEN : COLOR_RED;
  const response = await postMessage(
    slackWebhook,
    mkSlackMessage(slackMessage, color),
  );

  if (response.statusCode < 400) {
    console.log("Message posted successfully");
  } else if (response.statusCode < 500) {
    fail(
      `Error posting message to Slack API: ${response.statusCode} - ${response.statusMessage}`,
    );
  } else {
    fail(
      `Server error when processing message: ${response.statusCode} - ${response.statusMessage}`,
    );
  }
};

async function postMessage(slackWebhook, message) {
  const body = JSON.stringify(message);
  const options = url.parse(slackWebhook);
  options.method = "POST";
  options.headers = {
    "Content-Type": "application/json",
    "Content-Length": Buffer.byteLength(body),
  };

  return await new Promise((resolve, reject) => {
    const req = https.request(options, (res) => {
      const chunks = [];
      res.setEncoding("utf8");
      res.on("data", (chunk) => chunks.push(chunk));
      res.on("end", () => {
        const body = chunks.join("");
        resolve({
          body: body,
          statusCode: res.statusCode,
          statusMessage: res.statusMessage,
        });
      });
      return res;
    });

    req.on("error", (err) => reject(err));
    req.write(body);
    req.end();
  });
}

function mkSlackMessage(errorText, color) {
  // https://api.slack.com/messaging/attachments-to-blocks#direct_equivalents
  return {
    attachments: [
      {
        color,
        text: errorText,
      },
    ],
  };
}

const secretId = "slack-webhook";
const secretsExtensionHttpPort = 2773;
const secretsExtensionEdpoint = `http://localhost:${secretsExtensionHttpPort}/secretsmanager/get?secretId=${secretId}&withDecryption=true`;

async function getWebhook() {
  // https://docs.aws.amazon.com/secretsmanager/latest/userguide/retrieving-secrets_lambda.html
  const response = await fetch(secretsExtensionEdpoint, {
    headers: {
      "X-Aws-Parameters-Secrets-Token": process.env.AWS_SESSION_TOKEN,
    },
  });

  return response.json().then((j) => j.SecretString);
}

function fail(message) {
  console.log(message);
  throw new Error(message);
}
