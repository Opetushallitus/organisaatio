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
import * as rds from "aws-cdk-lib/aws-rds";
import * as route53 from "aws-cdk-lib/aws-route53";
import * as route53_targets from "aws-cdk-lib/aws-route53-targets";
import * as s3 from "aws-cdk-lib/aws-s3";
import * as secretsmanager from "aws-cdk-lib/aws-secretsmanager";
import * as sns from "aws-cdk-lib/aws-sns";
import * as ssm from "aws-cdk-lib/aws-ssm";
import * as subscriptions from "aws-cdk-lib/aws-sns-subscriptions";
import * as kms from "aws-cdk-lib/aws-kms";
import * as path from "node:path";
import { getConfig, getEnvironment } from "./config";
import { createHealthCheckStacks } from "./health-check";
import { DatabaseBackupToS3 } from "./DatabaseBackupToS3";
import { DatantuontiStack } from "./datantuonti";

const config = getConfig();

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
    const { alarmTopic, alarmsToSlackLambda } = new AlarmStack(
      this,
      "AlarmStack",
      stackProps,
    );
    const { vpc, bastion } = new VpcStack(this, "VpcStack", stackProps);
    const ecsStack = new ECSStack(this, "ECSStack", vpc, stackProps);
    const vardaRekisterointiDatabaseStack = new VardRekisterointiDatabaseStack(
      this,
      "VardaRekisterointiDatabase",
      vpc,
      ecsStack.cluster,
      bastion,
      alarmTopic,
      stackProps,
    );
    const datantuontiStack = new DatantuontiStack(
      this,
      "OrganisaatioDatantuonti",
      stackProps,
    );
    const organisaatioDatabaseStack = new OrganisaatioDatabaseStack(
      this,
      "Database",
      vpc,
      ecsStack.cluster,
      bastion,
      alarmTopic,
      datantuontiStack.exportBucket,
      datantuontiStack.s3ImportRole,
      stackProps,
    );
    createHealthCheckStacks(this, alarmsToSlackLambda, [
      {
        name: "Organisaatio",
        url: new URL(
          `https://virkailija.${config.opintopolkuHost}/organisaatio-service/actuator/health`,
        ),
      },
      {
        name: "VardaRekisterointi",
        url: new URL(
          `https://virkailija.${config.opintopolkuHost}/varda-rekisterointi/actuator/health`,
        ),
      },
      {
        name: "JotpaRekisterointi",
        url: new URL(
          `https://rekisterointi.${config.opintopolkuHost}/actuator/health`,
        ),
      },
    ]);
    new VardaRekisterointiApplicationStack(
      this,
      "VardaRekisterointiApplication",
      vpc,
      hostedZone,
      {
        database: vardaRekisterointiDatabaseStack.database,
        ecsCluster: ecsStack.cluster,
        ...stackProps,
      },
    );
    new RekisterointiApplicationStack(
      this,
      "RekisterointiApplication",
      vpc,
      hostedZone,
      { ecsCluster: ecsStack.cluster, ...stackProps },
    );
    new OrganisaatioApplicationStack(
      this,
      "OrganisaatioApplication",
      vpc,
      hostedZone,
      alarmTopic,
      {
        database: organisaatioDatabaseStack.database,
        exportBucket: organisaatioDatabaseStack.exportBucket,
        ecsCluster: ecsStack.cluster,
        datantuontiExportBucket: datantuontiStack.exportBucket,
        datantuontiEncryptionKey: datantuontiStack.encryptionKey,
        ...stackProps,
      },
    );
  }
}

