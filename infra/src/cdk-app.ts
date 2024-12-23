import * as cdk from "aws-cdk-lib";
import * as certificatemanager from "aws-cdk-lib/aws-certificatemanager";
import * as cloudwatch from "aws-cdk-lib/aws-cloudwatch";
import * as cloudwatch_actions from "aws-cdk-lib/aws-cloudwatch-actions";
import * as constructs from "constructs";
import * as ec2 from "aws-cdk-lib/aws-ec2";
import * as ecr_assets from "aws-cdk-lib/aws-ecr-assets";
import * as ecs from "aws-cdk-lib/aws-ecs";
import * as elasticloadbalancingv2 from "aws-cdk-lib/aws-elasticloadbalancingv2";
import * as iam from "aws-cdk-lib/aws-iam";
import * as lambda from "aws-cdk-lib/aws-lambda";
import * as logs from "aws-cdk-lib/aws-logs";
import * as rds from "aws-cdk-lib/aws-rds"
import * as route53 from "aws-cdk-lib/aws-route53";
import * as route53_targets from "aws-cdk-lib/aws-route53-targets"
import * as s3 from "aws-cdk-lib/aws-s3"
import * as secretsmanager from "aws-cdk-lib/aws-secretsmanager";
import * as sns from "aws-cdk-lib/aws-sns";
import * as ssm from "aws-cdk-lib/aws-ssm";
import * as subscriptions from "aws-cdk-lib/aws-sns-subscriptions";
import * as path from "node:path";
import {getConfig, getEnvironment} from "./config";
import {createHealthCheckStacks} from "./health-check";
import {DatabaseBackupToS3} from "./DatabaseBackupToS3";

class CdkApp extends cdk.App {
  constructor(props: cdk.AppProps) {
    super(props);
    const stackProps = {
      env: {
        account: process.env.CDK_DEPLOY_TARGET_ACCOUNT,
        region: process.env.CDK_DEPLOY_TARGET_REGION,
      },
    };

    const { hostedZone } = new DnsStack(this, "DnsStack", stackProps);
    const { alarmTopic, alarmsToSlackLambda } = new AlarmStack(this, "AlarmStack", stackProps);
    const { vpc, bastion } = new VpcStack(this, "VpcStack", stackProps);
    const ecsStack = new ECSStack(this, "ECSStack", vpc, stackProps);
    const vardaRekisterointiDatabaseStack = new VardRekisterointiDatabaseStack(this, "VardaRekisterointiDatabase", vpc, ecsStack.cluster, bastion, alarmTopic, stackProps);
    const organisaatioDatabaseStack = new OrganisaatioDatabaseStack(this, "Database", vpc, ecsStack.cluster, bastion, alarmTopic, stackProps);
    createHealthCheckStacks(this, alarmsToSlackLambda)
    new ApplicationStack(this, "OrganisaatioApplication", vpc, hostedZone, alarmTopic, {
      database: organisaatioDatabaseStack.database,
      exportBucket: organisaatioDatabaseStack.exportBucket,
      ecsCluster: ecsStack.cluster,
      ...stackProps,
    });
  }
}

export class DnsStack extends cdk.Stack {
  readonly hostedZone: route53.IHostedZone
  constructor(scope: constructs.Construct, id: string, props: cdk.StackProps) {
    super(scope, id, props);

    const zoneName = ssm.StringParameter.valueFromLookup(this, "zoneName");

    this.hostedZone = new route53.HostedZone(this, "HostedZone", {
      zoneName,
    });
  }
}

class VpcStack extends cdk.Stack {
  readonly vpc: ec2.IVpc;
  readonly bastion: ec2.BastionHostLinux;
  constructor(scope: constructs.Construct, id: string, props: cdk.StackProps) {
    super(scope, id, props);
    this.vpc = this.createVpc();
    this.bastion = this.createBastion()
  }

