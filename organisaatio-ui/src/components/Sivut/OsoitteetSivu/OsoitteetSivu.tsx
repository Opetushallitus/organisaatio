import React, { useState } from 'react';

import styles from './OsoitteetSivu.module.css';
import { Route, useHistory } from 'react-router-dom';
import { Hakutulos } from './OsoitteetApi';
import { HakutulosView } from './HakutulosView';
import { SearchView } from './SearchView';

const OsoitteetSivu = () => {
    type State = {
        hakutulos?: Hakutulos[];
    };
    const [state, setState] = useState<State>({});
    const history = useHistory();

    function onSearchResult(hakutulos: Hakutulos[]) {
        setState({ hakutulos });
        history.push('/osoitteet/hakutulos');
    }

    return (
        <div className={styles.OsoitteetSivu}>
            <Route exact path={'/osoitteet'}>
                <div className={styles.Header}>
                    <div className={styles.ContentContainer}>
                        <p>
                            Osoitepalveluun kerätään yhteystietoja OPH:n muista palveluista. Yhteystietojen ylläpidosta
                            ja ajantasaisuudesta huolehtivat koulutustoimijoiden virkailijat itse.
                        </p>
                    </div>
                </div>
                <div className={styles.MainContent}>
                    <div className={styles.ContentContainer}>
                        <SearchView onResult={onSearchResult} />
                    </div>
                </div>
            </Route>
            <Route exact path={'/osoitteet/hakutulos'}>
                <div className={styles.MainContent}>
                    <div className={styles.ContentContainer}>
                        <HakutulosView results={state.hakutulos} />
                    </div>
                </div>
            </Route>
        </div>
    );
};

export default OsoitteetSivu;
