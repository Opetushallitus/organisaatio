import React from 'react';

import { useLanguageContext } from './LanguageContext';

import styles from './FormError.module.css';

export const FormError = ({ error }: { error?: string }) => {
    const { i18n } = useLanguageContext();
    return error ? <div className={styles.error}>{i18n.translate(error)}</div> : null;
};
