import assert from 'assert/strict';
import { describe, it } from 'node:test';

import { UseFormGetValues } from 'react-hook-form';
import { DynamicField, Perustiedot } from '../../../../../types/types';
import { DynamicFieldMethods } from './DynamicFieldMethods';

describe('DynamicField', () => {
    const dynamicFields: DynamicField[] = [
        {
            name: 'vuosiluokat',
            label: 'PERUSTIETO_VUOSILUOKAT',
            type: 'MULTI_SELECT',
            koodisto: 'vuosiluokatKoodisto',
            when: [
                {
                    field: 'oppilaitosTyyppiUri',
                    is: 'oppilaitostyyppi_11',
                },
            ],
        },
    ];

    it('filters out if value is not matching', () => {
        const filtered = dynamicFields.filter(
            DynamicFieldMethods().filterDynamicFields((() => ({
                oppilaitosTyyppiUri: { value: 'foo' },
            })) as UseFormGetValues<Perustiedot>)
        );
        assert.strictEqual(filtered.length, 0);
    });
    it('filters in if value is matching without version', () => {
        const filtered = dynamicFields.filter(
            DynamicFieldMethods().filterDynamicFields((() => ({
                oppilaitosTyyppiUri: { value: 'oppilaitostyyppi_11' },
            })) as UseFormGetValues<Perustiedot>)
        );
        assert.strictEqual(filtered.length, 1);
    });
    it('filters in if value is matching with version', () => {
        const filtered = dynamicFields.filter(
            DynamicFieldMethods().filterDynamicFields((() => ({
                oppilaitosTyyppiUri: { value: 'oppilaitostyyppi_11#15' },
            })) as UseFormGetValues<Perustiedot>)
        );
        assert.strictEqual(filtered.length, 1);
    });
});
