export type Language = 'fi' | 'sv' | 'en';
export type LocalizableText = Partial<Record<Language, string>>;

// koodisto
export type KoodiUri = string;
export type KoodiArvo = string;
export type Koodi = {
    uri: KoodiUri;
    arvo: KoodiArvo;
    nimi: LocalizableText;
};

// lokalisointi
export type Lokalisointi = Record<Language, Record<string, string>>;

type LocalizationKey = string;
type LocalizationValue = string;
export type LokalisointiRivi = {
    key: LocalizationKey;
    value: LocalizationValue;
    locale: Language;
};
