import React from 'react';
import { Hakutulos } from './OsoitteetApi';
import styles from './HakutulosTable.module.css';
import Checkbox from '@opetushallitus/virkailija-ui-components/Checkbox';

type HakutulosTableProps = {
    results: Hakutulos[];
};
export function HakutulosTable({ results }: HakutulosTableProps) {
    return (
        <table className={styles.Table}>
            <thead className={styles.Header}>
                <tr>
                    <td className={styles.CheckboxColumn}>
                        <span className={styles.Checkbox}>
                            <Checkbox checked={true} disabled={true} />
                        </span>
                        Organisaation nimi
                    </td>
                    <td>Sähköpostiosoite</td>
                </tr>
            </thead>
            <tbody className={styles.Body}>
                {results.map((_) => (
                    <tr key={_.id}>
                        <td className={styles.CheckboxColumn}>
                            <span className={styles.Checkbox}>
                                <Checkbox checked={true} disabled={true} />
                            </span>
                            {_.nimi}
                        </td>
                        <td>{_.sahkoposti ?? '-'}</td>
                    </tr>
                ))}
            </tbody>
        </table>
    );
}
