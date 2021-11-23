import * as React from 'react';
import { useContext } from 'react';
import styles from '../PerustietoLomake/PerustietoLomake.module.css';
import { Controller } from 'react-hook-form';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import Select from '@opetushallitus/virkailija-ui-components/Select';
import { ValueType } from 'react-select';
import { DynamicField, KoodistoContextType, Perustiedot } from '../../../../../types/types';
import { LanguageContext } from '../../../../../contexts/contexts';
import { FieldErrors } from 'react-hook-form/dist/types/errors';
import { Control } from 'react-hook-form/dist/types/form';

type DynamicFieldsProps = {
    dynamicFields: DynamicField[];
    validationErrors: FieldErrors<Perustiedot>;
    formControl: Control<Perustiedot>;
    getPerustiedotValues: () => Perustiedot;
    koodistot: KoodistoContextType;
};

export const DynamicFieldMethods = () => {
    const filterDynamicFields = (getPerustiedotValues: () => Perustiedot) => {
        return (a) => !a.when || getPerustiedotValues()[a.when.name].value.match(/([^#]*).*/)[1] === a.when.value;
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
    const { i18n } = useContext(LanguageContext);
    return (
        <>
            {dynamicFields.filter(DynamicFieldMethods().filterDynamicFields(getPerustiedotValues)).map((field) => {
                switch (field.type) {
                    case 'INPUT':
                        return (
                            <div key={field.name} className={styles.Rivi}>
                                <div className={styles.Kentta}>
                                    <label>{field.label}</label>
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
                                </div>
                            </div>
                        );
                    case 'SELECT':
                        return (
                            <div key={field.name} className={styles.Rivi}>
                                <div className={styles.Kentta}>
                                    <label>{i18n.translate(field.label)}</label>
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
                                </div>
                            </div>
                        );
                    case 'MULTI_SELECT':
                        return (
                            <div key={field.name} className={styles.Rivi}>
                                <div className={styles.Kentta}>
                                    <label>{i18n.translate(field.label)}</label>
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
                                </div>
                            </div>
                        );
                }
            })}
        </>
    );
}