  createVpc() {
    const outIpAddresses = this.createOutIpAddresses();
    const natProvider = ec2.NatProvider.gateway({
      eipAllocationIds: outIpAddresses.map((ip) =>
        ip.getAtt("AllocationId").toString()
      ),
    });
    const vpc = new ec2.Vpc(this, "Vpc", {
      vpcName: "vpc",
      subnetConfiguration: [
        {
          name: "Ingress",
          subnetType: ec2.SubnetType.PUBLIC,
        },
        {
          name: "Application",
          subnetType: ec2.SubnetType.PRIVATE_WITH_EGRESS,
        },
        {
          name: "Database",
          subnetType: ec2.SubnetType.PRIVATE_ISOLATED,
        },
      ],
      maxAzs: 3,
      natGateways: 3,
      natGatewayProvider: natProvider,
    });
    vpc.addGatewayEndpoint("S3Endpoint", {
      service: ec2.GatewayVpcEndpointAwsService.S3,
    });
    return vpc;
  }

  private createBastion(): ec2.BastionHostLinux {
    return new ec2.BastionHostLinux(this, "Bastion", {
      vpc: this.vpc,
      instanceName: "Bastion",
    })
  }

  private createOutIpAddresses() {
    // Ainakin Oiva näitä IP-osoitteita rajaamaan pääsyä palvelun rajapintoihin
    return ["OutIpAddress1", "OutIpAddress2", "OutIpAddress3"].map((ip) =>
      this.createIpAddress(ip)
    );
  }

  private createIpAddress(id: string) {
    return new ec2.CfnEIP(this, id, {
      tags: [{ key: "Name", value: id }],
    });
  }
}

class AlarmStack extends cdk.Stack {
  readonly alarmTopic: sns.ITopic;
  readonly alarmsToSlackLambda: lambda.IFunction;
  constructor(scope: constructs.Construct, id: string, props: cdk.StackProps) {
    super(scope, id, props);

    this.alarmsToSlackLambda = this.createAlarmsToSlackLambda();
    this.alarmTopic = this.createAlarmTopic();

    this.alarmTopic.addSubscription(
      new subscriptions.LambdaSubscription(this.alarmsToSlackLambda),
    );

    const radiatorAccountId = "905418271050"
    const radiatorReader = new iam.Role(this, "RadiatorReaderRole", {
      assumedBy: new iam.AccountPrincipal(radiatorAccountId),
      roleName: "RadiatorReader",
    })
    radiatorReader.addToPolicy(new iam.PolicyStatement({
      effect: iam.Effect.ALLOW,
      actions: ["cloudwatch:DescribeAlarms"],
      resources: ["*"],
    }))
  }

  createAlarmTopic() {
    return new sns.Topic(this, "AlarmTopic", {
      topicName: "alarm",
    });
  }

  createAlarmsToSlackLambda() {
    const alarmsToSlack = new lambda.Function(this, "AlarmsToSlack", {
      functionName: "alarms-to-slack",
      code: lambda.Code.fromAsset("../alarms-to-slack"),
      handler: "alarms-to-slack.handler",
      runtime: lambda.Runtime.NODEJS_20_X,
      architecture: lambda.Architecture.ARM_64,
      timeout: cdk.Duration.seconds(30),
    });

    // https://docs.aws.amazon.com/secretsmanager/latest/userguide/retrieving-secrets_lambda.html
    const parametersAndSecretsExtension =
      lambda.LayerVersion.fromLayerVersionArn(
        this,
        "ParametersAndSecretsLambdaExtension",
        "arn:aws:lambda:eu-west-1:015030872274:layer:AWS-Parameters-and-Secrets-Lambda-Extension-Arm64:11",
      );

    alarmsToSlack.addLayers(parametersAndSecretsExtension);
    secretsmanager.Secret.fromSecretNameV2(
      this,
      "slack-webhook",
      "slack-webhook",
    ).grantRead(alarmsToSlack);

    return alarmsToSlack;
  }
}

class ECSStack extends cdk.Stack {
  public cluster: ecs.Cluster;

