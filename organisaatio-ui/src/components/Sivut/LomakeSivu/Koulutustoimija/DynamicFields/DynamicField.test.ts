import { UseFormGetValues } from 'react-hook-form';
import { DynamicField, Perustiedot } from '../../../../../types/types';
import { DynamicFieldMethods } from './DynamicFields';

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
        expect(filtered.length).toEqual(0);
    });
    it('filters in if value is matching without version', () => {
        const filtered = dynamicFields.filter(
            DynamicFieldMethods().filterDynamicFields((() => ({
                oppilaitosTyyppiUri: { value: 'oppilaitostyyppi_11' },
            })) as UseFormGetValues<Perustiedot>)
        );
        expect(filtered.length).toEqual(1);
    });
    it('filters in if value is matching with version', () => {
        const filtered = dynamicFields.filter(
            DynamicFieldMethods().filterDynamicFields((() => ({
                oppilaitosTyyppiUri: { value: 'oppilaitostyyppi_11#15' },
            })) as UseFormGetValues<Perustiedot>)
        );
        expect(filtered.length).toEqual(1);
    });
});
