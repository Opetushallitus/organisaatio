import { Koodi, KoodiUri, Organisaatio, Rakenne, ResolvedRakenne } from '../types/types';
import { ROOT_OID } from '../contexts/contexts';

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
    tyypit: Koodi[] | undefined,
    organisaatio: { tyypit: KoodiUri[]; oid: string } | undefined
): Koodi[] | undefined => {
    if (tyypit === undefined || organisaatio === undefined) return undefined;
    const parentRakenne = resolveOrganisaatio(rakenne, organisaatio);
    if (parentRakenne) {
        return tyypit
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

export const organisaatioNimiByLanguage = (organisaatio: Organisaatio | undefined, language: string) => {
    return (
        organisaatio?.nimi[language] ||
        organisaatio?.nimi['fi'] ||
        organisaatio?.nimi['sv'] ||
        organisaatio?.nimi['en'] ||
        ''
    );
};
