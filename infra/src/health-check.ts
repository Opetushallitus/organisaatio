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

export function createHealthCheckStacks(app: cdk.App) {
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
    regionalAlarmTopicName: healthCheckStack.regionalAlarmTopic.topicName,
  });
}

class GlobalHealthCheckStack extends cdk.Stack {
  readonly regionalAlarmTopic: sns.ITopic
  constructor(scope: constructs.Construct, id: string, props: cdk.StackProps) {
    super(scope, id, props);

    this.regionalAlarmTopic = new sns.Topic(this, "AlarmTopic", {
      topicName: "AlarmGlobalTopic",
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
    alarm.addOkAction(new cloudwatch_actions.SnsAction(this.regionalAlarmTopic));
    alarm.addAlarmAction(new cloudwatch_actions.SnsAction(this.regionalAlarmTopic));
  }
}

type RegionalHealthCheckStackProps = cdk.StackProps & {
  regionalAlarmTopicName: string
}

class RegionalHealthCheckStack extends cdk.Stack {
  constructor(scope: constructs.Construct, id: string, props: RegionalHealthCheckStackProps) {
    super(scope, id, props);

    const stack = cdk.Stack.of(this);

    const regionalAlarmTopic = sns.Topic.fromTopicArn(
      this,
      "RegionalAlarmTopic",
      `arn:aws:sns:${ROUTE53_HEALTH_CHECK_REGION}:${stack.account}:${props.regionalAlarmTopicName}`
    );
    const alarmsToSlackLambda = lambda.Function.fromFunctionArn(
      this,
      "AlarmsToSlackLambda",
      `arn:aws:lambda:${stack.region}:${stack.account}:function:alarms-to-slack`,
    );
    regionalAlarmTopic.addSubscription(
      new sns_subscriptions.LambdaSubscription(alarmsToSlackLambda)
    );
  }
}