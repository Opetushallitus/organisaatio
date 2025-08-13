import * as cdk from "aws-cdk-lib";
import * as codebuild from "aws-cdk-lib/aws-codebuild";
import * as codepipeline from "aws-cdk-lib/aws-codepipeline"
import * as codepipeline_actions from "aws-cdk-lib/aws-codepipeline-actions"
import * as codestarconnections from "aws-cdk-lib/aws-codestarconnections";
import * as constructs from "constructs";
import * as iam from "aws-cdk-lib/aws-iam";
import * as ssm from "aws-cdk-lib/aws-ssm";
import {ROUTE53_HEALTH_CHECK_REGION} from "./health-check";

class CdkAppUtil extends cdk.App {
  constructor(props: cdk.AppProps) {
    super(props);
    const env = {
      account: process.env.CDK_DEFAULT_ACCOUNT,
      region: process.env.CDK_DEFAULT_REGION,
    };
    new ContinuousDeploymentStack(
      this,
      "ContinuousDeploymentStack",
      {
        env,
      }
    );
  }
}

class ContinuousDeploymentStack extends cdk.Stack {
  constructor(scope: constructs.Construct, id: string, props: cdk.StackProps) {
    super(scope, id, props);

    new ContinuousDeploymentPipelineStack(
      this,
      "HahtuvaContinuousDeploymentPipelineStack",
      "hahtuva",
      { owner: "Opetushallitus", name: "organisaatio", branch: "master" },
      props
    );
    new ContinuousDeploymentPipelineStack(
      this,
      "DevContinuousDeploymentPipelineStack",
      "dev",
      { owner: "Opetushallitus", name: "organisaatio", branch: "green-hahtuva" },
      props
    );
    new ContinuousDeploymentPipelineStack(
      this,
      "QaContinuousDeploymentPipelineStack",
      "qa",
      { owner: "Opetushallitus", name: "organisaatio", branch: "green-dev" },
      props
    );
    new ContinuousDeploymentPipelineStack(
      this,
      "ProdContinuousDeploymentPipelineStack",
      "prod",
      { owner: "Opetushallitus", name: "organisaatio", branch: "green-qa" },
      props
    );

    const radiatorAccountId = "905418271050"
    const radiatorReader = new iam.Role(this, "RadiatorReaderRole", {
      assumedBy: new iam.AccountPrincipal(radiatorAccountId),
      roleName: "RadiatorReader",
    })
    radiatorReader.addManagedPolicy(iam.ManagedPolicy.fromAwsManagedPolicyName("AWSCodePipeline_ReadOnlyAccess"))
  }
}

type EnvironmentName = "hahtuva" | "dev" | "qa" | "prod";

type Repository = {
  owner: string;
  name: string;
  branch: string;
};

