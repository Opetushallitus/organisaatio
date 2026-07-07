import React from 'react';
import { List, RowComponentProps } from 'react-window';

import { HakutulosRow, Osoite } from './OsoitteetApi';
import styles from './HakutulosTable.module.css';
import { Checkbox } from './Checkbox';

type HakutulosTableProps = {
    rows: HakutulosRow[];
    selection: Set<string>;
    setSelection: (selection: Set<string>) => void;
};

type OrganisaatioRowProps = {
    rows: HakutulosRow[];
    selection: Set<string>;
    toggle: (oid: string) => void;
    stringifyOsoite: (osoite?: Osoite) => string;
};

function Row({ index, style, rows, selection, toggle, stringifyOsoite }: RowComponentProps<OrganisaatioRowProps>) {
    const _ = rows[index];
    const rowStyle = index % 2 ? styles.Odd : styles.Even;
    return (
        <div role="row" className={styles.Body + ' ' + rowStyle} style={style}>
            <div className={styles.ColumnCheckbox + ' ' + styles.Fade}>
                <span className={styles.Checkbox}>
                    <Checkbox
                        checked={selection.has(_.oid)}
                        onChange={() => toggle(_.oid)}
                        disabled={false}
                        dataTestid={`select-${_.oid}`}
                    >
                        {_.nimi}
                    </Checkbox>
                </span>
            </div>
            <div className={styles.ColumnEmail + ' ' + styles.Fade}>{_.sahkoposti ?? '-'}</div>
            <div className={styles.ColumnShortInfo + ' ' + styles.Fade}>{_.puhelinnumero ?? '-'}</div>
            <div className={styles.ColumnShortInfo + ' ' + styles.Fade}>{_.kunta}</div>
            <div className={styles.ColumnShortInfo + ' ' + styles.Fade}>{_.yritysmuoto}</div>
            <div className={styles.ColumnShortInfo + ' ' + styles.Fade}>{_.opetuskieli ?? '-'}</div>
            <div className={styles.ColumnEmail + ' ' + styles.Fade}>{_.koskiVirheilmoituksenOsoite ?? '-'}</div>
            <div className={styles.ColumnOid + ' ' + styles.Fade}>{_.oid}</div>
            <div className={styles.ColumnShortInfo + ' ' + styles.Fade}>{_.oppilaitostunnus ?? '-'}</div>
            <div className={styles.ColumnShortInfo + ' ' + styles.Fade}>{_.ytunnus}</div>
            <div className={styles.ColumnOsoite + ' ' + styles.Fade}>{stringifyOsoite(_.postiosoite)}</div>
            <div className={styles.ColumnOsoite + ' ' + styles.Fade}>{stringifyOsoite(_.kayntiosoite)}</div>
        </div>
    );
}

export function OrganisaatioHakutulosTable({ rows, selection, setSelection }: HakutulosTableProps) {
    const toggle = (oid: string) => {
        const newSelection = new Set(selection);
        if (selection.has(oid)) {
            newSelection.delete(oid);
            setSelection(newSelection);
        } else {
            newSelection.add(oid);
            setSelection(newSelection);
        }
    };

    const toggleAll = () => {
        if (selection.size === rows.length) {
            setSelection(new Set());
        } else {
            setSelection(new Set(rows.map((r) => r.oid)));
        }
    };

    const stringifyOsoite = (osoite?: Osoite) => {
        if (!osoite) {
            return '-';
        } else {
            const postiosoite = osoite.postinumero ? `, ${osoite.postinumero} ${osoite.postitoimipaikka}` : '';
            return osoite.osoite + postiosoite;
        }
    };

    return (
        <div role="table" className={styles.HakutulosTable}>
            <div role="rowgroup">
                <div role="row" className={styles.Header}>
                    <div className={styles.ColumnCheckbox}>
                        <span className={styles.Checkbox}>
                            <Checkbox
                                checked={selection.size === rows.length}
                                onChange={toggleAll}
                                disabled={false}
                                dataTestid="select-all"
                                indeterminate={selection.size !== 0 && selection.size !== rows.length}
                            >
                                Organisaation nimi
                            </Checkbox>
                        </span>
                    </div>
                    <div className={styles.ColumnEmail}>Sähköpostiosoite</div>
                    <div className={styles.ColumnShortInfo}>Puhelinnumero</div>
                    <div className={styles.ColumnShortInfo}>Sijaintikunta</div>
                    <div className={styles.ColumnShortInfo}>Yritysmuoto</div>
                    <div className={styles.ColumnShortInfo}>Opetuskieli</div>
                    <div className={styles.ColumnEmail}>KOSKI-virheilmoituksen osoite</div>
                    <div className={styles.ColumnOid}>Organisaation OID</div>
                    <div className={styles.ColumnShortInfo}>Oppilaitostunnus</div>
                    <div className={styles.ColumnShortInfo}>Y-tunnus</div>
                    <div className={styles.ColumnOsoite}>Postiosoite</div>
                    <div className={styles.ColumnOsoite}>Käyntiosoite</div>
                </div>
            </div>
            <div role="rowgroup" className={styles.Results}>
                <List
                    rowComponent={Row}
                    rowCount={rows.length}
                    rowHeight={50}
                    rowProps={{ rows, selection, toggle, stringifyOsoite }}
                    style={{ height: 490, width: 3200 }}
                />
            </div>
        </div>
    );
}
