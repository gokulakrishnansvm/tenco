import * as cdk from "aws-cdk-lib";
import { Construct } from "constructs";
import * as ec2 from "aws-cdk-lib/aws-ec2";
import * as ecs from "aws-cdk-lib/aws-ecs";
import * as ecsPatterns from "aws-cdk-lib/aws-ecs-patterns";
import * as rds from "aws-cdk-lib/aws-rds";
import * as elasticache from "aws-cdk-lib/aws-elasticache";
import * as s3 from "aws-cdk-lib/aws-s3";
import * as secretsmanager from "aws-cdk-lib/aws-secretsmanager";
import * as path from "path";

/**
 * TENCO backend infrastructure: VPC, RDS Postgres, ElastiCache Redis, and an
 * ECS Fargate service (behind an ALB) running the Spring Boot container.
 *
 * Credential-bearing values (JWT secret, Razorpay/FCM/SMS) come from Secrets Manager;
 * create/populate that secret before deploying.
 */
export class TencoStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    const vpc = new ec2.Vpc(this, "Vpc", { maxAzs: 2, natGateways: 1 });

    // --- Postgres (RDS) ---
    const db = new rds.DatabaseInstance(this, "Db", {
      engine: rds.DatabaseInstanceEngine.postgres({ version: rds.PostgresEngineVersion.VER_16 }),
      vpc,
      instanceType: ec2.InstanceType.of(ec2.InstanceClass.BURSTABLE4_GRAVITON, ec2.InstanceSize.MICRO),
      credentials: rds.Credentials.fromGeneratedSecret("tenco"),
      databaseName: "tenco",
      allocatedStorage: 20,
      maxAllocatedStorage: 100,
      multiAz: false,
      removalPolicy: cdk.RemovalPolicy.SNAPSHOT,
    });

    // --- Redis (ElastiCache) ---
    const redisSg = new ec2.SecurityGroup(this, "RedisSg", { vpc });
    const redisSubnets = new elasticache.CfnSubnetGroup(this, "RedisSubnets", {
      description: "TENCO redis subnets",
      subnetIds: vpc.privateSubnets.map((s) => s.subnetId),
    });
    const redis = new elasticache.CfnCacheCluster(this, "Redis", {
      cacheNodeType: "cache.t4g.micro",
      engine: "redis",
      numCacheNodes: 1,
      vpcSecurityGroupIds: [redisSg.securityGroupId],
      cacheSubnetGroupName: redisSubnets.ref,
    });

    // --- Photos bucket ---
    const bucket = new s3.Bucket(this, "PhotosBucket", {
      encryption: s3.BucketEncryption.S3_MANAGED,
      blockPublicAccess: s3.BlockPublicAccess.BLOCK_ALL,
      removalPolicy: cdk.RemovalPolicy.RETAIN,
    });

    // --- App secrets (create & populate separately) ---
    const appSecret = secretsmanager.Secret.fromSecretNameV2(this, "AppSecret", "tenco/app");

    // --- Fargate service ---
    const service = new ecsPatterns.ApplicationLoadBalancedFargateService(this, "Api", {
      vpc,
      cpu: 512,
      memoryLimitMiB: 1024,
      desiredCount: 2,
      publicLoadBalancer: true,
      taskImageOptions: {
        image: ecs.ContainerImage.fromAsset(path.join(__dirname, "..", "..", "backend")),
        containerPort: 8080,
        environment: {
          SPRING_PROFILES_ACTIVE: "prod",
          DB_URL: `jdbc:postgresql://${db.dbInstanceEndpointAddress}:5432/tenco`,
          REDIS_HOST: redis.attrRedisEndpointAddress,
          REDIS_PORT: "6379",
          OTP_STORE: "redis",
          REDIS_HEALTH: "true",
          S3_BUCKET: bucket.bucketName,
          AWS_REGION: this.region,
        },
        secrets: {
          DB_USER: ecs.Secret.fromSecretsManager(db.secret!, "username"),
          DB_PASSWORD: ecs.Secret.fromSecretsManager(db.secret!, "password"),
          TENCO_JWT_SECRET: ecs.Secret.fromSecretsManager(appSecret, "jwtSecret"),
          RAZORPAY_KEY_ID: ecs.Secret.fromSecretsManager(appSecret, "razorpayKeyId"),
          RAZORPAY_KEY_SECRET: ecs.Secret.fromSecretsManager(appSecret, "razorpayKeySecret"),
          RAZORPAY_WEBHOOK_SECRET: ecs.Secret.fromSecretsManager(appSecret, "razorpayWebhookSecret"),
          MSG91_AUTHKEY: ecs.Secret.fromSecretsManager(appSecret, "msg91Authkey"),
        },
      },
    });

    service.targetGroup.configureHealthCheck({ path: "/actuator/health" });

    // Networking: allow the service to reach Postgres and Redis.
    db.connections.allowDefaultPortFrom(service.service);
    redisSg.addIngressRule(service.service.connections.securityGroups[0], ec2.Port.tcp(6379));
    bucket.grantReadWrite(service.taskDefinition.taskRole);

    new cdk.CfnOutput(this, "ApiUrl", { value: `http://${service.loadBalancer.loadBalancerDnsName}` });
  }
}