  constructor(
    scope: constructs.Construct,
    id: string,
    vpc: ec2.IVpc,
    props: cdk.StackProps,
  ) {
    super(scope, id, props);

    this.cluster = new ecs.Cluster(this, "Cluster", {
      vpc,
      clusterName: "Cluster",
    });
  }
}

class OrganisaatioDatabaseStack extends cdk.Stack {
  readonly database: rds.DatabaseCluster;
  readonly exportBucket: s3.Bucket;

  constructor(
      scope: constructs.Construct,
      id: string,
      vpc: ec2.IVpc,
      ecsCluster: ecs.Cluster,
      bastion: ec2.BastionHostLinux,
      alarmTopic: sns.ITopic,
      props: cdk.StackProps
  ) {
    super(scope, id, props);

    this.exportBucket = new s3.Bucket(this, "ExportBucket", {});

    this.database = new rds.DatabaseCluster(this, "Database", {
      vpc,
      vpcSubnets: {subnetType: ec2.SubnetType.PRIVATE_ISOLATED},
      defaultDatabaseName: "organisaatio",
      engine: rds.DatabaseClusterEngine.auroraPostgres({
        version: rds.AuroraPostgresEngineVersion.VER_15_7,
      }),
      credentials: rds.Credentials.fromGeneratedSecret("organisaatio", {
        secretName: "DatabaseSecret",
      }),
      storageType: rds.DBClusterStorageType.AURORA,
      writer: rds.ClusterInstance.provisioned("writer", {
        enablePerformanceInsights: true,
        instanceType: ec2.InstanceType.of(
            ec2.InstanceClass.R6G,
            ec2.InstanceSize.XLARGE
        ),
      }),
      storageEncrypted: true,
      readers: [],
      s3ExportBuckets: [this.exportBucket],
    });
    this.database.connections.allowDefaultPortFrom(bastion);

    const backup = new DatabaseBackupToS3(this, "DatabaseBackupToS3", {
      ecsCluster: ecsCluster,
      dbCluster: this.database,
      dbName: "organisaatio",
      alarmTopic,
    });
    this.database.connections.allowDefaultPortFrom(backup);
  }
}

class VardRekisterointiDatabaseStack extends cdk.Stack {
  readonly database: rds.DatabaseCluster;

  constructor(
      scope: constructs.Construct,
      id: string,
      vpc: ec2.IVpc,
      ecsCluster: ecs.Cluster,
      bastion: ec2.BastionHostLinux,
      alarmTopic: sns.ITopic,
      props: cdk.StackProps
  ) {
    super(scope, id, props);

    this.database = new rds.DatabaseCluster(this, "Database", {
      vpc,
      vpcSubnets: {subnetType: ec2.SubnetType.PRIVATE_ISOLATED},
      defaultDatabaseName: "vardarekisterointi",
      engine: rds.DatabaseClusterEngine.auroraPostgres({
        version: rds.AuroraPostgresEngineVersion.VER_15_7,
      }),
      credentials: rds.Credentials.fromGeneratedSecret("vardarekisterointi", {
        secretName: "VardaDatabaseSecret",
      }),
      storageType: rds.DBClusterStorageType.AURORA,
      writer: rds.ClusterInstance.provisioned("writer", {
        enablePerformanceInsights: true,
        instanceType: ec2.InstanceType.of(
            ec2.InstanceClass.T4G,
            ec2.InstanceSize.SMALL
        ),
      }),
      storageEncrypted: true,
      readers: [],
    });
    this.database.connections.allowDefaultPortFrom(bastion);

    const backup = new DatabaseBackupToS3(this, "DatabaseBackupToS3", {
      ecsCluster: ecsCluster,
      dbCluster: this.database,
      dbName: "vardarekisterointi",
      alarmTopic,
    });
    this.database.connections.allowDefaultPortFrom(backup);
  }
}

type ApplicationStackProps = cdk.StackProps & {
  database: rds.DatabaseCluster
  ecsCluster: ecs.Cluster
  exportBucket: s3.Bucket
}