export class DnsStack extends cdk.Stack {
  readonly hostedZone: route53.IHostedZone;
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
    this.bastion = this.createBastion();
  }

  createVpc() {
    const outIpAddresses = this.createOutIpAddresses();
    const natProvider = ec2.NatProvider.gateway({
      eipAllocationIds: outIpAddresses.map((ip) =>
        ip.getAtt("AllocationId").toString(),
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
    });
  }

  private createOutIpAddresses() {
    // Ainakin Oiva näitä IP-osoitteita rajaamaan pääsyä palvelun rajapintoihin
    return ["OutIpAddress1", "OutIpAddress2", "OutIpAddress3"].map((ip) =>
      this.createIpAddress(ip),
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

    const radiatorAccountId = "905418271050";
    const radiatorReader = new iam.Role(this, "RadiatorReaderRole", {
      assumedBy: new iam.AccountPrincipal(radiatorAccountId),
      roleName: "RadiatorReader",
    });
    radiatorReader.addToPolicy(
      new iam.PolicyStatement({
        effect: iam.Effect.ALLOW,
        actions: ["cloudwatch:DescribeAlarms"],
        resources: ["*"],
      }),
    );
    this.exportValue(this.alarmTopic.topicArn);
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
    datantuontiExportBucket: s3.Bucket,
    datantuontiS3ImportRole: iam.Role,
    props: cdk.StackProps,
  ) {
    super(scope, id, props);

    this.exportBucket = new s3.Bucket(this, "ExportBucket", {});
    this.database = new rds.DatabaseCluster(this, "Database", {
      vpc,
      vpcSubnets: { subnetType: ec2.SubnetType.PRIVATE_ISOLATED },
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
          ec2.InstanceSize.XLARGE,
        ),
      }),
      storageEncrypted: true,
      readers: [],
      s3ExportBuckets: [this.exportBucket, datantuontiExportBucket],
      s3ImportRole: datantuontiS3ImportRole,
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
    props: cdk.StackProps,
  ) {
    super(scope, id, props);

    this.database = new rds.DatabaseCluster(this, "Database", {
      vpc,
      vpcSubnets: { subnetType: ec2.SubnetType.PRIVATE_ISOLATED },
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
          ec2.InstanceSize.MEDIUM,
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

type OrganisaatioApplicationStackProps = cdk.StackProps & {
  database: rds.DatabaseCluster;
  ecsCluster: ecs.Cluster;
  exportBucket: s3.Bucket;
  datantuontiExportBucket: s3.Bucket;
  datantuontiEncryptionKey: kms.Key;
};

class OrganisaatioApplicationStack extends cdk.Stack {
  constructor(
    scope: constructs.Construct,
    id: string,
    vpc: ec2.IVpc,
    hostedZone: route53.IHostedZone,
    alarmTopic: sns.ITopic,
    props: OrganisaatioApplicationStackProps,
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
      exclude: ["infra/cdk.out"],
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
      },
    );

    const lampiProperties: ecs.ContainerDefinitionProps["environment"] =
      config.lampiExport
        ? {
            "organisaatio.tasks.export.enabled":
              config.lampiExport.enabled.toString(),
            "organisaatio.tasks.export.bucket-name":
              props.exportBucket.bucketName,
            "organisaatio.tasks.export.lampi-bucket-name":
              config.lampiExport.bucketName,
          }
        : {};
    const lampiSecrets: ecs.ContainerDefinitionProps["secrets"] =
      config.lampiExport
        ? {
            "organisaatio.tasks.export.lampi-role-arn":
              this.ssmString("LampiRoleArn"),
            "organisaatio.tasks.export.lampi-external-id":
              this.ssmSecret("LampiExternalId"),
          }
        : {};

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
        "organisaatio.tasks.datantuonti.export.bucket-name":
          props.datantuontiExportBucket.bucketName,
        "organisaatio.tasks.datantuonti.export.encryption-key-id":
          props.datantuontiEncryptionKey.keyId,
        "organisaatio.tasks.datantuonti.export.encryption-key-arn":
          props.datantuontiEncryptionKey.keyArn,
        "organisaatio.tasks.datantuonti.import.enabled": `${config.features["organisaatio.tasks.datantuonti.import.enabled"]}`,
        ...lampiProperties,
        "otuva.jwt.issuer-uri": config.oauthJwtIssuerUri,
        "oppijanumerorekisteri.baseurl": config.oppijanumerorekisteriBaseUrl,
      },
      secrets: {
        postgresql_username: ecs.Secret.fromSecretsManager(
          props.database.secret!,
          "username",
        ),
        postgresql_password: ecs.Secret.fromSecretsManager(
          props.database.secret!,
          "password",
        ),
        ...lampiSecrets,
        "organisaatio.palvelukayttaja.client_id": this.ssmSecret(
          "PalvelukayttajaClientId",
        ),
        "organisaatio.palvelukayttaja.client_secret": this.ssmSecret(
          "PalvelukayttajaClientSecret",
        ),
        organisaatio_service_username: this.ssmSecret(
          "PalvelukayttajaUsername",
        ),
        organisaatio_service_password: this.ssmSecret(
          "PalvelukayttajaPassword",
        ),
        rajapinnat_ytj_asiakastunnus: this.ssmSecret("YtjAsiakastunnus"),
        rajapinnat_ytj_avain: this.ssmSecret("YtjAvain"),
        ytjpaivitysloki_service_email: this.ssmSecret(
          "YtjpaivityslokiServiceEmail",
        ),
        oiva_baseurl: this.ssmSecret("OivaBaseurl"),
        oiva_username: this.ssmSecret("OivaUsername"),
        oiva_password: this.ssmSecret("OivaPassword"),
        "organisaatio.tasks.datantuonti.import.bucket.name": this.ssmString(
          "organisaatio.tasks.datantuonti.import.bucket.name",
          "",
        ),
      },
      portMappings: [
        {
          name: "organisaatio",
          containerPort: appPort,
          appProtocol: ecs.AppProtocol.http,
        },
      ],
    });

    props.datantuontiEncryptionKey.grantEncrypt(taskDefinition.taskRole);
    props.datantuontiExportBucket.grantReadWrite(taskDefinition.taskRole);
    props.exportBucket.grantReadWrite(taskDefinition.taskRole);
    if (config.lampiExport) {
      taskDefinition.addToTaskRolePolicy(
        new iam.PolicyStatement({
          actions: ["sts:AssumeRole"],
          resources: [
            ssm.StringParameter.valueFromLookup(
              this,
              "/organisaatio/LampiRoleArn",
            ),
          ],
        }),
      );
    }
    const importBucketName = ssm.StringParameter.valueFromLookup(
      this,
      "organisaatio.tasks.datantuonti.import.bucket.name",
    );
    const decryptionKeyArn = ssm.StringParameter.valueFromLookup(
      this,
      "organisaatio.tasks.datantuonti.import.bucket.decryption-key-arn",
    );
    taskDefinition.addToTaskRolePolicy(
      new iam.PolicyStatement({
        actions: ["s3:GetObject", "s3:ListBucket"],
        resources: [
          `arn:aws:s3:::${importBucketName}`,
          `arn:aws:s3:::${importBucketName}/*`,
        ],
      }),
    );
    taskDefinition.addToTaskRolePolicy(
      new iam.PolicyStatement({
        actions: ["kms:Decrypt"],
        resources: [decryptionKeyArn],
      }),
    );

    const service = new ecs.FargateService(this, "Service", {
      cluster: props.ecsCluster,
      taskDefinition,
      desiredCount: config.minCapacity,
      minHealthyPercent: 100,
      maxHealthyPercent: 200,
      circuitBreaker: {
        enable: true,
      },
      vpcSubnets: { subnetType: ec2.SubnetType.PRIVATE_WITH_EGRESS },
      healthCheckGracePeriod: cdk.Duration.minutes(5),
    });
    const scaling = service.autoScaleTaskCount({
      minCapacity: config.minCapacity,
      maxCapacity: config.maxCapacity,
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
      },
    );

    const albHostname = `organisaatio.${hostedZone.zoneName}`;

    new route53.ARecord(this, "ALBARecord", {
      zone: hostedZone,
      recordName: albHostname,
      target: route53.RecordTarget.fromAlias(
        new route53_targets.LoadBalancerTarget(alb),
      ),
    });

    const albCertificate = new certificatemanager.Certificate(
      this,
      "AlbCertificate",
      {
        domainName: albHostname,
        validation:
          certificatemanager.CertificateValidation.fromDns(hostedZone),
      },
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

    if (config.lampiExport) {
      this.exportFailureAlarm(logGroup, alarmTopic);
    }
    this.datantuontiExportFailureAlarm(logGroup, alarmTopic);
    this.organisaatioUpdateFailureAlarm(logGroup, alarmTopic);
    this.oivaIntegrationAlarm(logGroup, alarmTopic);

    if (config.features["organisaatio.tasks.datantuonti.import.enabled"]) {
      this.datantuontiImportFailureAlarm(logGroup, alarmTopic);
    }
  }

  exportFailureAlarm(logGroup: logs.LogGroup, alarmTopic: sns.ITopic) {
    this.alarmIfExpectedLogLineIsMissing(
      "OrganisaatioExportTask",
      logGroup,
      alarmTopic,
      logs.FilterPattern.literal('"Organisaatio export task completed"'),
    );
  }

  datantuontiExportFailureAlarm(
    logGroup: logs.LogGroup,
    alarmTopic: sns.ITopic,
  ) {
    this.alarmIfExpectedLogLineIsMissing(
      "DatantuontiExportTask",
      logGroup,
      alarmTopic,
      logs.FilterPattern.literal(
        '"Organisaatio datantuonti export task completed"',
      ),
    );
  }

  datantuontiImportFailureAlarm(
    logGroup: logs.LogGroup,
    alarmTopic: sns.ITopic,
  ) {
    this.alarmIfExpectedLogLineIsMissing(
      "DatantuontiImportTask",
      logGroup,
      alarmTopic,
      logs.FilterPattern.literal(
        '"Organisaatio datantuonti import task completed"',
      ),
      cdk.Duration.hours(25),
      1,
    );
  }

  organisaatioUpdateFailureAlarm(
    logGroup: logs.LogGroup,
    alarmTopic: sns.ITopic,
  ) {
    this.alarmIfExpectedLogLineIsMissing(
      "OrganisaatioUpdateTask",
      logGroup,
      alarmTopic,
      logs.FilterPattern.literal('"Organisaatio update task completed"'),
      cdk.Duration.hours(25),
      1,
    );
  }

  private oivaIntegrationAlarm(
    logGroup: logs.LogGroup,
    alarmTopic: sns.ITopic,
  ) {
    this.alarmIfExpectedLogLineIsMissing(
      "OrganisaatioFetchKoulutusluvatTask",
      logGroup,
      alarmTopic,
      logs.FilterPattern.literal('"Completed FetchKoulutusluvatTask"'),
    );
  }

  private alarmIfExpectedLogLineIsMissing(
    id: string,
    logGroup: logs.LogGroup,
    alarmTopic: sns.ITopic,
    filterPattern: logs.IFilterPattern,
    period: cdk.Duration = cdk.Duration.hours(1),
    evaluationPeriods: number = 8,
  ) {
    const metricFilter = logGroup.addMetricFilter(`${id}SuccessMetricFilter`, {
      filterPattern,
      metricName: `${id}Success`,
      metricNamespace: "Organisaatio",
      metricValue: "1",
    });
    const alarm = new cloudwatch.Alarm(this, `${id}FailingAlarm`, {
      alarmName: id,
      metric: metricFilter.metric({
        statistic: "Sum",
        period,
      }),
      comparisonOperator:
        cloudwatch.ComparisonOperator.LESS_THAN_OR_EQUAL_TO_THRESHOLD,
      threshold: 0,
      evaluationPeriods,
      treatMissingData: cloudwatch.TreatMissingData.BREACHING,
    });
    alarm.addOkAction(new cloudwatch_actions.SnsAction(alarmTopic));
    alarm.addAlarmAction(new cloudwatch_actions.SnsAction(alarmTopic));
  }

  ssmString(name: string, prefix: string = "/organisaatio/"): ecs.Secret {
    return ecs.Secret.fromSsmParameter(
      ssm.StringParameter.fromStringParameterName(
        this,
        `Param${name}`,
        `${prefix}${name}`,
      ),
    );
  }
  ssmSecret(name: string): ecs.Secret {
    return ecs.Secret.fromSsmParameter(
      ssm.StringParameter.fromSecureStringParameterAttributes(
        this,
        `Param${name}`,
        { parameterName: `/organisaatio/${name}` },
      ),
    );
  }
}

