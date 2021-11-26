import * as React from 'react';
import { DynamicField, KoodistoContextType, Perustiedot } from '../../../../../types/types';
import { FieldErrors } from 'react-hook-form/dist/types/errors';
import { Control } from 'react-hook-form/dist/types/form';
import { Kentta, Rivi } from '../../LomakeFields/LomakeFields';
import InputController from '../../../../Controllers/InputController';
import SelectController from '../../../../Controllers/SelectController';
import MultiSelectController from '../../../../Controllers/MultiSelectController';

type DynamicFieldsProps = {
    dynamicFields: DynamicField[];
    validationErrors: FieldErrors<Perustiedot>;
    formControl: Control<Perustiedot>;
    getPerustiedotValues: () => Perustiedot;
    koodistot: KoodistoContextType;
};

export const DynamicFieldMethods = () => {
    const filterDynamicFields = (getPerustiedotValues: () => Perustiedot) => {
        return (a) => !a.when || getPerustiedotValues()[a.when.name]?.value.match(/([^#]*).*/)[1] === a.when.value;
    };
    return { filterDynamicFields };
};
export default function DynamicFields({
    dynamicFields,
    getPerustiedotValues,
    formControl,
    validationErrors,
    koodistot,
}: DynamicFieldsProps) {
    return (
        <>
            {dynamicFields.filter(DynamicFieldMethods().filterDynamicFields(getPerustiedotValues)).map((field) => {
                switch (field.type) {
                    case 'INPUT':
                        return (
                            <Rivi key={field.name}>
                                <Kentta label={field.label}>
                                    <InputController<Perustiedot>
                                        form={formControl}
                                        name={field.name}
                                        validationErrors={validationErrors}
                                    />
                                </Kentta>
                            </Rivi>
                        );
                    case 'SELECT':
                        return (
                            <Rivi key={field.name}>
                                <Kentta label={field.label}>
                                    <SelectController
                                        form={formControl}
                                        validationErrors={validationErrors}
                                        name={field.name}
                                        koodisto={koodistot[field.koodisto]}
                                    />
                                </Kentta>
                            </Rivi>
                        );
                    case 'MULTI_SELECT':
                        return (
                            <Rivi key={field.name}>
                                <Kentta label={field.label}>
                                    <MultiSelectController
                                        form={formControl}
                                        validationErrors={validationErrors}
                                        name={field.name}
                                        koodisto={koodistot[field.koodisto]}
                                    />
                                </Kentta>
                            </Rivi>
                        );
                }
            })}
        </>
    );
}
