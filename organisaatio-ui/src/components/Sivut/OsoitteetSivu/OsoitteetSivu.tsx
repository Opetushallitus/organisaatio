import React, { useEffect, useState } from 'react';
import { Route, Routes, useNavigate } from 'react-router-dom';

import {
    Hakutulos,
    HakutulosRow,
    KayttajaHakutulosRow,
    useHakuParametrit,
    useKayttooikeusryhmat,
} from './OsoitteetApi';
import { HakutulosView } from './HakutulosView';
import { SearchView } from './SearchView';
import { ViestiView } from './ViestiView';
import Loading from '../../Loading/Loading';
import { ViestiStatusView } from './ViestiStatusView';
import { GenericOsoitepalveluError } from './GenericOsoitepalveluError';
import { FrontProperties } from '../../../types/types';

import styles from './OsoitteetSivu.module.css';
import VirheSivu from '../VirheSivu/VirheSivu';

type OsoitteetSivuProps = {
    frontProperties: FrontProperties;
};

const OsoitteetSivu = ({ frontProperties }: OsoitteetSivuProps) => {
    useTitle('Osoitepalvelu');
    const navigate = useNavigate();
    const hakuParametrit = useHakuParametrit();
    const kayttooikeusryhmat = useKayttooikeusryhmat(frontProperties);
    const [hakutulosCache, setHakutulosCache] = useState<Hakutulos>();
    const [selection, setSelection] = useState(new Set<string>());

    useEffect(() => {
        if (hakutulosCache) {
            setSelection(new Set(hakutulosCache?.rows.map((r: HakutulosRow | KayttajaHakutulosRow) => r.oid)));
        }
    }, [hakutulosCache]);

    function onSearchResult(hakutulos: Hakutulos) {
        navigate(`/osoitteet/hakutulos/${hakutulos.id}`);
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
            <Routes>
                <Route
                    path="/"
                    element={
                        <>
                            <div className={styles.Header}>
                                <div className={styles.ContentContainer}>
                                    <p>
                                        Osoitepalveluun kerätään yhteystietoja OPH:n muista palveluista. Yhteystietojen
                                        ylläpidosta ja ajantasaisuudesta huolehtivat koulutustoimijoiden virkailijat
                                        itse.
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
                                        setHakutulosCache={setHakutulosCache}
                                    />
                                </div>
                            </div>
                        </>
                    }
                />
                <Route
                    path="hakutulos/:hakutulosId"
                    element={
                        <div className={styles.MainContent}>
                            <div className={styles.WideContentContainer}>
                                <HakutulosView
                                    selection={selection}
                                    setSelection={setSelection}
                                    hakutulosCache={hakutulosCache}
                                />
                            </div>
                        </div>
                    }
                />
                <Route
                    path="hakutulos/:hakutulosId/viesti"
                    element={
                        <div className={styles.MainContent}>
                            <div className={styles.ContentContainer}>
                                <ViestiView selection={selection} setSelection={setSelection} />
                            </div>
                        </div>
                    }
                />
                <Route
                    path="viesti/:emailId"
                    element={
                        <div className={styles.MainContent}>
                            <div className={styles.ContentContainer}>
                                <ViestiStatusView />
                            </div>
                        </div>
                    }
                />
                <Route path="*" element={<VirheSivu>{'ERROR_404'}</VirheSivu>} />
            </Routes>
        </div>
    );
};

function useTitle(title: string) {
    useEffect(() => {
        document.title = title;
    }, []);
}

export default OsoitteetSivu;
