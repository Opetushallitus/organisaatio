import * as React from 'react';
import styles from './PohjaSivu.module.css';

const PohjaSivu: React.FC = (props) => {
    return <div className={styles.OrganisaatioKehys}>{props.children}</div>;
};

export default PohjaSivu;
