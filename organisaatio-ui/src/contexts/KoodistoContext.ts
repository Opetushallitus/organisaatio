import {
    Koodi,
    KoodiArvo,
    Koodisto,
    KoodistoContextType,
    KoodistoSelectOption,
    KoodiUri,
    Language,
} from '../types/types';
import * as React from 'react';

export class KoodistoImpl implements Koodisto {
    private readonly koodisto: Koodi[];
    private readonly kieli: Language;
    private readonly KoodistoOptionValues: KoodistoSelectOption[];

    constructor(koodisto: Koodi[], kieli: Language) {
        this.koodisto = koodisto.sort((a, b) => a.uri.localeCompare(b.uri));
        this.kieli = kieli;
        this.KoodistoOptionValues = koodisto.map((koodi: Koodi) =>
            this.uri2SelectOption(koodi.uri, false, koodi.versio)
        );
    }

    uri2SelectOption(uri: KoodiUri, disabled = false, versio?: number): KoodistoSelectOption {
        const label = this.nimi((koodi) => koodi.uri === uri || uri?.startsWith(`${koodi.uri}#`));
        return {
            value: label === '' ? label : `${uri}${versio ? `#${versio}` : ''}`,
            label,
            disabled,
        };
    }

    uri2Nimi(uri: KoodiUri): string {
        return this.nimi((koodi) => koodi.uri === uri);
    }

    uri2Arvo(uri: KoodiUri): string {
        return this.koodit().find((koodi) => koodi.uri === uri)?.arvo || '';
    }

    arvo2Nimi(arvo: KoodiArvo): string {
        return this.nimi((koodi) => koodi.arvo === arvo);
    }

    arvo2Uri(arvo: string): string {
        return this.koodit().find((koodi) => koodi.arvo === arvo)?.uri || '';
    }

    nimet(): string[] {
        return this.koodisto.map((koodi) => this.kielistettyNimi(koodi));
    }

    koodit(): Koodi[] {
        return [...this.koodisto];
    }

    selectOptions(): KoodistoSelectOption[] {
        return [...this.KoodistoOptionValues];
    }

    private nimi(predikaatti: (koodi: Koodi) => boolean): string {
        const koodi = this.koodisto.find(predikaatti);
        let nimi = '';
        if (koodi) {
            nimi = this.kielistettyNimi(koodi);
        }
        return nimi;
    }

    private kielistettyNimi(koodi: Koodi): string {
        return koodi.nimi[this.kieli] || (this.kieli === 'fi' ? '' : koodi.nimi['fi'] || '');
    }
}

export const KoodistoContext = React.createContext<KoodistoContextType>({
    kuntaKoodisto: new KoodistoImpl([], 'fi'),
    kayttoRyhmatKoodisto: new KoodistoImpl([], 'fi'),
    ryhmaTyypitKoodisto: new KoodistoImpl([], 'fi'),
    organisaatioTyypitKoodisto: new KoodistoImpl([], 'fi'),
    ryhmanTilaKoodisto: new KoodistoImpl([], 'fi'),
    oppilaitoksenOpetuskieletKoodisto: new KoodistoImpl([], 'fi'),
    postinumerotKoodisto: new KoodistoImpl([], 'fi'),
    maatJaValtiotKoodisto: new KoodistoImpl([], 'fi'),
    vuosiluokatKoodisto: new KoodistoImpl([], 'fi'),
    oppilaitostyyppiKoodisto: new KoodistoImpl([], 'fi'),
});