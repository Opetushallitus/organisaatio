const environments = ["hahtuva", "dev", "qa", "prod"] as const;
type EnvironmentName = (typeof environments)[number];

const defaultConfig = {
    opintopolkuHost: "",
    minCapacity: 1,
    maxCapacity: 1,
    vardaRekisterointiCapacity: 0,
    features: {
      "organisaatio.tasks.datantuonti.import.enabled": false
    }
};

export type Config = typeof defaultConfig;

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
        "organisaatio.tasks.datantuonti.import.enabled": true
    }
};

export const dev: Config = {
    ...defaultConfig,
    opintopolkuHost: "untuvaopintopolku.fi",
    vardaRekisterointiCapacity: 1,
    features: {
        "organisaatio.tasks.datantuonti.import.enabled": true
    }
};

export const qa: Config = {
    ...defaultConfig,
    opintopolkuHost: "testiopintopolku.fi",
    vardaRekisterointiCapacity: 1,
};

export const prod: Config = {
    ...defaultConfig,
    opintopolkuHost: "opintopolku.fi",
    vardaRekisterointiCapacity: 1,
};
