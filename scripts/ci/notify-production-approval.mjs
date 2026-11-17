import { execFileSync } from "node:child_process";

const webhookUrl = process.env.SLACK_PRODUCTION_APPROVAL_WEBHOOK_URL;
const pipelineName = process.env.PIPELINE_NAME;
const region = process.env.PIPELINE_REGION;

if (!webhookUrl || !pipelineName || !region) {
  throw new Error("Missing Slack webhook or pipeline link configuration.");
}

function git(...args) {
  return execFileSync("git", args, { encoding: "utf8" }).trim();
}

git(
  "fetch",
  "--no-tags",
  "origin",
  "refs/heads/green-prod:refs/remotes/origin/green-prod",
);
git("merge-base", "refs/remotes/origin/green-prod", "HEAD");

const commits = git(
  "log",
  "--reverse",
  "--format=%h (%cI) %s <%an>",
  "refs/remotes/origin/green-prod..HEAD",
);
const commitLines = commits
  ? commits.split("\n")
  : ["Ei uusia committeja verrattuna tuotannossa olevaan versioon."];

const pipelineUrl = `https://${region}.console.aws.amazon.com/codesuite/codepipeline/pipelines/${encodeURIComponent(pipelineName)}/view?region=${encodeURIComponent(region)}`;
const response = await fetch(webhookUrl, {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({
    text: [
      "*Organisaatio: QA → Prod -tuotantodeployn tarkistus*",
      ...commitLines,
      `Tarkista ja hyväksy tai hylkää ajo AWS:ssa: ${pipelineUrl}`,
    ].join("\n"),
  }),
});

if (!response.ok) {
  throw new Error(`Slack notification failed: HTTP ${response.status}`);
}