type VardaRekisterointiApplicationStackProps = cdk.StackProps & {
  database: rds.DatabaseCluster;
  ecsCluster: ecs.Cluster;
};

class VardaRekisterointiApplicationStack extends cdk.Stack {
  constructor(
    scope: constructs.Construct,
    id: string,
    vpc: ec2.IVpc,
    hostedZone: route53.IHostedZone,
    props: VardaRekisterointiApplicationStackProps,
  ) {
    super(scope, id, props);

    const logGroup = new logs.LogGroup(this, "AppLogGroup", {
      logGroupName: "Organisaatio/varda-rekisterointi",
      retention: logs.RetentionDays.INFINITE,
    });

    const dockerImage = new ecr_assets.DockerImageAsset(this, "AppImage", {
      directory: path.join(__dirname, "../../varda-rekisterointi"),
      file: "Dockerfile",
      platform: ecr_assets.Platform.LINUX_ARM64,
    });

    const taskDefinition = new ecs.FargateTaskDefinition(
      this,
      "TaskDefinition",
      {
        cpu: 512,
        memoryLimitMiB: 2048,
        runtimePlatform: {
          operatingSystemFamily: ecs.OperatingSystemFamily.LINUX,
          cpuArchitecture: ecs.CpuArchitecture.ARM64,
        },
      },
    );

    const appPort = 8080;
    taskDefinition.addContainer("AppContainer", {
      image: ecs.ContainerImage.fromDockerImageAsset(dockerImage),
      logging: new ecs.AwsLogDriver({ logGroup, streamPrefix: "app" }),
      environment: {
        ENV: getEnvironment(),
        postgresql_host: props.database.clusterEndpoint.hostname,
        postgresql_port: props.database.clusterEndpoint.port.toString(),
        postgresql_db: "vardarekisterointi",
        aws_region: this.region,
        "otuva.jwt.issuer-uri": config.oauthJwtIssuerUri,
      },
      secrets: {
        postgresql_username: ecs.Secret.fromSecretsManager(
          props.database.secret!,
          "username",
        ),
        postgresql_password: ecs.Secret.fromSecretsManager(
          props.database.secret!,
          "password",
        ),
        palvelukayttaja_username: this.ssmSecret("PalvelukayttajaUsername"),
        palvelukayttaja_password: this.ssmSecret("PalvelukayttajaPassword"),
        varda_rekisterointi_palvelukayttaja_client_id: this.ssmSecret(
          "PalvelukayttajaClientId",
        ),
        varda_rekisterointi_palvelukayttaja_client_secret: this.ssmSecret(
          "PalvelukayttajaClientSecret",
        ),
        varda_rekisterointi_valtuudet_client_id:
          this.ssmSecret("ValtuudetClientId"),
        varda_rekisterointi_valtuudet_api_key:
          this.ssmSecret("ValtuudetApiKey"),
        varda_rekisterointi_valtuudet_oauth_password: this.ssmSecret(
          "ValtuudetOauthPassword",
        ),
        varda_rekisterointi_rekisterointi_ui_username: this.ssmSecret(
          "RekisterointiUiUsername",
        ),
        varda_rekisterointi_rekisterointi_ui_password: this.ssmSecret(
          "RekisterointiUiPassword",
        ),
      },
      portMappings: [
        {
          name: "vardareisterointi",
          containerPort: appPort,
          appProtocol: ecs.AppProtocol.http,
        },
      ],
    });

    const service = new ecs.FargateService(this, "Service", {
      cluster: props.ecsCluster,
      taskDefinition,
      desiredCount: config.vardaRekisterointiCapacity,
      minHealthyPercent: 100,
      maxHealthyPercent: 200,
      circuitBreaker: {
        enable: true,
      },
      vpcSubnets: { subnetType: ec2.SubnetType.PRIVATE_WITH_EGRESS },
      healthCheckGracePeriod: cdk.Duration.minutes(5),
    });
    service.connections.allowToDefaultPort(props.database);

    const alb = new elasticloadbalancingv2.ApplicationLoadBalancer(
      this,
      "LoadBalancer",
      {
        vpc,
        internetFacing: true,
      },
    );

    const albHostname = `vardarekisterointi.${hostedZone.zoneName}`;

    new route53.ARecord(this, "ALBARecord", {
      zone: hostedZone,
      recordName: albHostname,
      target: route53.RecordTarget.fromAlias(
        new route53_targets.LoadBalancerTarget(alb),
      ),
    });

    const albCertificate = new certificatemanager.Certificate(
      this,
      "AlbCertificate",
      {
        domainName: albHostname,
        validation:
          certificatemanager.CertificateValidation.fromDns(hostedZone),
      },
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
        path: "/varda-rekisterointi/actuator/health",
        port: appPort.toString(),
      },
    });
  }

  ssmSecret(name: string): ecs.Secret {
    return ecs.Secret.fromSsmParameter(
      ssm.StringParameter.fromSecureStringParameterAttributes(
        this,
        `Param${name}`,
        { parameterName: `/vardarekisterointi/${name}` },
      ),
    );
  }
}

