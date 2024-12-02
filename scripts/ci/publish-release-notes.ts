import {execSync} from 'node:child_process';

const REPOSITORY_NAME = "organisaatio"
const SLACK_NOTIFICATIONS_CHANNEL_WEBHOOK_URL = process.env.SLACK_NOTIFICATIONS_CHANNEL_WEBHOOK_URL as string
const ENVIRONMENT_NAME = process.env.ENVIRONMENT_NAME as string

async function main(): Promise<void> {
  const notes = generateReleaseNotes();
  if (notes.commits.length > 0) {
    const messages = formatSlackMessages(notes.commits, notes.header)
    for (const message of messages) {
      await sendToSlack(message);
    }
  } else {
    console.log("No changes found.");
  }
}

function formatSlackMessages(commits: string[], header?: string): SlackMessage[] {
  const fullText = commits.join('\n');
  if (fullText.length > 3000) {
    const half = Math.floor(commits.length / 2);
    return [
      ...formatSlackMessages(commits.slice(0, half), header),
      ...formatSlackMessages(commits.slice(half))
    ]
  } else {
    const blocks = []
    if (header) {
      blocks.push(makeSlackHeader(header))
    }
    blocks.push(makeSlackMarkdown(fullText))
    return [{ blocks }]
  }
}

type SlackMessage = { blocks: SlackMessageBlock[] }
type SlackMessageBlockHeader = { type: "header", text: { type: "plain_text", text: string } }
type SlackMessageBlockMarkdown = { type: "section", text: { type: "mrkdwn", text: string } }
type SlackMessageBlock = SlackMessageBlockHeader | SlackMessageBlockMarkdown

function makeSlackHeader(text: string): SlackMessageBlockHeader {
  return { type: "header", text: { type: "plain_text", text } }
}
function makeSlackMarkdown(text: string): SlackMessageBlockMarkdown {
  return { type: "section", text: { type: "mrkdwn", text } }
}

async function sendToSlack(message: SlackMessage): Promise<void> {
  console.log("Sending message to Slack:", JSON.stringify(message));
  const response = await fetch(SLACK_NOTIFICATIONS_CHANNEL_WEBHOOK_URL, {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify(message),
  });

  if (!response.ok) {
    throw new Error(`Failed to send message to Slack: ${response.statusText}`);
  }
}

type ReleaseNotes = {
  header: string;
  commits: string[];
}

function parseDateTimeFromTag(tag: string): string {
  const date = new Date(Number(tag.split('-')[2]) * 1000)
  return new Intl.DateTimeFormat('fi-FI', {
    dateStyle: 'short',
    timeStyle: 'short',
    timeZone: 'Europe/Helsinki'
  }).format(date)
}

function generateReleaseNotes(): ReleaseNotes {
  const tags = getLastNTags(2);
  if (tags.length < 2) {
    console.log("Not enough tags to compare.");
    return {header: "", commits: []};
  }

  const date = parseDateTimeFromTag(tags[0])
  const header = `🎁 ${REPOSITORY_NAME} ${ENVIRONMENT_NAME} deployment on ${date}`

  const releaseNotes: string[] = [];
  let prevTag = tags[0];

  for (let i = 1; i < tags.length; i++) {
    const currentTag = tags[i];
    const gitLogLine = getCommitsBetweenTags(currentTag, prevTag);
    if (gitLogLine) {
      for (const logLine of gitLogLine) {
        const space = logLine.indexOf(' ')
        const hash = logLine.substring(0, space)
        const message = linkifyMessage(logLine.substring(space + 1))
        releaseNotes.push(`\`<https://github.com/Opetushallitus/${REPOSITORY_NAME}/commit/${hash}|${hash}>\` ${message}`)
      }
    }
    prevTag = currentTag;
  }

  return {header, commits: releaseNotes};
}

function linkifyMessage(message: string): string {
  return message.replace(/OPHYK-(\d+)/g, '<https://jira.eduuni.fi/browse/OPHYK-$1|OPHYK-$1>');
}

function getLastNTags(n: number): string[] {
  const command = `git tag --list 'green-${ENVIRONMENT_NAME}-*' --sort=-creatordate | head -n ${n}`;
  const tags = runGitCommand(command);
  return tags.split('\n').filter(Boolean);
}

function getCommitsBetweenTags(tag1: string, tag2: string): string[] {
  const command = `git log ${tag1}..${tag2} --oneline`;
  return runGitCommand(command).split("\n").filter(Boolean);
}

function runGitCommand(command: string): string {
  try {
    return execSync(command, {encoding: 'utf-8'}).trim();
  } catch (error) {
    throw new Error(`Git command failed: ${(error as Error).message}`);
  }
}

main().catch(err => {
  console.error(err)
  process.exit(1)
});
