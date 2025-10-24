import * as cdk from "aws-cdk-lib";
import * as cloudwatch from "aws-cdk-lib/aws-cloudwatch";
import * as cloudwatch_actions from "aws-cdk-lib/aws-cloudwatch-actions";
import * as constructs from "constructs";
import * as elasticloadbalancingv2 from "aws-cdk-lib/aws-elasticloadbalancingv2";
import * as sns from "aws-cdk-lib/aws-sns";

type ResponseAlarmsProps = {
  prefix: string;
  alarmTopic: sns.ITopic;
  alb: elasticloadbalancingv2.IApplicationLoadBalancer;
  target: elasticloadbalancingv2.IApplicationTargetGroup;
  albThreshold: number;
  targetThreshold: number;
};

export class ResponseAlarms extends constructs.Construct {
  readonly props: ResponseAlarmsProps;

  constructor(
    scope: constructs.Construct,
    id: string,
    props: ResponseAlarmsProps,
  ) {
    super(scope, id);
    this.props = props;

    this.createAlarm("LoadBalancer5XXAlarm", {
      alarmName: `${this.props.prefix}LoadBalancer5XXAlarm`,
      metric: this.props.alb.metrics.httpCodeElb(
        elasticloadbalancingv2.HttpCodeElb.ELB_5XX_COUNT,
        {
          statistic: "Sum",
          period: cdk.Duration.minutes(5),
        },
      ),
      comparisonOperator:
        cloudwatch.ComparisonOperator.GREATER_THAN_OR_EQUAL_TO_THRESHOLD,
      threshold: this.props.albThreshold,
      evaluationPeriods: 2,
      datapointsToAlarm: 1,
      treatMissingData: cloudwatch.TreatMissingData.NOT_BREACHING,
    });
    this.createAlarm("Target5XXAlarm", {
      alarmName: `${this.props.prefix}Target5XXAlarm`,
      metric: this.props.target.metrics.httpCodeTarget(
        elasticloadbalancingv2.HttpCodeTarget.TARGET_5XX_COUNT,
        {
          statistic: "Sum",
          period: cdk.Duration.minutes(5),
        },
      ),
      comparisonOperator:
        cloudwatch.ComparisonOperator.GREATER_THAN_OR_EQUAL_TO_THRESHOLD,
      threshold: this.props.targetThreshold,
      evaluationPeriods: 2,
      datapointsToAlarm: 1,
      treatMissingData: cloudwatch.TreatMissingData.NOT_BREACHING,
    });
  }

  createAlarm(id: string, alarmProps: cloudwatch.AlarmProps): cloudwatch.Alarm {
    const alarm = new cloudwatch.Alarm(this, id, alarmProps);
    alarm.addOkAction(new cloudwatch_actions.SnsAction(this.props.alarmTopic));
    alarm.addAlarmAction(
      new cloudwatch_actions.SnsAction(this.props.alarmTopic),
    );
    return alarm;
  }
}
