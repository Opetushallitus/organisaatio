import {
  IAMClient,
  Policy,
  GetPolicyCommand,
  CreatePolicyCommand,
  ListPolicyVersionsCommand,
  CreatePolicyVersionCommand,
  DeletePolicyVersionCommand,
} from "@aws-sdk/client-iam";
import { GetCallerIdentityCommand, STSClient } from "@aws-sdk/client-sts";

const PolicyDocument = JSON.stringify({
  Version: "2012-10-17",
  Statement: [
    {
      Effect: "Allow",
      Action: [
          "ssm:*",
          "ecs:*",
          "secretsmanager:*",
          "rds:*",
          "ec2:*",
          "s3:*",
          "iam:*",
          "sns:*",
          "route53:*",
          "acm:*",
          "elasticloadbalancing:*",
          "events:*",
          "lambda:*",
          "logs:*",
          "application-autoscaling:*",
          "cloudwatch:*",
          "kms:*"
      ],
      Resource: "*",
    },
  ],
});

function main() {
  buildPolicyArn().then(updatePolicy);
}

function buildPolicyArn() {
  const policyName = getPolicyName()
  return new STSClient({})
    .send(new GetCallerIdentityCommand({}))
    .then((_) => `arn:aws:iam::${_.Account}:policy/${policyName}`);
}

function getPolicyName() {
  const policyName = process.env.POLICY_NAME;
  if (!policyName) {
    console.error("Policy name missing, aborting");
    process.exit(1);
  }

  return policyName;
}

function updatePolicy(PolicyArn: string) {
  const iamClient = new IAMClient({});
  return getOrCreatePolicy(iamClient, PolicyArn)
    .then(deleteUnusedPolicyVersions(iamClient, PolicyArn))
    .then(createNewDefaultPolicyVersion(iamClient, PolicyArn));
}

function getOrCreatePolicy(iamClient: IAMClient, PolicyArn: string) {
  console.log(`Getting policy ${PolicyArn}`);
  return iamClient
    .send(
      new GetPolicyCommand({
        PolicyArn,
      }),
    )
    .then((_) => {
      console.log(`Found policy ${PolicyArn}`);
      return _.Policy;
    })
    .catch((error) => {
      if (error.name === "NoSuchEntityException") {
        console.log(`Policy ${PolicyArn} no found`);
        return createPolicy(iamClient);
      } else {
        throw error;
      }
    });
}

function createPolicy(iamClient: IAMClient) {
  const PolicyName = getPolicyName();
  console.log("Creating new Policy");
  return iamClient
    .send(
      new CreatePolicyCommand({
        PolicyName,
        PolicyDocument,
      }),
    )
    .then((data) => {
      console.log(`Created policy ${data.Policy?.Arn}`);
      return data.Policy;
    });
}

function deleteUnusedPolicyVersions(iamClient: IAMClient, PolicyArn: string) {
  return (policy?: Policy) => {
    console.log(`Deleting unused versions of ${PolicyArn}`);
    const defaultVersionId = policy?.DefaultVersionId;
    return iamClient
      .send(new ListPolicyVersionsCommand({ PolicyArn }))
      .then((data) => {
        if (!data.Versions) {
          return Promise.resolve([]);
        } else {
          return Promise.all(
            data.Versions.map((version) => {
              if (version.VersionId != defaultVersionId) {
                console.log(
                  `Deleting unused version ${version.VersionId} of ${PolicyArn}`,
                );
                const command = new DeletePolicyVersionCommand({
                  PolicyArn,
                  VersionId: version.VersionId,
                });
                return iamClient.send(command).then((_) => Promise.resolve({}));
              } else {
                return Promise.resolve({});
              }
            }),
          );
        }
      });
  };
}

function createNewDefaultPolicyVersion(
  iamClient: IAMClient,
  PolicyArn: string,
) {
  return () => {
    console.log(`Creating new default version of ${PolicyArn}`);
    iamClient.send(
      new CreatePolicyVersionCommand({
        PolicyArn,
        PolicyDocument,
        SetAsDefault: true,
      }),
    );
  };
}

main();