class ContinuousDeploymentPipelineStack extends cdk.Stack {
  constructor(
    scope: constructs.Construct,
    id: string,
    env: EnvironmentName,
    repository: Repository,
    props: cdk.StackProps
  ) {
    super(scope, id, props);
    const capitalizedEnv = capitalize(env);

    const connection = new codestarconnections.CfnConnection(
      this,
      "GithubConnection",
      {
        connectionName: "GithubConnection",
        providerType: "GitHub",
      },
    );

    const pipeline = new codepipeline.Pipeline(
      this,
      `DeployPipeline`,
      {
        pipelineName: `Deploy${capitalizedEnv}`,
        pipelineType: codepipeline.PipelineType.V1,
      }
    );
    cdk.Tags.of(pipeline).add("Repository", `${repository.owner}/${repository.name}`, { includeResourceTypes: ["AWS::CodePipeline::Pipeline"] });
    cdk.Tags.of(pipeline).add("FromBranch", repository.branch, { includeResourceTypes: ["AWS::CodePipeline::Pipeline"] });
    cdk.Tags.of(pipeline).add("ToBranch", `green-${env}`, { includeResourceTypes: ["AWS::CodePipeline::Pipeline"] });

    const sourceOutput = new codepipeline.Artifact();
    const sourceAction =
      new codepipeline_actions.CodeStarConnectionsSourceAction({
        actionName: "Source",
        connectionArn: connection.attrConnectionArn,
        codeBuildCloneOutput: true,
        owner: repository.owner,
        repo: repository.name,
        branch: repository.branch,
        output: sourceOutput,
        triggerOnPush: ["hahtuva", "dev"].includes(env),
      });
    const sourceStage = pipeline.addStage({ stageName: "Source" });
    sourceStage.addAction(sourceAction);

    const runTests = env === "hahtuva";
    if (runTests) {
      const testStage = pipeline.addStage({ stageName: "Test" });

      const amazonLinuxTests = [
        { name: "OrganisaatioService", commands: ["scripts/ci/run-java-tests.sh"] },
        { name: "OrganisaatioFrontend", commands: ["scripts/ci/run-frontend-tests.sh"] },
        { name: "VardaRekisterointiJunit", commands: ["scripts/ci/run-varda-rekisterointi-java-tests.sh"] },
      ]
      for (const test of amazonLinuxTests) {
        testStage.addAction(
          new codepipeline_actions.CodeBuildAction({
            actionName: test.name,
            input: sourceOutput,
            project: makeAmazonLinuxTestProject(this, env, `TestOrganisaatio${test.name}`, test.commands),
          })
        );
      }

      const ubuntuTests = [
        { name: "OrganisaatioPlaywright", commands: ["scripts/ci/run-playwright-tests.sh test:organisaatiot"] },
        { name: "OsoitepalveluPlaywright", commands: ["scripts/ci/run-playwright-tests.sh test:osoitepalvelu"] },
        { name: "RekisterointiPlaywright", commands: ["scripts/ci/run-rekisterointi-tests.sh"] },
      ]
      for (const test of ubuntuTests) {
        testStage.addAction(
          new codepipeline_actions.CodeBuildAction({
            actionName: test.name,
            input: sourceOutput,
            outputs: [new codepipeline.Artifact(test.name + "Output")],
            project: makeUbuntuTestProject(this, env, `TestOrganisaatio${test.name}`, test.commands),
          })
        );
      }
    }

    const deployProject = new codebuild.PipelineProject(
      this,
      `DeployProject`,
      {
        projectName: `Deploy${capitalizedEnv}`,
        concurrentBuildLimit: 1,
        environment: {
          buildImage: codebuild.LinuxArmBuildImage.AMAZON_LINUX_2_STANDARD_3_0,
          computeType: codebuild.ComputeType.SMALL,
          privileged: true,
        },
        environmentVariables: {
          CDK_DEPLOY_TARGET_ACCOUNT: {
            type: codebuild.BuildEnvironmentVariableType.PARAMETER_STORE,
            value: `/env/${env}/account_id`,
          },
          CDK_DEPLOY_TARGET_REGION: {
            type: codebuild.BuildEnvironmentVariableType.PARAMETER_STORE,
            value: `/env/${env}/region`,
          },
          DOCKER_USERNAME: {
            type: codebuild.BuildEnvironmentVariableType.PARAMETER_STORE,
            value: "/docker/username",
          },
          DOCKER_PASSWORD: {
            type: codebuild.BuildEnvironmentVariableType.PARAMETER_STORE,
            value: "/docker/password",
          },
          SLACK_NOTIFICATIONS_CHANNEL_WEBHOOK_URL: {
            type: codebuild.BuildEnvironmentVariableType.PARAMETER_STORE,
            value: `/env/${env}/slack-notifications-channel-webhook`,
          },
          GITHUB_PACKAGES_GRADLE_PROPERTIES: {
            type: codebuild.BuildEnvironmentVariableType.PARAMETER_STORE,
            value: "/gradle/github-packages-gradle-properties",
          },
          MVN_SETTINGSXML: {
            type: codebuild.BuildEnvironmentVariableType.PARAMETER_STORE,
            value: `/mvn/settingsxml`,
          },
        },
        buildSpec: codebuild.BuildSpec.fromObject({
          version: "0.2",
          env: {
            "git-credential-helper": "yes",
          },
          phases: {
            pre_build: {
              commands: [
                "sudo yum install -y perl-Digest-SHA", // for shasum command
                "echo $GITHUB_PACKAGES_GRADLE_PROPERTIES | base64 -d > github-packages-gradle.properties",
                "echo $MVN_SETTINGSXML > ./settings.xml",
                "echo $MVN_SETTINGSXML > ./varda-rekisterointi/settings.xml",
                "echo $MVN_SETTINGSXML > ./rekisterointi/settings.xml",
              ],
            },
            build: {
              commands: [
                `./deploy-${env}.sh && ./scripts/ci/tag-green-build-${env}.sh && ./scripts/ci/publish-release-notes-${env}.sh`,
              ],
            },
          },
        }),
      }
    );

    const deploymentTargetAccount = ssm.StringParameter.valueFromLookup(
      this,
      `/env/${env}/account_id`
    );
    const deploymentTargetRegion = ssm.StringParameter.valueFromLookup(
      this,
      `/env/${env}/region`
    );

    const targetRegions = [deploymentTargetRegion, ROUTE53_HEALTH_CHECK_REGION];
    deployProject.role?.attachInlinePolicy(
      new iam.Policy(this, `Deploy${capitalizedEnv}Policy`, {
        statements: [
          new iam.PolicyStatement({
            effect: iam.Effect.ALLOW,
            actions: ["sts:AssumeRole"],
            resources: targetRegions.flatMap(targetRegion => [
              `arn:aws:iam::${deploymentTargetAccount}:role/cdk-hnb659fds-lookup-role-${deploymentTargetAccount}-${targetRegion}`,
              `arn:aws:iam::${deploymentTargetAccount}:role/cdk-hnb659fds-file-publishing-role-${deploymentTargetAccount}-${targetRegion}`,
              `arn:aws:iam::${deploymentTargetAccount}:role/cdk-hnb659fds-image-publishing-role-${deploymentTargetAccount}-${targetRegion}`,
              `arn:aws:iam::${deploymentTargetAccount}:role/cdk-hnb659fds-deploy-role-${deploymentTargetAccount}-${targetRegion}`,
            ])
          }),
        ],
      })
    );
    const deployAction = new codepipeline_actions.CodeBuildAction({
      actionName: "Deploy",
      input: sourceOutput,
      project: deployProject,
    });
    const deployStage = pipeline.addStage({ stageName: "Deploy" });
    deployStage.addAction(deployAction);
  }
}