class ApplicationStack extends cdk.Stack {
  constructor(
      scope: constructs.Construct,
      id: string,
      vpc: ec2.IVpc,
      hostedZone: route53.IHostedZone,
      alarmTopic: sns.ITopic,
      props: ApplicationStackProps,
  ) {
    super(scope, id, props);

    const logGroup = new logs.LogGroup(this, "AppLogGroup", {
      logGroupName: "Organisaatio/organisaatio",
      retention: logs.RetentionDays.INFINITE,
    });

    const dockerImage = new ecr_assets.DockerImageAsset(this, "AppImage", {
      directory: path.join(__dirname, "../../"),
      file: "Dockerfile",
      platform: ecr_assets.Platform.LINUX_ARM64,
      exclude: ['infra/cdk.out'],
    });

    const taskDefinition = new ecs.FargateTaskDefinition(
        this,
        "TaskDefinition",
        {
          cpu: 4096,
          memoryLimitMiB: 12288,
          runtimePlatform: {
            operatingSystemFamily: ecs.OperatingSystemFamily.LINUX,
            cpuArchitecture: ecs.CpuArchitecture.ARM64,
          },
        });

    const appPort = 8080;
    taskDefinition.addContainer("AppContainer", {
      image: ecs.ContainerImage.fromDockerImageAsset(dockerImage),
      logging: new ecs.AwsLogDriver({ logGroup, streamPrefix: "app" }),
      environment: {
        ENV: getEnvironment(),
        postgresql_host: props.database.clusterEndpoint.hostname,
        postgresql_port: props.database.clusterEndpoint.port.toString(),
        postgresql_db: "organisaatio",
        aws_region: this.region,
        export_bucket_name: props.exportBucket.bucketName,
      },
      secrets: {
        postgresql_username: ecs.Secret.fromSecretsManager(
            props.database.secret!,
            "username"
        ),
        postgresql_password: ecs.Secret.fromSecretsManager(
            props.database.secret!,
            "password"
        ),
        lampi_role_arn: this.ssmString("LampiRoleArn"),
        lampi_external_id: this.ssmSecret("LampiExternalId"),
        organisaatio_service_username: this.ssmSecret("PalvelukayttajaUsername"),
        organisaatio_service_password: this.ssmSecret("PalvelukayttajaPassword"),
        rajapinnat_ytj_asiakastunnus: this.ssmSecret("YtjAsiakastunnus"),
        rajapinnat_ytj_avain: this.ssmSecret("YtjAvain"),
        organisaatio_service_username_to_koodisto: this.ssmSecret("KoodistoUsername"),
        organisaatio_service_password_to_koodisto: this.ssmSecret("KoodistoPassword"),
        oiva_baseurl: this.ssmSecret("OivaBaseurl"),
        oiva_username: this.ssmSecret("OivaUsername"),
        oiva_password: this.ssmSecret("OivaPassword"),
      },
      portMappings: [
        {
          name: "organisaatio",
          containerPort: appPort,
          appProtocol: ecs.AppProtocol.http,
        },
      ],
    });

    props.exportBucket.grantReadWrite(taskDefinition.taskRole);
    taskDefinition.addToTaskRolePolicy(
      new iam.PolicyStatement({
        actions: ["sts:AssumeRole"],
        resources: [
          ssm.StringParameter.valueFromLookup(
            this,
            "/organisaatio/LampiRoleArn"
          ),
        ],
      })
    );

    const conf = getConfig();
    const service = new ecs.FargateService(this, "Service", {
      cluster: props.ecsCluster,
      taskDefinition,
      desiredCount: conf.minCapacity,
      minHealthyPercent: 100,
      maxHealthyPercent: 200,
      vpcSubnets: { subnetType: ec2.SubnetType.PRIVATE_WITH_EGRESS },
      healthCheckGracePeriod: cdk.Duration.minutes(5),
    });
    const scaling = service.autoScaleTaskCount({
      minCapacity: conf.minCapacity,
      maxCapacity: conf.maxCapacity,
    });

    scaling.scaleOnMetric("ServiceScaling", {
      metric: service.metricCpuUtilization(),
      scalingSteps: [
        { upper: 15, change: -1 },
        { lower: 50, change: +1 },
        { lower: 65, change: +2 },
        { lower: 80, change: +3 },
      ],
    });
    service.connections.allowToDefaultPort(props.database);

    const alb = new elasticloadbalancingv2.ApplicationLoadBalancer(
        this,
        "LoadBalancer",
        {
          vpc,
          internetFacing: true,
        }
    );

    const albHostname = `organisaatio.${hostedZone.zoneName}`;

    new route53.ARecord(this, "ALBARecord", {
      zone: hostedZone,
      recordName: albHostname,
      target: route53.RecordTarget.fromAlias(
          new route53_targets.LoadBalancerTarget(alb)
      ),
    });

    const albCertificate = new certificatemanager.Certificate(
        this,
        "AlbCertificate",
        {
          domainName: albHostname,
          validation:
              certificatemanager.CertificateValidation.fromDns(hostedZone),
        }
    );

    const listener = alb.addListener("Listener", {
      protocol: elasticloadbalancingv2.ApplicationProtocol.HTTPS,
      port: 443,
      open: true,
      certificates: [albCertificate],
    });

    listener.addTargets("ServiceTarget", {
      port: appPort,
      targets: [service],
      healthCheck: {
        enabled: true,
        interval: cdk.Duration.seconds(10),
        path: "/organisaatio-service/actuator/health",
        port: appPort.toString(),
      },
    });

    this.exportFailureAlarm(logGroup, alarmTopic);
    this.oivaIntegrationAlarm(logGroup, alarmTopic);
  }

