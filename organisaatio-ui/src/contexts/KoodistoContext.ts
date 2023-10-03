import { Koodi, Koodisto, KoodistoSelectOption, KoodiUri, Language } from '../types/types';

export class KoodistoImpl implements Koodisto {
    private readonly koodisto: Koodi[];
    private readonly kieli: Language;
    private readonly KoodistoOptionValues: KoodistoSelectOption[];

    constructor({
        koodisto,
        kieli,
        disableOption = (koodi) => koodi.tila === 'PASSIIVINEN',
    }: {
        koodisto: Koodi[];
        kieli: Language;
        disableOption?: (koodi: Koodi) => boolean;
    }) {
        this.koodisto = koodisto;
        this.kieli = kieli;
        this.KoodistoOptionValues = koodisto.map((koodi: Koodi) =>
            this.uri2SelectOption(koodi.uri, disableOption(koodi))
        );
        this.KoodistoOptionValues.sort((a, b) =>
            a.isDisabled === b.isDisabled ? a.value.localeCompare(b.value) : a.isDisabled ? 1 : -1
        );
    }

    uri2SelectOption(uri: KoodiUri, disabled = false): KoodistoSelectOption {
        return {
            ...this.nimi((koodi) => koodi.uri === uri || uri?.startsWith(`${koodi.uri}#`)),
            isDisabled: disabled,
            disabled,
        };
    }

    uri2Nimi(uri: KoodiUri): string {
        return this.nimi((koodi) => koodi.uri === uri).label;
    }

    uri2Arvo(uri: KoodiUri): string {
        return this.koodit().find((koodi) => koodi.uri === uri)?.arvo || '';
    }

    arvo2Uri(arvo: string): string {
        return this.koodit().find((koodi) => koodi.arvo === arvo)?.uri || '';
    }

    koodit(): Koodi[] {
        return [...this.koodisto];
    }

    selectOptions(): KoodistoSelectOption[] {
        return [...this.KoodistoOptionValues];
    }

    private nimi(predikaatti: (koodi: Koodi) => boolean): KoodistoSelectOption {
        return this.kielistettyNimi(this.koodisto.find(predikaatti));
    }

    private kielistettyNimi(koodi?: Koodi): KoodistoSelectOption {
        return {
            arvo: koodi?.arvo || '',
            value: koodi?.uri || '',
            versio: koodi?.versio || 0,
            label: koodi?.nimi[this.kieli] || (this.kieli === 'fi' ? '' : koodi?.nimi['fi'] || ''),
        };
    }
}
