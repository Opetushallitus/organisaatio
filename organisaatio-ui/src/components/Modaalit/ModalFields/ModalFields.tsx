import * as React from 'react';
import styles from './ModalFields.module.css';
import { useAtom } from 'jotai';
import { languageAtom } from '../../../api/lokalisaatio';

const BodyKehys = (props) => <div className={styles.BodyKehys}>{props.children}</div>;
const BodyRivi = (props) => <div className={styles.BodyRivi}>{props.children}</div>;
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
        <div className={styles.BodyKentta}>
            {label && (
                <label>
                    {i18n.translate(label)} {isRequired && '*'}
                </label>
            )}
            {children}
        </div>
    );
};

export { BodyKehys, BodyRivi, BodyKentta };
