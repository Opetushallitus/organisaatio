import * as cdk from "aws-cdk-lib";
import * as sns from "aws-cdk-lib/aws-sns";
import * as constructs from "constructs";
import * as route53 from "aws-cdk-lib/aws-route53";
import * as config from "./config";
import * as cloudwatch from "aws-cdk-lib/aws-cloudwatch";
import * as cloudwatch_actions from "aws-cdk-lib/aws-cloudwatch-actions";
import * as lambda from "aws-cdk-lib/aws-lambda";
import * as sns_subscriptions from "aws-cdk-lib/aws-sns-subscriptions";

export const ROUTE53_HEALTH_CHECK_REGION = "us-east-1"

export function createHealthCheckStacks(app: cdk.App, alarmsToSlackLambda: lambda.IFunction) {
  const healthCheckStack = new GlobalHealthCheckStack(app, "GlobalHealthCheckStack", {
    env: {
      account: process.env.CDK_DEPLOY_TARGET_ACCOUNT,
      region: ROUTE53_HEALTH_CHECK_REGION,
    }
  });
  new RegionalHealthCheckStack(app, "RegionalHealthCheckStack", {
    env: {
      account: process.env.CDK_DEPLOY_TARGET_ACCOUNT,
      region: process.env.CDK_DEPLOY_TARGET_REGION,
    },
    globalAlarmTopicArn: healthCheckStack.globalAlarmTopic.topicArn,
    alarmsToSlackLambdaArn: alarmsToSlackLambda.functionArn,
  });
}

class GlobalHealthCheckStack extends cdk.Stack {
  readonly globalAlarmTopic: sns.ITopic
  constructor(scope: constructs.Construct, id: string, props: cdk.StackProps) {
    super(scope, id, props);

    this.globalAlarmTopic = new sns.Topic(this, "AlarmTopic", {
      topicName: "GlobalAlarm",
    });

    const check = new route53.CfnHealthCheck(this, "VirkailijaDomainHealthCheck", {
      healthCheckConfig: {
        type: "HTTPS",
        fullyQualifiedDomainName: config.getConfig().virkailijaHost,
        port: 443,
        resourcePath: "/organisaatio-service/actuator/health",
      },
      healthCheckTags: [{
        key: "Name",
        value: "OrganisaatioHealthCheck",
      }],
    });

    const metric = new cloudwatch.Metric({
      namespace: "AWS/Route53",
      metricName: "HealthCheckStatus",
      dimensionsMap: {
        HealthCheckId: check.attrHealthCheckId,
      },
    })
    const alarm = metric.createAlarm(this, "OrganisaatioHealthCheckAlarm", {
      alarmName: "OrganisaatioHealthCheckAlarm",
      threshold: 1,
      evaluationPeriods: 1,
      treatMissingData: cloudwatch.TreatMissingData.BREACHING,
      comparisonOperator: cloudwatch.ComparisonOperator.LESS_THAN_THRESHOLD,
    })
    alarm.addOkAction(new cloudwatch_actions.SnsAction(this.globalAlarmTopic));
    alarm.addAlarmAction(new cloudwatch_actions.SnsAction(this.globalAlarmTopic));
  }
}

type RegionalHealthCheckStackProps = cdk.StackProps & {
  globalAlarmTopicArn: string
  alarmsToSlackLambdaArn: string,
}

class RegionalHealthCheckStack extends cdk.Stack {
  constructor(scope: constructs.Construct, id: string, props: RegionalHealthCheckStackProps) {
    super(scope, id, props);
    const alarmsToSlackLambda = lambda.Function.fromFunctionArn(
      this,
      "AlarmsToSlackLambda",
      props.alarmsToSlackLambdaArn
    )
    const globalAlarmTopic = sns.Topic.fromTopicArn(
      this,
      "GlobalAlarmTopic",
      props.globalAlarmTopicArn
    );
    globalAlarmTopic.addSubscription(
      new sns_subscriptions.LambdaSubscription(alarmsToSlackLambda)
    );
  }
}