import React from 'react';

import styles from './ButtonGroup.module.css';

type ButtonGroupProps = {
    children?: React.ReactNode;
};

export const ButtonGroup = ({ children }: ButtonGroupProps) => {
    return <div className={styles.buttonGroup}>{children}</div>;
};
