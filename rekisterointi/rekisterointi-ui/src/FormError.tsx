import React from 'react';

import { useLanguageContext } from './LanguageContext';

import styles from './FormError.module.css';

export const FormError = ({ error, inputId }: { error?: string; inputId: string }) => {
    const { i18n } = useLanguageContext();
    return error ? (
        <div className={styles.error} data-test-id={`error-${inputId}`}>
            {i18n.translate(error)}
        </div>
    ) : null;
};
