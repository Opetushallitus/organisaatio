import React, { useEffect, useState } from 'react';

import styles from './OsoitteetSivu.module.css';
import { Route, useHistory } from 'react-router-dom';
import { haeHakuParametrit, HaeRequest, HakuParametrit, Hakutulos } from './OsoitteetApi';
import { HakutulosView } from './HakutulosView';
import { SearchView } from './SearchView';
import { ViestiView } from './ViestiView';
import Loading from '../../Loading/Loading';

type OsoitteetSivuProps = {
    muotoilematonViestiEnabled: boolean;
};

const OsoitteetSivu = ({ muotoilematonViestiEnabled }: OsoitteetSivuProps) => {
    useTitle('Osoitepalvelu');

    type State = {
        request?: HaeRequest;
        hakutulos?: Hakutulos;
    };
    const [hakuParametrit, setHakuParametrit] = useState<HakuParametrit | undefined>(undefined);
    const [state, setState] = useState<State>({});
    const history = useHistory();

    function onSearchResult(request: HaeRequest, hakutulos: Hakutulos) {
        setState({ request, hakutulos });
        history.push('/osoitteet/hakutulos');
    }

    function onWriteMail() {
        history.push('/osoitteet/viesti');
    }

    useEffect(() => {
        haeHakuParametrit().then(setHakuParametrit);
    }, []);

    return hakuParametrit ? (
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
                        <SearchView hakuParametrit={hakuParametrit} onResult={onSearchResult} />
                    </div>
                </div>
            </Route>
            <Route path={'/osoitteet/hakutulos'}>
                <div className={styles.MainContent}>
                    <div className={styles.WideContentContainer}>
                        <HakutulosView
                            muotoilematonViestiEnabled={muotoilematonViestiEnabled}
                            result={state.hakutulos}
                            onWriteMail={onWriteMail}
                        />
                    </div>
                </div>
            </Route>
            <Route exact path={'/osoitteet/viesti'}>
                <div className={styles.MainContent}>
                    <div className={styles.ContentContainer}>
                        <ViestiView></ViestiView>
                    </div>
                </div>
            </Route>
        </div>
    ) : (
        <Loading />
    );
};

function useTitle(title: string) {
    useEffect(() => {
        document.title = title;
    }, []);
}

export default OsoitteetSivu;
