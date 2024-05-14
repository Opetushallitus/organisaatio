import { UseFormGetValues } from 'react-hook-form/dist/types/form';

import { DynamicField, Perustiedot } from '../../../../../types/types';

export const DynamicFieldMethods = () => {
    const filterDynamicFields = (getPerustiedotValues: UseFormGetValues<Perustiedot>) => {
        return (showField: DynamicField) => {
            if (showField.when?.length > 0) {
                return showField.when.reduce((p, c) => {
                    return p || getPerustiedotValues()[c.field]?.value.match(/([^#]*).*/)[1] === c.is;
                }, false);
            }
            return true;
        };
    };
    return { filterDynamicFields };
};
