import * as React from 'react';
import styles from './ModalFields.module.css';
import { useAtom } from 'jotai';
import { languageAtom } from '../../../api/lokalisaatio';

const BodyKehys = (props: { children: React.ReactNode }) => <div className={styles.BodyKehys}>{props.children}</div>;
const BodyRivi = (props: { children: React.ReactNode }) => <div className={styles.BodyRivi}>{props.children}</div>;
const BodyKentta = ({
    label,
    children,
    isRequired = false,
}: {
    label?: string;
    children: React.ReactNode;
    isRequired?: boolean;
}) => {
    const [i18n] = useAtom(languageAtom);
    return (
        <div id={label} className={styles.BodyKentta}>
            {label && <label className={isRequired ? styles.Required : undefined}>{i18n.translate(label)}</label>}
            {children}
        </div>
    );
};

export { BodyKehys, BodyRivi, BodyKentta };