function makeAmazonLinuxTestProject(
  scope: constructs.Construct,
  env: string,
  name: string,
  testCommands: string[],
): codebuild.PipelineProject {
  return makeTestProject(scope, env, name, testCommands, codebuild.LinuxArmBuildImage.AMAZON_LINUX_2_STANDARD_3_0, [
    "sudo yum install -y perl-Digest-SHA", // for shasum command
  ]);
}

function makeUbuntuTestProject(
  scope: constructs.Construct,
  env: string,
  name: string,
  testCommands: string[],
): codebuild.PipelineProject {
  return makeTestProject(scope, env, name, testCommands, codebuild.LinuxBuildImage.STANDARD_7_0,  [
    "sudo apt-get update -y",
    "sudo apt-get install -y netcat", // for nc command
    "sudo apt-get install -y libgtk2.0-0 libgtk-3-0 libgbm-dev libnotify-dev libnss3 libxss1 libasound2 libxtst6 xauth xvfb", // For Chromium
  ]);
}

function makeTestProject(
  scope: constructs.Construct,
  env: string,
  name: string,
  testCommands: string[],
  buildImage: codebuild.IBuildImage,
  preBuildCommands: string[],
) {
  return new codebuild.PipelineProject(
    scope,
    `${name}${capitalize(env)}Project`,
    {
      projectName: `${name}${capitalize(env)}`,
      environment: {
        buildImage: buildImage,
        computeType: codebuild.ComputeType.MEDIUM,
        privileged: true,
      },
      environmentVariables: {
        DOCKER_USERNAME: {
          type: codebuild.BuildEnvironmentVariableType.PARAMETER_STORE,
          value: "/docker/username",
        },
        DOCKER_PASSWORD: {
          type: codebuild.BuildEnvironmentVariableType.PARAMETER_STORE,
          value: "/docker/password",
        },
        GITHUB_PACKAGES_GRADLE_PROPERTIES: {
          type: codebuild.BuildEnvironmentVariableType.PARAMETER_STORE,
          value: "/gradle/github-packages-gradle-properties",
        },
        MVN_SETTINGSXML: {
          type: codebuild.BuildEnvironmentVariableType.PARAMETER_STORE,
          value: `/mvn/settingsxml`,
        },
        TZ: {
          type: codebuild.BuildEnvironmentVariableType.PLAINTEXT,
          value: "Europe/Helsinki",
        }
      },
      buildSpec: codebuild.BuildSpec.fromObject({
        version: "0.2",
        env: {
          "git-credential-helper": "yes",
        },
        artifacts: {
          files: ["playwright-organisaatio-ui.tar.gz"],
        },
        phases: {
          install: {
            "runtime-versions": {
              java: "corretto21",
            },
          },
          pre_build: {
            commands: [
              "docker login --username $DOCKER_USERNAME --password $DOCKER_PASSWORD",
              ...preBuildCommands,
              "mkdir -p ~/.gradle && echo $GITHUB_PACKAGES_GRADLE_PROPERTIES | base64 -d > ~/.gradle/gradle.properties",
              "echo $GITHUB_PACKAGES_GRADLE_PROPERTIES | base64 -d > ./organisaatio-service/github-packages-gradle.properties",
              "echo $MVN_SETTINGSXML > ./settings.xml",
              "echo $MVN_SETTINGSXML > ./varda-rekisterointi/settings.xml",
              "echo $MVN_SETTINGSXML > ./rekisterointi/settings.xml",
            ]
          },
          build: {
            commands: testCommands,
          },
          post_build: {
            commands: [
              "mkdir -p playwright/test-results && touch playwright/test-results/dummy && tar czf playwright-organisaatio-ui.tar.gz playwright/test-results/*"
            ]
          }
        },
      }),
    }
  );
}

function capitalize(s: string) {
  return s.charAt(0).toUpperCase() + s.slice(1);
}

const app = new CdkAppUtil({});
app.synth();
