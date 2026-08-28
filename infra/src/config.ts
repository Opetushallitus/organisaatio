const environments = ["hahtuva", "dev", "qa", "prod"] as const;
type EnvironmentName = (typeof environments)[number];

export type Config = {
  organisaatioTaskCpu: number;
  organisaatioTaskMemoryMiB: number;
  vardaTaskCpu: number;
  vardaTaskMemoryMiB: number;
  rekisterointiTaskCpu: number;
  rekisterointiTaskMemoryMiB: number;
  useGraviton4MainDatabase: boolean;
  opintopolkuHost: string;
  minCapacity: number;
  maxCapacity: number;
  vardaRekisterointiCapacity: number;
  features: {
    "organisaatio.tasks.datantuonti.import.enabled": boolean;
  };
  lampiExport?: {
    enabled: boolean;
    bucketName: string;
  };
  oauthJwtIssuerUri: string;
  oppijanumerorekisteriBaseUrl: string;
};
const defaultConfig = {
  organisaatioTaskCpu: 2048,
  organisaatioTaskMemoryMiB: 4096,
  vardaTaskCpu: 256,
  vardaTaskMemoryMiB: 1024,
  rekisterointiTaskCpu: 256,
  rekisterointiTaskMemoryMiB: 1024,
  useGraviton4MainDatabase: true,
  minCapacity: 1,
  maxCapacity: 1,
  vardaRekisterointiCapacity: 0,
  features: {
    "organisaatio.tasks.datantuonti.import.enabled": false,
  },
};

export function getEnvironment(): EnvironmentName {
  const env = process.env.ENV;
  if (!env) {
    throw new Error("ENV environment variable is not set");
  }
  if (!contains(environments, env)) {
    throw new Error(`Invalid environment name: ${env}`);
  }
  return env as EnvironmentName;
}

function contains(arr: readonly string[], value: string): boolean {
  return arr.includes(value);
}

export function getConfig(): Config {
  const env = getEnvironment();
  return { hahtuva, dev, qa, prod }[env];
}

export const hahtuva: Config = {
  ...defaultConfig,
  opintopolkuHost: "hahtuvaopintopolku.fi",
  vardaRekisterointiCapacity: 1,
  features: {
    "organisaatio.tasks.datantuonti.import.enabled": true,
  },
  oauthJwtIssuerUri:
    "https://hahtuva.otuva.opintopolku.fi/kayttooikeus-service",
  oppijanumerorekisteriBaseUrl:
    "https://hahtuva.oppijanumerorekisteri.opintopolku.fi/oppijanumerorekisteri-service",
};

export const dev: Config = {
  ...defaultConfig,
  opintopolkuHost: "untuvaopintopolku.fi",
  vardaRekisterointiCapacity: 1,
  features: {
    "organisaatio.tasks.datantuonti.import.enabled": true,
  },
  lampiExport: {
    enabled: true,
    bucketName: "oph-lampi-dev",
  },
  oauthJwtIssuerUri: "https://dev.otuva.opintopolku.fi/kayttooikeus-service",
  oppijanumerorekisteriBaseUrl:
    "https://dev.oppijanumerorekisteri.opintopolku.fi/oppijanumerorekisteri-service",
};

export const qa: Config = {
  ...defaultConfig,
  opintopolkuHost: "testiopintopolku.fi",
  vardaRekisterointiCapacity: 1,
  features: {
    "organisaatio.tasks.datantuonti.import.enabled": true,
  },
  lampiExport: {
    enabled: true,
    bucketName: "oph-lampi-qa",
  },
  oauthJwtIssuerUri: "https://qa.otuva.opintopolku.fi/kayttooikeus-service",
  oppijanumerorekisteriBaseUrl:
    "https://qa.oppijanumerorekisteri.opintopolku.fi/oppijanumerorekisteri-service",
};

export const prod: Config = {
  ...defaultConfig,
  organisaatioTaskCpu: 4096,
  organisaatioTaskMemoryMiB: 12288,
  vardaTaskCpu: 512,
  vardaTaskMemoryMiB: 2048,
  rekisterointiTaskCpu: 512,
  rekisterointiTaskMemoryMiB: 2048,
  useGraviton4MainDatabase: false,
  opintopolkuHost: "opintopolku.fi",
  vardaRekisterointiCapacity: 1,
  lampiExport: {
    enabled: true,
    bucketName: "oph-lampi-prod",
  },
  oauthJwtIssuerUri: "https://prod.otuva.opintopolku.fi/kayttooikeus-service",
  oppijanumerorekisteriBaseUrl:
    "https://prod.oppijanumerorekisteri.opintopolku.fi/oppijanumerorekisteri-service",
};
