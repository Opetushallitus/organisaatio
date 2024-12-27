const environments = ["hahtuva", "dev", "qa", "prod"] as const;
type EnvironmentName = (typeof environments)[number];

const defaultConfig = {
    virkailijaHost: "",
    minCapacity: 1,
    maxCapacity: 1,
    vardaRekisterointiCapacity: 0,
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
    virkailijaHost: "virkailija.hahtuvaopintopolku.fi",
    vardaRekisterointiCapacity: 1,
};

export const dev: Config = {
    ...defaultConfig,
    virkailijaHost: "virkailija.untuvaopintopolku.fi",
    vardaRekisterointiCapacity: 1,
};

export const qa: Config = {
    ...defaultConfig,
    virkailijaHost: "virkailija.testiopintopolku.fi",
    vardaRekisterointiCapacity: 0,
};

export const prod: Config = {
    ...defaultConfig,
    virkailijaHost: "virkailija.opintopolku.fi",
    vardaRekisterointiCapacity: 0,
};
