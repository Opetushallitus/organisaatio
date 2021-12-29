import * as React from 'react';
import { DynamicField, KoodistoContextType, Perustiedot } from '../../../../../types/types';
import { FieldErrors } from 'react-hook-form/dist/types/errors';
import { Control } from 'react-hook-form/dist/types/form';
import { Kentta, LabelLink, Rivi } from '../../LomakeFields/LomakeFields';
import InputController from '../../../../Controllers/InputController';
import SelectController from '../../../../Controllers/SelectController';
import MultiSelectController from '../../../../Controllers/MultiSelectController';

type DynamicFieldsProps = {
    dynamicFields: DynamicField[];
    validationErrors: FieldErrors<Perustiedot>;
    formControl: Control<Perustiedot>;
    getPerustiedotValues: () => Perustiedot;
    koodistot: KoodistoContextType;
    readOnly?: boolean;
};

export const DynamicFieldMethods = () => {
    const filterDynamicFields = (getPerustiedotValues: () => Perustiedot) => {
        return (showField) => {
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
export default function DynamicFields({
    dynamicFields,
    getPerustiedotValues,
    formControl,
    validationErrors,
    koodistot,
    readOnly = false,
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
                                        disabled={readOnly}
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
                                        disabled={readOnly}
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
                                        disabled={readOnly}
                                        form={formControl}
                                        validationErrors={validationErrors}
                                        name={field.name}
                                        koodisto={koodistot[field.koodisto]}
                                    />
                                </Kentta>
                            </Rivi>
                        );
                    case 'LINK':
                        return (
                            <>
                                {field.value && (
                                    <Rivi key={field.name}>
                                        <LabelLink value={field.label} to={field.value} />
                                    </Rivi>
                                )}
                            </>
                        );
                    default:
                        return <></>;
                }
            })}
        </>
    );
}
