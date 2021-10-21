import { Koodisto, KoodistoSelectOption, KoodiUri, Rakenne, ResolvedRakenne } from '../types/types';
import { ROOT_OID } from '../contexts/contexts';

//TODO pitää tsekkaa mitä tästä tulee jos tyypit ei osu mihinkään.
export const resolveOrganisaatio = (
    rakenne: Rakenne[],
    organisaatio: { tyypit: KoodiUri[]; oid: string }
): ResolvedRakenne => {
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
    organisaatio: { tyypit: KoodiUri[]; oid: string }
): KoodistoSelectOption[] => {
    const parentRakenne = resolveOrganisaatio(rakenne, organisaatio);
    return parentRakenne.childTypes
        .map((tyyppiUri) => koodisto.uri2SelectOption(tyyppiUri))
        .sort((a, b) => a.label.localeCompare(b.label));
};
/*
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
*/
