import React, { useEffect } from 'react';
import { useNavigate } from 'react-router';
import * as yup from 'yup';
import { JotpaRekisterointiState, useJotpaRekisterointiSelector } from './store';

type ValidatedForm = {
    slice: keyof JotpaRekisterointiState;
    schema: yup.AnySchema;
    redirectPath: string;
};

type JotpaWizardValidatorProps = {
    validate: ValidatedForm[];
    children?: React.ReactNode;
};

function getRedirectPath(state: JotpaRekisterointiState, validate: ValidatedForm[]) {
    for (const { schema, slice, redirectPath } of validate) {
        if (!schema.isValidSync(state[slice].form)) {
            return redirectPath;
        }
    }
}

export function JotpaWizardValidator({ validate, children }: JotpaWizardValidatorProps) {
    const state = useJotpaRekisterointiSelector((state) => state);
    const navigate = useNavigate();
    const redirect = getRedirectPath(state, validate);

    useEffect(() => {
        if (redirect) {
            navigate(redirect);
        }
    }, [navigate, redirect]);

    return <>{children}</>;
}
