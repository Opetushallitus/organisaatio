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
                        <td className={styles.CheckboxColumn}>
                            <span className={styles.Checkbox}>
                                <Checkbox checked={true} disabled={true} />
                            </span>
                            Organisaation nimi
                        </td>
                        <td>Sähköpostiosoite</td>
                        <td>Organisaatio OID</td>
                        <td>Yritysmuoto</td>
                        <td>Puhelinnumero</td>
                        <td>Opetuskieli</td>
                        <td>Oppilaitostunnus</td>
                        <td>Kunta</td>
                        <td>Koski-virheilmoituksen osoite</td>
                        <td>Y-tunnus</td>
                        <td>Postiosoite</td>
                        <td>Käyntiosoite</td>
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
                            <td>{_.oid}</td>
                            <td>{_.yritysmuoto}</td>
                            <td>{_.puhelinnumero ?? '-'}</td>
                            <td>{_.opetuskieli ?? '-'}</td>
                            <td>{_.oppilaitostunnus ?? '-'}</td>
                            <td>{_.kunta}</td>
                            <td>{_.koskiVirheilmoituksenOsoite ?? '-'}</td>
                            <td>{_.ytunnus}</td>
                            <td>{_.postiosoite}</td>
                            <td>{_.kayntiosoite}</td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
}
