import { Koodi, KoodiUri, Organisaatio, Rakenne, ResolvedRakenne } from '../types/types';
import { Koodisto, ROOT_OID } from '../contexts/contexts';
import { YhteystiedotOsoite, YhteystiedotPhone, YtjOrganisaatio } from '../types/apiTypes';

export const resolveOrganisaatio = (
    rakenne: Rakenne[],
    organisaatio: { tyypit: KoodiUri[]; oid: string } | undefined
): ResolvedRakenne | undefined => {
    if (organisaatio === undefined) return undefined;
    let tyypit = [...organisaatio.tyypit];
    if (organisaatio.oid === ROOT_OID) {
        tyypit = ['opetushallitus'];
    }
    return rakenne
        .filter((a) => {
            return tyypit.includes(a.type);
        })
        .reduce<ResolvedRakenne>(
            (previous, current) => {
                const mergeTarget = current.mergeTargetType
                    ? [current.mergeTargetType, ...previous.mergeTargetType]
                    : previous.mergeTargetType;
                const moveTarget = current.moveTargetType
                    ? [current.moveTargetType, ...previous.moveTargetType]
                    : previous.moveTargetType;
                return {
                    type: [current.type, ...previous.type],
                    mergeTargetType: mergeTarget,
                    moveTargetType: moveTarget,
                    childTypes: [...previous.childTypes, ...current.childTypes],
                };
            },
            { type: [], mergeTargetType: [], moveTargetType: [], childTypes: [] }
        );
};
export const resolveOrganisaatioTyypit = (
    rakenne: Rakenne[],
    koodisto: Koodisto,
    organisaatio: { tyypit: KoodiUri[]; oid: string } | undefined
): Koodi[] | undefined => {
    if (koodisto === undefined || organisaatio === undefined) return undefined;
    const parentRakenne = resolveOrganisaatio(rakenne, organisaatio);
    if (parentRakenne) {
        return koodisto
            .koodit()
            .filter((t) => {
                return parentRakenne.childTypes.includes(t.uri);
            })
            .sort((a, b) => a.uri.localeCompare(b.uri));
    }
};

export const mapOrganisaatioToSelect = (o: Organisaatio | undefined, language: string) => {
    if (o)
        return {
            value: `${o.oid}`,
            label: `${o.nimi[language]} ${o.oid}`,
        };
    else return { value: '', label: '' };
};
export const organisaatioSelectMapper = (organisaatiot: Organisaatio[], language: string) =>
    organisaatiot.map((o: Organisaatio) => mapOrganisaatioToSelect(o, language));
export const mapYtjToAPIOrganisaatio = ({
    ytjOrganisaatio,
    organisaatio,
    postinumerotKoodisto,
}: {
    ytjOrganisaatio: YtjOrganisaatio;
    organisaatio?: Organisaatio;
    postinumerotKoodisto?: Koodisto;
}): Organisaatio => {
    const {
        nimi,
        postiOsoite,
        kayntiOsoite,
        yritysmuoto,
        yritysTunnus: { alkupvm, ytunnus },
    } = ytjOrganisaatio;
    const alkuPvm = alkupvm.split('.');
    [alkuPvm[0], alkuPvm[2]] = [alkuPvm[2], alkuPvm[0]]; // reverse date to YYYY-MM-DD format
    organisaatio &&
        organisaatio.yhteystiedot &&
        organisaatio.yhteystiedot
            .filter((yT) => yT.kieli === 'kieli_fi#1')
            .forEach((yT) => {
                if ((yT as YhteystiedotOsoite).osoiteTyyppi && (yT as YhteystiedotOsoite).osoiteTyyppi === 'posti') {
                    const { katu: osoite, postinumero, toimipaikka: postitoimipaikka } = postiOsoite;
                    const postinumeroKoodi = postinumerotKoodisto?.koodit().find((p) => p.arvo === postinumero);
                    yT = Object.assign(yT, {
                        osoite,
                        postinumeroUri: (postinumeroKoodi && postinumeroKoodi.uri) || '',
                        postitoimipaikka,
                    });
                } else if (
                    (yT as YhteystiedotOsoite).osoiteTyyppi &&
                    (yT as YhteystiedotOsoite).osoiteTyyppi === 'kaynti'
                ) {
                    if (kayntiOsoite) {
                        const { katu: osoite, postinumero, toimipaikka: postitoimipaikka } = kayntiOsoite;
                        const postinumeroKoodi = postinumerotKoodisto?.koodit().find((p) => p.arvo === postinumero);
                        yT = Object.assign(yT, {
                            osoite,
                            postinumeroUri: (postinumeroKoodi && postinumeroKoodi.uri) || '',
                            postitoimipaikka,
                        });
                    }
                } else if ((yT as YhteystiedotPhone).tyyppi && (yT as YhteystiedotPhone).tyyppi === 'puhelin') {
                    (yT as YhteystiedotPhone).numero = ytjOrganisaatio.puhelin;
                }
            });
    const newOganisaatio = Object.assign({}, organisaatio, {
        nimi: { fi: nimi },
        alkuPvm: alkuPvm.join('-'),
        ytunnus,
        yritysmuoto,
    });
    return newOganisaatio;
};
