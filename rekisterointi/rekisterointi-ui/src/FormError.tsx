import React from 'react';

import styles from './FormError.module.css';

export const FormError = ({ error }: { error?: string }) => {
    return error ? <div className={styles.error}>{error}</div> : null;
};
