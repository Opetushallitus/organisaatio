import * as cdk from "aws-cdk-lib";
import * as codeartifact from "aws-cdk-lib/aws-codeartifact";
import * as iam from "aws-cdk-lib/aws-iam";
import * as constructs from "constructs";

export class DependencyManagementStack extends cdk.Stack {
  readonly domainName = "oph-domain";
  readonly repositoryName = "maven";
  readonly upstreamRepositoryName = "maven-central-upstream";
  readonly githubUsernameSsmPath = "/mvn/settings/github/username";
  readonly githubPasswordSsmPath = "/mvn/settings/github/password";

  constructor(scope: constructs.Construct, id: string, props: cdk.StackProps) {
    super(scope, id, props);

    const domain = new codeartifact.CfnDomain(this, "Domain", {
      domainName: this.domainName,
    });

    const mavenCentralUpstream = new codeartifact.CfnRepository(
      this,
      "MavenCentralUpstream",
      {
        domainName: this.domainName,
        repositoryName: this.upstreamRepositoryName,
        externalConnections: ["public:maven-central"],
      }
    );
    mavenCentralUpstream.addDependency(domain);

    const repo = new codeartifact.CfnRepository(this, "Repository", {
      domainName: this.domainName,
      repositoryName: this.repositoryName,
      upstreams: [this.upstreamRepositoryName],
    });
    repo.addDependency(mavenCentralUpstream);
  }

  grantRead(grantee: iam.IGrantable): void {
    iam.Grant.addToPrincipal({
      grantee,
      actions: [
        "codeartifact:GetAuthorizationToken",
        "codeartifact:GetRepositoryEndpoint",
        "codeartifact:ReadFromRepository",
      ],
      resourceArns: [
        `arn:aws:codeartifact:${this.region}:${this.account}:domain/${this.domainName}`,
        `arn:aws:codeartifact:${this.region}:${this.account}:repository/${this.domainName}/${this.repositoryName}`,
      ],
    });
    iam.Grant.addToPrincipal({
      grantee,
      actions: ["sts:GetServiceBearerToken"],
      resourceArns: ["*"],
    });
    iam.Grant.addToPrincipal({
      grantee,
      actions: ["ssm:GetParameter"],
      resourceArns: [
        `arn:aws:ssm:${this.region}:${this.account}:parameter${this.githubUsernameSsmPath}`,
        `arn:aws:ssm:${this.region}:${this.account}:parameter${this.githubPasswordSsmPath}`,
      ],
    });
  }

  createMavenSettingsXmlCommands(): string[] {
    return [
      `CODEARTIFACT_AUTH_TOKEN=$(aws codeartifact get-authorization-token --domain ${this.domainName} --query authorizationToken --output text)`,
      `CODEARTIFACT_REPO_URL=$(aws codeartifact get-repository-endpoint --domain ${this.domainName} --repository ${this.repositoryName} --format maven --query repositoryEndpoint --output text)`,
      `GITHUB_USERNAME=$(aws ssm get-parameter --name ${this.githubUsernameSsmPath} --query Parameter.Value --output text)`,
      `GITHUB_PASSWORD=$(aws ssm get-parameter --name ${this.githubPasswordSsmPath} --with-decryption --query Parameter.Value --output text)`,
      `cat <<EOF > codebuild-mvn-settings.xml
<settings xmlns='http://maven.apache.org/SETTINGS/1.0.0'
  xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'
  xsi:schemaLocation='http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd'>
  <servers>
    <server>
      <id>github</id>
      <username>\${GITHUB_USERNAME}</username>
      <password>\${GITHUB_PASSWORD}</password>
    </server>
    <server>
      <id>codeartifact</id>
      <username>aws</username>
      <password>\${CODEARTIFACT_AUTH_TOKEN}</password>
    </server>
  </servers>
  <mirrors>
    <mirror>
      <id>codeartifact</id>
      <name>OPH CodeArtifact Maven Central Mirror</name>
      <url>\${CODEARTIFACT_REPO_URL}</url>
      <mirrorOf>central</mirrorOf>
    </mirror>
  </mirrors>
</settings>
EOF`,
    ];
  }
}
