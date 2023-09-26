import React from 'react';
import { Hakutulos } from './OsoitteetApi';
import css from './HakutulosTable.module.css';
import Checkbox from '@opetushallitus/virkailija-ui-components/Checkbox';

type HakutulosTableProps = {
    results: Hakutulos[];
};
export function HakutulosTable({ results }: HakutulosTableProps) {
    return (
        <table className={css.Table}>
            <thead className={css.Header}>
                <tr>
                    <td className={css.CheckboxColumn}>
                        <Checkbox checked={true} disabled={true} />
                    </td>
                    <td>Organisaation nimi</td>
                    <td>Sähköpostiosoite</td>
                </tr>
            </thead>
            <tbody className={css.Body}>
                {results.map((_) => (
                    <tr key={_.id}>
                        <td className={css.CheckboxColumn}>
                            <Checkbox checked={true} disabled={true} />
                        </td>
                        <td>{_.nimi}</td>
                        <td>{_.sahkoposti ?? '-'}</td>
                    </tr>
                ))}
            </tbody>
        </table>
    );
}
