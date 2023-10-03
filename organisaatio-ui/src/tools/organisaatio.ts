import {
    Koodisto,
    KoodistoSelectOption,
    KoodiUri,
    Nimi,
    OrganisaatioChildType,
    Rakenne,
    ResolvedRakenne,
} from '../types/types';
import { ROOT_OID } from '../contexts/constants';
import { ApiOrganisaatio } from '../types/apiTypes';
import queryString from 'query-string';

type ResolvingOrganisaatio = { organisaatioTyypit: KoodiUri[]; oppilaitosTyyppiUri?: string; oid?: string };
export const resolveOrganisaatio = (rakenne: Rakenne[], organisaatio: ResolvingOrganisaatio): ResolvedRakenne => {
    const tyypit = organisaatio.oid === ROOT_OID ? ['opetushallitus'] : [...organisaatio.organisaatioTyypit];
    const distinctTypeFilter = (a: OrganisaatioChildType, i: number, array: OrganisaatioChildType[]): boolean =>
        array.findIndex((b) => b.type === a.type) === i;
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
                    childTypes: [...previous.childTypes, ...current.childTypes].filter(distinctTypeFilter),
                    dynamicFields: [
                        ...previous.dynamicFields,
                        ...current.dynamicFields.filter((a) => {
                            return !previous.dynamicFields.some((b) => b.name === a.name);
                        }),
                    ],
                    showYtj: current.showYtj || previous.showYtj,
                };
            },
            {
                type: [],
                mergeTargetType: [],
                moveTargetType: [],
                childTypes: [],
                dynamicFields: [],
                showYtj: false,
            }
        );
};
export const resolveOrganisaatioTyypit = (
    rakenne: Rakenne[],
    koodisto: Koodisto,
    organisaatio: ResolvingOrganisaatio
): KoodistoSelectOption[] => {
    const parentRakenne = resolveOrganisaatio(rakenne, organisaatio);
    return parentRakenne.childTypes
        .map((tyyppi) => koodisto.uri2SelectOption(tyyppi.type, tyyppi.disabled))
        .sort((a, b) => a.label.localeCompare(b.label));
};

export const mapOrganisaatioToSelect = (o: ApiOrganisaatio | undefined, language: keyof Nimi) => {
    if (o)
        return {
            value: `${o.oid}`,
            label: `${o.nimi[language]} (${o.ytunnus || o.oppilaitosKoodi || o.oid})`,
        };
    else return { value: '', label: '' };
};
export const organisaatioSelectMapper = (organisaatiot: ApiOrganisaatio[], language: keyof Nimi) =>
    organisaatiot.map((o: ApiOrganisaatio) => mapOrganisaatioToSelect(o, language));

export const resolveParentOidByQuery = (searchStr: string): string => {
    const { parentOid } = queryString.parse(searchStr);
    return (parentOid as string) || ROOT_OID;
};

export const showCreateChildButton = (organisaatioRakenne: ResolvedRakenne): boolean =>
    organisaatioRakenne?.type.length > 0 &&
    organisaatioRakenne?.childTypes.length > 0 &&
    !!organisaatioRakenne.childTypes.find((resolvedTyyppi) => !resolvedTyyppi.disabled);
