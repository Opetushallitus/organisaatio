import { Koodisto, KoodistoSelectOption, KoodiUri, Rakenne, ResolvedRakenne } from '../types/types';
import { ROOT_OID } from '../contexts/contexts';
import { ApiOrganisaatio } from '../types/apiTypes';
import queryString from 'query-string';

const VAKA_TOIMIPAIKKA_TYYPPIURI = 'organisaatiotyyppi_08';
const VAKA_JARJESTAJA_TYYPPIURI = 'organisaatiotyyppi_07';

type ResolvingOrganisaatio = { organisaatioTyypit: KoodiUri[]; oppilaitosTyyppiUri?: string; oid?: string };
export const resolveOrganisaatio = (rakenne: Rakenne[], organisaatio: ResolvingOrganisaatio): ResolvedRakenne => {
    const tyypit = organisaatio.oid === ROOT_OID ? ['opetushallitus'] : [...organisaatio.organisaatioTyypit];
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
                    disabledChildTypes: [...previous.disabledChildTypes, ...current.disabledChildTypes],
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
                disabledChildTypes: [],
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
        .map((tyyppiUri) => koodisto.uri2SelectOption(tyyppiUri, parentRakenne.disabledChildTypes.includes(tyyppiUri)))
        .sort((a, b) => a.label.localeCompare(b.label));
};

export const mapOrganisaatioToSelect = (o: ApiOrganisaatio | undefined, language: string) => {
    if (o)
        return {
            value: `${o.oid}`,
            label: `${o.nimi[language]} ${o.oid}`,
        };
    else return { value: '', label: '' };
};
export const organisaatioSelectMapper = (organisaatiot: ApiOrganisaatio[], language: string) =>
    organisaatiot.map((o: ApiOrganisaatio) => mapOrganisaatioToSelect(o, language));

export const resolveParentOidByQuery = (searchStr): string => {
    const { parentOid } = queryString.parse(searchStr);
    return (parentOid as string) || ROOT_OID;
};

export const IsOnlyVakaToimipaikkaOrVakaJarjestaja = (organisaatioTyypit: string[]): boolean =>
    organisaatioTyypit?.length === 1 &&
    (organisaatioTyypit[0] === VAKA_TOIMIPAIKKA_TYYPPIURI || organisaatioTyypit[0] === VAKA_JARJESTAJA_TYYPPIURI);