type RekisterointiApplicationStackProps = cdk.StackProps & {
  ecsCluster: ecs.Cluster;
};

class RekisterointiApplicationStack extends cdk.Stack {
  constructor(
    scope: constructs.Construct,
    id: string,
    vpc: ec2.IVpc,
    hostedZone: route53.IHostedZone,
    props: RekisterointiApplicationStackProps,
  ) {
    super(scope, id, props);

    const logGroup = new logs.LogGroup(this, "AppLogGroup", {
      logGroupName: "Organisaatio/rekisterointi",
      retention: logs.RetentionDays.INFINITE,
    });

    const dockerImage = new ecr_assets.DockerImageAsset(this, "AppImage", {
      directory: path.join(__dirname, "../../rekisterointi"),
      file: "Dockerfile",
      platform: ecr_assets.Platform.LINUX_ARM64,
    });

    const taskDefinition = new ecs.FargateTaskDefinition(
      this,
      "TaskDefinition",
      {
        cpu: 512,
        memoryLimitMiB: 2048,
        runtimePlatform: {
          operatingSystemFamily: ecs.OperatingSystemFamily.LINUX,
          cpuArchitecture: ecs.CpuArchitecture.ARM64,
        },
      },
    );

    const appPort = 8080;
    taskDefinition.addContainer("AppContainer", {
      image: ecs.ContainerImage.fromDockerImageAsset(dockerImage),
      logging: new ecs.AwsLogDriver({ logGroup, streamPrefix: "app" }),
      environment: {
        ENV: getEnvironment(),
        aws_region: this.region,
        "otuva.jwt.issuer-uri": config.oauthJwtIssuerUri,
      },
      secrets: {
        varda_rekisterointi_service_username: this.ssmSecret(
          "PalvelukayttajaUsername",
        ),
        varda_rekisterointi_service_password: this.ssmSecret(
          "PalvelukayttajaPassword",
        ),
        varda_rekisterointi_palvelukayttaja_client_id: this.ssmSecret(
          "PalvelukayttajaClientId",
        ),
        varda_rekisterointi_palvelukayttaja_client_secret: this.ssmSecret(
          "PalvelukayttajaClientSecret",
        ),
        varda_rekisterointi_valtuudet_client_id:
          this.ssmSecret("ValtuudetClientId"),
        varda_rekisterointi_valtuudet_api_key:
          this.ssmSecret("ValtuudetApiKey"),
        varda_rekisterointi_valtuudet_oauth_password: this.ssmSecret(
          "ValtuudetOauthPassword",
        ),
        varda_rekisterointi_rekisterointi_ui_username: this.ssmSecret(
          "RekisterointiUiUsername",
        ),
        varda_rekisterointi_rekisterointi_ui_password: this.ssmSecret(
          "RekisterointiUiPassword",
        ),
      },
      portMappings: [
        {
          name: "rekisterointi",
          containerPort: appPort,
          appProtocol: ecs.AppProtocol.http,
        },
      ],
    });

    const service = new ecs.FargateService(this, "Service", {
      cluster: props.ecsCluster,
      taskDefinition,
      desiredCount: 1,
      minHealthyPercent: 100,
      maxHealthyPercent: 200,
      circuitBreaker: { enable: true },
      vpcSubnets: { subnetType: ec2.SubnetType.PRIVATE_WITH_EGRESS },
      healthCheckGracePeriod: cdk.Duration.minutes(5),
    });

    const alb = new elasticloadbalancingv2.ApplicationLoadBalancer(
      this,
      "LoadBalancer",
      {
        vpc,
        internetFacing: true,
      },
    );

    const albHostname = `rekisterointi.${hostedZone.zoneName}`;

    new route53.ARecord(this, "ALBARecord", {
      zone: hostedZone,
      recordName: albHostname,
      target: route53.RecordTarget.fromAlias(
        new route53_targets.LoadBalancerTarget(alb),
      ),
    });

    const albCertificate = new certificatemanager.Certificate(
      this,
      "AlbCertificate",
      {
        domainName: albHostname,
        validation:
          certificatemanager.CertificateValidation.fromDns(hostedZone),
      },
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
        path: "/actuator/health",
        port: appPort.toString(),
      },
    });
  }

  ssmSecret(name: string): ecs.Secret {
    return ecs.Secret.fromSsmParameter(
      ssm.StringParameter.fromSecureStringParameterAttributes(
        this,
        `Param${name}`,
        { parameterName: `/vardarekisterointi/${name}` },
      ),
    );
  }
}

const app = new CdkApp({});
app.synth();
