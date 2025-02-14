import assert from 'assert/strict';
import { describe, it } from 'node:test';

import { showCreateChildButton, resolveOrganisaatioTyypit } from './organisaatio';
import { rakenne, ROOT_OID } from '../contexts/constants';
import { Koodi, Koodisto, OrganisaatioType } from '../types/types';
const defaultValues = { arvo: '', disabled: false, versio: 0 };
const koodisto: Partial<Koodisto> = {
    uri2SelectOption: (uri) => {
        return { label: uri, value: uri, arvo: '', disabled: false, versio: 0 };
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
        assert.deepStrictEqual(
            resolveOrganisaatioTyypit(rakenne, koodisto as Koodisto, {
                organisaatioTyypit: [],
                oid: '123',
            }),
            []
        );
    });
    it('Works for combining two', () => {
        assert.deepStrictEqual(
            resolveOrganisaatioTyypit(rakenne, koodisto as Koodisto, {
                organisaatioTyypit: ['organisaatiotyyppi_01', 'organisaatiotyyppi_07'],
                oid: '123',
            }),
            [
                {
                    ...defaultValues,
                    value: 'organisaatiotyyppi_02',
                    label: 'organisaatiotyyppi_02',
                },
                {
                    ...defaultValues,
                    label: 'organisaatiotyyppi_04',
                    value: 'organisaatiotyyppi_04',
                },
                {
                    ...defaultValues,
                    label: 'organisaatiotyyppi_08',
                    value: 'organisaatiotyyppi_08',
                },
            ]
        );
    });
    it('Works when second has no children', () => {
        assert.deepStrictEqual(
            resolveOrganisaatioTyypit(rakenne, koodisto as Koodisto, {
                organisaatioTyypit: ['organisaatiotyyppi_01', 'organisaatiotyyppi_09'],
                oid: '123',
            }),
            [
                {
                    ...defaultValues,
                    value: 'organisaatiotyyppi_02',
                    label: 'organisaatiotyyppi_02',
                },
                {
                    ...defaultValues,
                    value: 'organisaatiotyyppi_04',
                    label: 'organisaatiotyyppi_04',
                },
            ]
        );
    });
    it('Works for combining three', () => {
        assert.deepStrictEqual(
            resolveOrganisaatioTyypit(rakenne, koodisto as Koodisto, {
                organisaatioTyypit: ['organisaatiotyyppi_01', 'organisaatiotyyppi_07', 'organisaatiotyyppi_09'],
                oid: '123',
            }),
            [
                {
                    ...defaultValues,
                    value: 'organisaatiotyyppi_02',
                    label: 'organisaatiotyyppi_02',
                },
                {
                    ...defaultValues,
                    value: 'organisaatiotyyppi_04',
                    label: 'organisaatiotyyppi_04',
                },
                {
                    ...defaultValues,
                    label: 'organisaatiotyyppi_08',
                    value: 'organisaatiotyyppi_08',
                },
            ]
        );
    });
    it('Works when parent is ROOT', () => {
        assert.deepStrictEqual(
            resolveOrganisaatioTyypit(rakenne, koodisto as Koodisto, {
                organisaatioTyypit: ['organisaatiotyyppi_01', 'organisaatiotyyppi_09'],
                oid: ROOT_OID,
            }),
            [
                {
                    ...defaultValues,
                    value: 'organisaatiotyyppi_01',
                    label: 'organisaatiotyyppi_01',
                },
                {
                    ...defaultValues,
                    value: 'organisaatiotyyppi_05',
                    label: 'organisaatiotyyppi_05',
                },
                {
                    ...defaultValues,
                    value: 'organisaatiotyyppi_06',
                    label: 'organisaatiotyyppi_06',
                },
                {
                    ...defaultValues,
                    value: 'organisaatiotyyppi_07',
                    label: 'organisaatiotyyppi_07',
                },
                {
                    ...defaultValues,
                    value: 'organisaatiotyyppi_09',
                    label: 'organisaatiotyyppi_09',
                },
            ]
        );
    });
});

describe('showCreateChildButton', () => {
    const onlyKoulutustoimijaRakenne = {
        type: ['organisaatiotyyppi_01'] as OrganisaatioType[],
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
        type: ['organisaatiotyyppi_08', 'organisaatiotyyppi_07'] as OrganisaatioType[],
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
        type: ['organisaatiotyyppi_08', 'organisaatiotyyppi_03'] as OrganisaatioType[],
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
        type: ['organisaatiotyyppi_06', 'organisaatiotyyppi_03'] as OrganisaatioType[],
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
        type: [] as OrganisaatioType[],
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
        type: ['organisaatiotyyppi_03'] as OrganisaatioType[],
        childTypes: [],
        moveTargetType: [],
        mergeTargetType: [],
        showYtj: false,
        dynamicFields: [],
    };
    it('Returns false if organisaatiotyyppi childtypes includes only disabled', () => {
        assert.strictEqual(showCreateChildButton(onlyDisabledRakene), false);
    });
    it('Returns true if only koulutustoimija is selected', () => {
        assert.strictEqual(showCreateChildButton(onlyKoulutustoimijaRakenne), true);
    });
    it('Returns true if organisaatiotyyppi childtypes one valid', () => {
        assert.strictEqual(showCreateChildButton(oneDisabledRakenne), true);
    });
    it('Returns true if all organisaatiotyyppi childtypes are valid', () => {
        assert.strictEqual(showCreateChildButton(allValidRakenne), true);
    });
    it('Returns false organisaatiotyypit is empty', () => {
        assert.strictEqual(showCreateChildButton(emptyTypeRakenne), false);
    });
    it('Returns false when organisaatiotyyppi childtypes is empty', () => {
        assert.strictEqual(showCreateChildButton(emptyChildrenRakenne), false);
    });
});
