import { showCreateChildButton, resolveOrganisaatioTyypit, resolveParentOidByQuery } from './organisaatio';
import { rakenne, ROOT_OID } from '../contexts/constants';
import { Koodi, Koodisto } from '../types/types';
const koodisto: Partial<Koodisto> = {
    uri2SelectOption: (uri) => {
        return { label: uri, value: uri };
    },
    koodit: () =>
        [
            {
                uri: 'organisaatiotyyppi_01',
            },
            {
                uri: 'organisaatiotyyppi_02',
            },
            {
                uri: 'organisaatiotyyppi_03',
            },
            {
                uri: 'organisaatiotyyppi_04',
            },
            {
                uri: 'organisaatiotyyppi_05',
            },
            {
                uri: 'organisaatiotyyppi_06',
            },
            {
                uri: 'organisaatiotyyppi_07',
            },
            {
                uri: 'organisaatiotyyppi_08',
            },
            {
                uri: 'organisaatiotyyppi_09',
            },
        ] as Koodi[],
};

describe('resolveOrganisaatioTyypit', () => {
    it('Works for empty list', () => {
        expect(
            resolveOrganisaatioTyypit(rakenne, koodisto as Koodisto, {
                organisaatioTyypit: [],
                oid: '123',
            })
        ).toStrictEqual([]);
    });
    it('Works for combining two', () => {
        expect(
            resolveOrganisaatioTyypit(rakenne, koodisto as Koodisto, {
                organisaatioTyypit: ['organisaatiotyyppi_01', 'organisaatiotyyppi_07'],
                oid: '123',
            })
        ).toStrictEqual([
            {
                value: 'organisaatiotyyppi_02',
                label: 'organisaatiotyyppi_02',
            },
            {
                label: 'organisaatiotyyppi_04',
                value: 'organisaatiotyyppi_04',
            },
            {
                label: 'organisaatiotyyppi_08',
                value: 'organisaatiotyyppi_08',
            },
        ]);
    });
    it('Works when second has no children', () => {
        expect(
            resolveOrganisaatioTyypit(rakenne, koodisto as Koodisto, {
                organisaatioTyypit: ['organisaatiotyyppi_01', 'organisaatiotyyppi_09'],
                oid: '123',
            })
        ).toStrictEqual([
            {
                value: 'organisaatiotyyppi_02',
                label: 'organisaatiotyyppi_02',
            },
            {
                value: 'organisaatiotyyppi_04',
                label: 'organisaatiotyyppi_04',
            },
        ]);
    });
    it('Works for combining three', () => {
        expect(
            resolveOrganisaatioTyypit(rakenne, koodisto as Koodisto, {
                organisaatioTyypit: ['organisaatiotyyppi_01', 'organisaatiotyyppi_07', 'organisaatiotyyppi_09'],
                oid: '123',
            })
        ).toStrictEqual([
            {
                value: 'organisaatiotyyppi_02',
                label: 'organisaatiotyyppi_02',
            },
            {
                value: 'organisaatiotyyppi_04',
                label: 'organisaatiotyyppi_04',
            },
            {
                label: 'organisaatiotyyppi_08',
                value: 'organisaatiotyyppi_08',
            },
        ]);
    });
    it('Works when parent is ROOT', () => {
        expect(
            resolveOrganisaatioTyypit(rakenne, koodisto as Koodisto, {
                organisaatioTyypit: ['organisaatiotyyppi_01', 'organisaatiotyyppi_09'],
                oid: ROOT_OID,
            })
        ).toStrictEqual([
            {
                value: 'organisaatiotyyppi_01',
                label: 'organisaatiotyyppi_01',
            },
            {
                value: 'organisaatiotyyppi_05',
                label: 'organisaatiotyyppi_05',
            },
            {
                value: 'organisaatiotyyppi_06',
                label: 'organisaatiotyyppi_06',
            },
            {
                value: 'organisaatiotyyppi_07',
                label: 'organisaatiotyyppi_07',
            },
            {
                value: 'organisaatiotyyppi_09',
                label: 'organisaatiotyyppi_09',
            },
        ]);
    });
});

describe('resolveParentOidByQuery', () => {
    it('Maps root oid to parent if there is no parentOid', () => {
        expect(resolveParentOidByQuery('testiQueryString=testi')).toBe(ROOT_OID);
    });
    it('Gets parentOid from query string if it exists', () => {
        const parentOid = '1.23.1.21111000';
        expect(resolveParentOidByQuery(`parentOid=${parentOid}`)).toBe(parentOid);
    });
});

describe('showCreateChildButton', () => {
    const onlyKoulutustoimijaRakenne = {
        type: ['organisaatiotyyppi_01'],
        childTypes: [
            {
                type: 'organisaatiotyyppi_02',
            },
            {
                type: 'organisaatiotyyppi_04',
            },
        ],
        moveTargetType: [],
        mergeTargetType: [],
        showYtj: false,
        dynamicFields: [],
    };

    const onlyDisabledRakene = {
        type: ['organisaatiotyyppi_08', 'organisaatiotyyppi_07'],
        childTypes: [
            {
                type: 'organisaatiotyyppi_08',
                disabled: true,
            },
            {
                type: 'organisaatiotyyppi_07',
                disabled: true,
            },
        ],
        moveTargetType: [],
        mergeTargetType: [],
        showYtj: false,
        dynamicFields: [],
    };
    const oneDisabledRakenne = {
        type: ['organisaatiotyyppi_08', 'organisaatiotyyppi_03'],
        childTypes: [
            {
                type: 'organisaatiotyyppi_08',
                disabled: true,
            },
            {
                type: 'organisaatiotyyppi_03',
            },
        ],
        moveTargetType: [],
        mergeTargetType: [],
        showYtj: false,
        dynamicFields: [],
    };

    const allValidRakenne = {
        type: ['organisaatiotyyppi_06', 'organisaatiotyyppi_03'],
        childTypes: [
            {
                type: 'organisaatiotyyppi_06',
                disabled: true,
            },
            {
                type: 'organisaatiotyyppi_03',
            },
        ],
        moveTargetType: [],
        mergeTargetType: [],
        showYtj: false,
        dynamicFields: [],
    };

    const emptyTypeRakenne = {
        type: [],
        childTypes: [
            {
                type: 'organisaatiotyyppi_06',
                disabled: true,
            },
            {
                type: 'organisaatiotyyppi_03',
            },
        ],
        moveTargetType: [],
        mergeTargetType: [],
        showYtj: false,
        dynamicFields: [],
    };
    const emptyChildrenRakenne = {
        type: ['organisaatiotyyppi_03'],
        childTypes: [],
        moveTargetType: [],
        mergeTargetType: [],
        showYtj: false,
        dynamicFields: [],
    };
    test.each([
        ['Returns false if organisaatiotyyppi childtypes includes only disabled', onlyDisabledRakene, false],
        ['Returns true if only koulutustoimija is selected', onlyKoulutustoimijaRakenne, true],
        ['Returns true if organisaatiotyyppi childtypes one valid', oneDisabledRakenne, true],
        ['Returns true if all organisaatiotyyppi childtypes are valid', allValidRakenne, true],
        ['Returns false organisaatiotyypit is empty', emptyTypeRakenne, false],
        ['Returns false when organisaatiotyyppi childtypes is empty', emptyChildrenRakenne, false],
    ])('%s', (_, input, expected) => {
        expect(showCreateChildButton(input)).toStrictEqual(expected);
    });
});
