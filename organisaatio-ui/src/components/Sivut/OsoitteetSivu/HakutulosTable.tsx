import React from 'react';
import { Hakutulos } from './OsoitteetApi';
import styles from './HakutulosTable.module.css';
import Checkbox from '@opetushallitus/virkailija-ui-components/Checkbox';

type HakutulosTableProps = {
    results: Hakutulos[];
};
export function HakutulosTable({ results }: HakutulosTableProps) {
    return (
        <div className={styles.HakutulosTable}>
            <table className={styles.Table}>
                <thead className={styles.Header}>
                    <tr>
                        <td className={styles.ColumnCheckbox}>
                            <span className={styles.Checkbox}>
                                <Checkbox checked={true} disabled={true} />
                            </span>
                            Organisaation nimi
                        </td>
                        <td className={styles.ColumnEmail}>Sähköpostiosoite</td>
                        <td className={styles.ColumnOid}>Organisaatio OID</td>
                        <td className={styles.ColumnYritysmuoto}>Yritysmuoto</td>
                        <td>Puhelinnumero</td>
                        <td>Opetuskieli</td>
                        <td>Oppilaitostunnus</td>
                        <td>Kunta</td>
                        <td>Koski-virheilmoituksen osoite</td>
                        <td>Y-tunnus</td>
                        <td className={styles.ColumnOsoite}>Postiosoite</td>
                        <td className={styles.ColumnOsoite}>Käyntiosoite</td>
                    </tr>
                </thead>
                <tbody className={styles.Body}>
                    {results.map((_) => (
                        <tr key={_.id}>
                            <td className={styles.ColumnCheckbox + ' ' + styles.Fade}>
                                <span className={styles.Checkbox}>
                                    <Checkbox checked={true} disabled={true} />
                                </span>
                                {_.nimi}
                            </td>
                            <td className={styles.ColumnEmail + ' ' + styles.Fade}>{_.sahkoposti ?? '-'}</td>
                            <td className={styles.ColumnOid + ' ' + styles.Fade}>{_.oid}</td>
                            <td className={styles.ColumnYritysmuoto + ' ' + styles.Fade}>{_.yritysmuoto}</td>
                            <td>{_.puhelinnumero ?? '-'}</td>
                            <td>{_.opetuskieli ?? '-'}</td>
                            <td>{_.oppilaitostunnus ?? '-'}</td>
                            <td>{_.kunta}</td>
                            <td>{_.koskiVirheilmoituksenOsoite ?? '-'}</td>
                            <td>{_.ytunnus}</td>
                            <td className={styles.ColumnOsoite + ' ' + styles.Fade}>{_.postiosoite ?? '-'}</td>
                            <td className={styles.ColumnOsoite + ' ' + styles.Fade}>{_.kayntiosoite ?? '-'}</td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
}
