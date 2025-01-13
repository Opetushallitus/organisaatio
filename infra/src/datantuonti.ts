import * as cdk from "aws-cdk-lib";
import * as kms from "aws-cdk-lib/aws-kms";
import * as ssm from "aws-cdk-lib/aws-ssm";
import * as iam from "aws-cdk-lib/aws-iam";
import * as s3 from "aws-cdk-lib/aws-s3";
import * as constructs from "constructs";

export class DatantuontiStack extends cdk.Stack {
  readonly exportBucket: s3.Bucket;
  readonly encryptionKey: kms.Key;
  readonly s3ImportRole: iam.Role;
  readonly importPolicy: iam.Policy;

  constructor(scope: constructs.Construct, id: string, props: cdk.StackProps) {
    super(scope, id, props);

    const _export = new Export(this, "Export");
    this.exportBucket = _export.bucket;
    this.encryptionKey = _export.encryptionKey;

    const _import = new Import(this, "Import");
    this.s3ImportRole = _import.role;
    this.importPolicy = _import.policy;
  }
}

class Export extends constructs.Construct {
  readonly bucket: s3.Bucket;
  readonly encryptionKey: kms.Key;

  constructor(scope: constructs.Construct, id: string) {
    super(scope, id);

    const targetAccountPrincipal = this.createTargetAccountPrincipal();
    this.bucket = this.createExportBucket(targetAccountPrincipal);
    this.encryptionKey = this.createEncryptionKey(targetAccountPrincipal);
  }

  private createEncryptionKey(targetAccountPrincipal: iam.AccountPrincipal) {
    const key = new kms.Key(this, "S3EncryptionKey", {
      enableKeyRotation: true,
    });

    key.grantDecrypt(targetAccountPrincipal);

    return key;
  }

  private createTargetAccountPrincipal() {
    const targetAccountId = ssm.StringParameter.valueFromLookup(
      this,
      "organisaatio.tasks.datantuonti.export.role.target-account-id"
    );

    return new iam.AccountPrincipal(targetAccountId);
  }

  private createExportBucket(targetAccountPrincipal: iam.AccountPrincipal) {
    const bucket = new s3.Bucket(this, "ExportBucket");

    bucket.addLifecycleRule({
      id: "DeleteDatantuontiObjectsAfterSevenDays",
      enabled: true,
      expiration: cdk.Duration.days(7),
      prefix: "organisaatio/v1/csv/",
    });
    bucket.grantRead(targetAccountPrincipal);

    return bucket;
  }
}

class Import extends constructs.Construct {
  readonly role: iam.Role;
  readonly policy: iam.Policy;

  constructor(scope: constructs.Construct, id: string) {
    super(scope, id);

    this.policy = this.createPolicy();
    this.role = this.createRole(this.policy);
  }

  private createRole(policy: iam.Policy) {
    const role = new iam.Role(this, "Role", {
      assumedBy: new iam.ServicePrincipal("rds.amazonaws.com"),
    });
    policy.attachToRole(role);

    return role;
  }

  private createPolicy() {
    const importBucketName = ssm.StringParameter.valueFromLookup(
      this,
      "organisaatio.tasks.datantuonti.import.bucket.name"
    );

    const decryptionKeyArn = ssm.StringParameter.valueFromLookup(
      this,
      "organisaatio.tasks.datantuonti.import.bucket.decryption-key-arn"
    );
    const policy = new iam.Policy(this, "Import");

    policy.addStatements(
      new iam.PolicyStatement({
        actions: ["s3:GetObject", "s3:ListBucket"],
        resources: [
          `arn:aws:s3:::${importBucketName}`,
          `arn:aws:s3:::${importBucketName}/*`,
        ],
      }),
      new iam.PolicyStatement({
        actions: ["kms:Decrypt"],
        resources: [decryptionKeyArn],
      })
    );

    return policy;
  }
}
