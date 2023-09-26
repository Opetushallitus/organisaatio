import * as React from 'react';
import styles from './PohjaSivu.module.css';

type PohjaSivuProps = {
    backgroundColor?: string;
};
const PohjaSivu: React.FC<PohjaSivuProps> = (props) => {
    const style = props.backgroundColor ? { backgroundColor: props.backgroundColor } : {};
    return (
        <div className={styles.OrganisaatioKehys} style={style}>
            {props.children}
        </div>
    );
};

export default PohjaSivu;
