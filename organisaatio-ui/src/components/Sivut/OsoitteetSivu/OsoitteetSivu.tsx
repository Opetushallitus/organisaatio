import React, { useState } from 'react';
import PohjaSivu from '../PohjaSivu/PohjaSivu';
import { PaaOsio } from '../LomakeSivu/LomakeFields/LomakeFields';

import css from './OsoitteetSivu.module.css';
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
        <PohjaSivu backgroundColor="#f0f3f7">
            <PaaOsio>
                <div className={css.container}>
                    <Route exact path={'/osoitteet'}>
                        <SearchView onResult={onSearchResult} />
                    </Route>
                    <Route exact path={'/osoitteet/hakutulos'}>
                        <HakutulosView results={state.hakutulos} />
                    </Route>
                </div>
            </PaaOsio>
        </PohjaSivu>
    );
};

export default OsoitteetSivu;
