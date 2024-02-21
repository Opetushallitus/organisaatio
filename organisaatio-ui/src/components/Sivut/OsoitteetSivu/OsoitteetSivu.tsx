import React, { useEffect } from 'react';

import styles from './OsoitteetSivu.module.css';
import { Route, useHistory } from 'react-router-dom';
import { Hakutulos, useHakuParametrit } from './OsoitteetApi';
import { HakutulosView } from './HakutulosView';
import { SearchView } from './SearchView';
import { ViestiView } from './ViestiView';
import Loading from '../../Loading/Loading';
import { ViestiStatusView } from './ViestiStatusView';
import { GenericOsoitepalveluError } from './GenericOsoitepalveluError';

type OsoitteetSivuProps = {
    muotoilematonViestiEnabled: boolean;
};

const OsoitteetSivu = ({ muotoilematonViestiEnabled }: OsoitteetSivuProps) => {
    useTitle('Osoitepalvelu');
    const history = useHistory();
    const hakuParametrit = useHakuParametrit();

    function onSearchResult(hakutulos: Hakutulos) {
        history.push(`/osoitteet/hakutulos/${hakutulos.id}`);
    }

    if (hakuParametrit.state === 'ERROR') {
        return (
            <div className={styles.OsoitteetSivu}>
                <div className={styles.MainContent}>
                    <div className={styles.ContentContainer}>
                        <GenericOsoitepalveluError />
                    </div>
                </div>
            </div>
        );
    }

    if (hakuParametrit.state === 'LOADING') {
        return <Loading />;
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
                        <SearchView hakuParametrit={hakuParametrit.value} onResult={onSearchResult} />
                    </div>
                </div>
            </Route>
            <Route exact path={'/osoitteet/hakutulos/:hakutulosId'}>
                <div className={styles.MainContent}>
                    <div className={styles.WideContentContainer}>
                        <HakutulosView muotoilematonViestiEnabled={muotoilematonViestiEnabled} />
                    </div>
                </div>
            </Route>
            <Route exact path={'/osoitteet/hakutulos/:hakutulosId/viesti'}>
                <div className={styles.MainContent}>
                    <div className={styles.ContentContainer}>
                        <ViestiView></ViestiView>
                    </div>
                </div>
            </Route>
            <Route exact path={'/osoitteet/viesti/:emailId'}>
                <div className={styles.MainContent}>
                    <div className={styles.ContentContainer}>
                        <ViestiStatusView />
                    </div>
                </div>
            </Route>
        </div>
    );
};

function useTitle(title: string) {
    useEffect(() => {
        document.title = title;
    }, []);
}

export default OsoitteetSivu;
