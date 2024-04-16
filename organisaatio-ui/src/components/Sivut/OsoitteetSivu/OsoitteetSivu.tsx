import React, { useEffect, useState } from 'react';
import { Route, useHistory } from 'react-router-dom';

import { Hakutulos, useHakuParametrit, useKayttooikeusryhmat } from './OsoitteetApi';
import { HakutulosView } from './HakutulosView';
import { SearchView } from './SearchView';
import { ViestiView } from './ViestiView';
import Loading from '../../Loading/Loading';
import { ViestiStatusView } from './ViestiStatusView';
import { GenericOsoitepalveluError } from './GenericOsoitepalveluError';
import { FrontProperties } from '../../../types/types';

import styles from './OsoitteetSivu.module.css';

type OsoitteetSivuProps = {
    frontProperties: FrontProperties;
    muotoilematonViestiEnabled: boolean;
};

const OsoitteetSivu = ({ frontProperties, muotoilematonViestiEnabled }: OsoitteetSivuProps) => {
    useTitle('Osoitepalvelu');
    const history = useHistory();
    const hakuParametrit = useHakuParametrit();
    const kayttooikeusryhmat = useKayttooikeusryhmat(frontProperties);
    const [selection, setSelection] = useState(new Set<string>());

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
                        <SearchView
                            hakuParametrit={hakuParametrit.value}
                            kayttooikeusryhmat={kayttooikeusryhmat}
                            onResult={onSearchResult}
                            setSelection={setSelection}
                        />
                    </div>
                </div>
            </Route>
            <Route exact path={'/osoitteet/hakutulos/:hakutulosId'}>
                <div className={styles.MainContent}>
                    <div className={styles.WideContentContainer}>
                        <HakutulosView
                            muotoilematonViestiEnabled={muotoilematonViestiEnabled}
                            selection={selection}
                            setSelection={setSelection}
                        />
                    </div>
                </div>
            </Route>
            <Route exact path={'/osoitteet/hakutulos/:hakutulosId/viesti'}>
                <div className={styles.MainContent}>
                    <div className={styles.ContentContainer}>
                        <ViestiView selection={selection} setSelection={setSelection} />
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
