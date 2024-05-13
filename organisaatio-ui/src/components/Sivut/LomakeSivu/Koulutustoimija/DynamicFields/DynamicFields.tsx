import * as React from 'react';
import { DynamicField, Koodistot, Perustiedot } from '../../../../../types/types';
import { FieldErrors } from 'react-hook-form/dist/types/errors';
import { Control, UseFormGetValues } from 'react-hook-form/dist/types/form';
import { Kentta, LabelLink, Rivi } from '../../LomakeFields/LomakeFields';
import InputController from '../../../../Controllers/InputController';
import SelectController from '../../../../Controllers/SelectController';
import MultiSelectController from '../../../../Controllers/MultiSelectController';
import { DynamicFieldMethods } from './DynamicFieldMethods';

type DynamicFieldsProps = {
    dynamicFields: DynamicField[];
    validationErrors: FieldErrors<Perustiedot>;
    formControl: Control<Perustiedot>;
    getPerustiedotValues: UseFormGetValues<Perustiedot>;
    koodistot: Koodistot;
    readOnly?: boolean;
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
                                <Kentta label={field.label} error={validationErrors[field.name]}>
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
                            <div key={field.name}>
                                {field.value && (
                                    <Rivi>
                                        <LabelLink value={field.label} to={field.value} />
                                    </Rivi>
                                )}
                            </div>
                        );
                    default:
                        return <></>;
                }
            })}
        </>
    );
}