  exportFailureAlarm(logGroup: logs.LogGroup, alarmTopic: sns.ITopic) {
    this.alarmIfExpectedLogLineIsMissing(
      "OrganisaatioExportTask",
      logGroup,
      alarmTopic,
      logs.FilterPattern.literal('"Organisaatio export task completed"'),
    );
  }

  private oivaIntegrationAlarm(logGroup: logs.LogGroup, alarmTopic: sns.ITopic) {
    this.alarmIfExpectedLogLineIsMissing(
      "OrganisaatioFetchKoulutusluvatTask",
      logGroup,
      alarmTopic,
      logs.FilterPattern.literal('"Completed FetchKoulutusluvatTask"'),
    );
  }

  private alarmIfExpectedLogLineIsMissing(id: string, logGroup: logs.LogGroup, alarmTopic: sns.ITopic, filterPattern: logs.IFilterPattern) {
    const metricFilter = logGroup.addMetricFilter(
      `${id}SuccessMetricFilter`,
      {
        filterPattern,
        metricName: `${id}Success`,
        metricNamespace: "Organisaatio",
        metricValue: "1",
      }
    );
    const alarm = new cloudwatch.Alarm(this, `${id}FailingAlarm`, {
      alarmName: id,
      metric: metricFilter.metric({
        statistic: "Sum",
        period: cdk.Duration.hours(1),
      }),
      comparisonOperator:
      cloudwatch.ComparisonOperator.LESS_THAN_OR_EQUAL_TO_THRESHOLD,
      threshold: 0,
      evaluationPeriods: 8,
      treatMissingData: cloudwatch.TreatMissingData.BREACHING,
    });
    alarm.addOkAction(new cloudwatch_actions.SnsAction(alarmTopic));
    alarm.addAlarmAction(new cloudwatch_actions.SnsAction(alarmTopic));
  }

  ssmString(name: string): ecs.Secret {
    return ecs.Secret.fromSsmParameter(
      ssm.StringParameter.fromStringParameterName(
        this,
        `Param${name}`,
        `/organisaatio/${name}`
      )
    );
  }
  ssmSecret(name: string): ecs.Secret {
    return ecs.Secret.fromSsmParameter(
        ssm.StringParameter.fromSecureStringParameterAttributes(
            this,
            `Param${name}`,
            { parameterName: `/organisaatio/${name}` }
        )
    );
  }
}

const app = new CdkApp({});
app.synth();
