import { Hakutulos } from './OsoitteetApi';
import css from './HakutulosView.module.css';
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
        <div className={css.HakutulosView}>
            <div className={css.TitleRow}>
                <div className={css.Title}>
                    <h1 className={css.TitleText}>Hakutulokset</h1>
                    <span className={css.TitleAlt}>( {results.length} )</span>
                </div>
                <Link to={'/osoitteet'}>Muokkaa hakua</Link>
            </div>
            <div className={css.SininenPalkki}>{results.length} hakutulosta valittu</div>
            <HakutulosTable results={results} />
        </div>
    );
}
