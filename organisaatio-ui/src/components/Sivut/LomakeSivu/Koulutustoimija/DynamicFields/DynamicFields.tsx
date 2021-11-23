import * as React from 'react';
import { Controller } from 'react-hook-form';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import Select from '@opetushallitus/virkailija-ui-components/Select';
import { ValueType } from 'react-select';
import { DynamicField, KoodistoContextType, Perustiedot } from '../../../../../types/types';
import { FieldErrors } from 'react-hook-form/dist/types/errors';
import { Control } from 'react-hook-form/dist/types/form';
import { Kentta, Rivi } from '../../LomakeFields/LomakeFields';

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
                                    <Controller
                                        control={formControl}
                                        name={field.name}
                                        render={({ field: { ref, ...rest } }) => {
                                            return (
                                                <Input
                                                    id={field.name}
                                                    {...rest}
                                                    error={!!validationErrors[field.name]}
                                                    options={koodistot[field.koodisto]?.selectOptions()}
                                                />
                                            );
                                        }}
                                    />
                                </Kentta>
                            </Rivi>
                        );
                    case 'SELECT':
                        return (
                            <Rivi key={field.name}>
                                <Kentta label={field.label}>
                                    <Controller
                                        control={formControl}
                                        name={field.name}
                                        render={({ field: { ref, value, ...rest } }) => {
                                            return (
                                                <Select
                                                    value={value as ValueType<{ label: string; value: string }>}
                                                    id={field.name}
                                                    {...rest}
                                                    error={!!validationErrors[field.name]}
                                                    options={koodistot[field.koodisto]?.selectOptions()}
                                                />
                                            );
                                        }}
                                    />
                                </Kentta>
                            </Rivi>
                        );
                    case 'MULTI_SELECT':
                        return (
                            <Rivi key={field.name}>
                                <Kentta label={field.label}>
                                    <Controller
                                        control={formControl}
                                        name={field.name}
                                        render={({ field: { ref, value, ...rest } }) => {
                                            return (
                                                <Select
                                                    isMulti
                                                    value={value as ValueType<{ label: string; value: string }>}
                                                    id={field.name}
                                                    {...rest}
                                                    error={!!validationErrors[field.name]}
                                                    options={koodistot[field.koodisto]?.selectOptions()}
                                                />
                                            );
                                        }}
                                    />
                                </Kentta>
                            </Rivi>
                        );
                }
            })}
        </>
    );
}
