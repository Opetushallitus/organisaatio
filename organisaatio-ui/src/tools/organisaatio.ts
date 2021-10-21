import { Koodi, KoodiUri, Organisaatio, Rakenne, ResolvedRakenne } from '../types/types';
import { Koodisto, ROOT_OID } from '../contexts/contexts';

export const resolveOrganisaatio = (
    rakenne: Rakenne[],
    organisaatio: { tyypit: KoodiUri[]; oid?: string } | undefined
): ResolvedRakenne | undefined => {
    if (organisaatio === undefined) return undefined;
    const tyypit = organisaatio.oid === ROOT_OID ? ['opetushallitus'] : [...organisaatio.tyypit];
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
                    showYtj: current.showYtj || previous.showYtj,
                };
            },
            { type: [], mergeTargetType: [], moveTargetType: [], childTypes: [], showYtj: false }
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
