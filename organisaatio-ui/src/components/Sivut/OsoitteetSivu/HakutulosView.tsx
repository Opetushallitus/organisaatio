import { Hakutulos } from './OsoitteetApi';
import styles from './HakutulosView.module.css';
import { Link, useHistory } from 'react-router-dom';
import { HakutulosTable } from './HakutulosTable';
import React from 'react';

type HakutulosViewProps = {
    results?: Hakutulos[];
};

export function HakutulosView({ results }: HakutulosViewProps) {
    const history = useHistory();
    if (typeof results === 'undefined') {
        history.push('/osoitteet');
        return null;
    }

    return (
        <div className={styles.HakutulosView}>
            <div className={styles.TitleRow}>
                <div className={styles.Title}>
                    <h1 className={styles.TitleText}>Hakutulokset</h1>
                    <span className={styles.TitleAlt}>( {results.length} )</span>
                </div>
                <Link to={'/osoitteet'}>Muokkaa hakua</Link>
            </div>
            <div className={styles.SininenPalkki}>{results.length} hakutulosta valittu</div>
            <HakutulosTable results={results} />
        </div>
    );
}
