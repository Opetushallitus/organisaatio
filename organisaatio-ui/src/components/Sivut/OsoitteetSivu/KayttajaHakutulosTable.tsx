import React from 'react';
import { FixedSizeList as List, ListChildComponentProps } from 'react-window';

import { KayttajaHakutulosRow } from './OsoitteetApi';
import styles from './HakutulosTable.module.css';
import { Checkbox } from './Checkbox';

type HakutulosTableProps = {
    rows: KayttajaHakutulosRow[];
};
export function KayttajaHakutulosTable({ rows }: HakutulosTableProps) {
    const Row = ({ index, style }: ListChildComponentProps) => {
        const _ = rows[index];
        const rowStyle = index % 2 ? styles.Odd : styles.Even;
        return (
            <div role="row" className={styles.Body + ' ' + rowStyle} style={style}>
                <div className={styles.ColumnHalf + ' ' + styles.Fade}>
                    <span className={styles.Checkbox}>
                        <Checkbox checked={true} onChange={() => {}} disabled={true}>
                            {_.etunimet + ' ' + _.sukunimi}
                        </Checkbox>
                    </span>
                </div>
                <div className={styles.ColumnHalf + ' ' + styles.Fade}>{_.sahkoposti ?? '-'}</div>
            </div>
        );
    };

    return (
        <div role="table" className={styles.HakutulosTable}>
            <div role="rowgroup">
                <div role="row" className={styles.Header}>
                    <div className={styles.ColumnHalf}>
                        <span className={styles.Checkbox}>
                            <Checkbox checked={true} onChange={() => {}} disabled={true}>
                                Nimi
                            </Checkbox>
                        </span>
                    </div>
                    <div className={styles.ColumnHalf}>Sähköpostiosoite</div>
                </div>
            </div>
            <div role="rowgroup">
                <List itemSize={50} height={490} width={1320} itemCount={rows.length}>
                    {Row}
                </List>
            </div>
        </div>
    );
}
