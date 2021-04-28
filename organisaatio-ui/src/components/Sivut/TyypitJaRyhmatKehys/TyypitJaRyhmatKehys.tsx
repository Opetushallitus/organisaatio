import * as React from 'react';
import styles from './TyypitJaRyhmatKehys.module.css';
import PohjaSivu from '../PohjaSivu/PohjaSivu';

const TyypitJaRyhmatKehys: React.FC = (props) => {
    return (
        <PohjaSivu>
            <div className={styles.TyypitJaRyhmatKehys}>{props.children}</div>
        </PohjaSivu>
    );
};

export default TyypitJaRyhmatKehys;
