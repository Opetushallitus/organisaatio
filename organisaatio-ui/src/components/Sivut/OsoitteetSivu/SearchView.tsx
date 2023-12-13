import { haeOsoitteet, HaeRequest, HakuParametrit, Hakutulos } from './OsoitteetApi';
import React, { useCallback, useState } from 'react';
import styles from './SearchView.module.css';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import { ErrorBanner } from './ErrorBanner';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';
import { LinklikeButton } from './LinklikeButton';
import { useHistory } from 'react-router-dom';
import { makeDefaultSearchFilterValue, SijaintiFilter, SijaintiFilterValue } from './SijaintiFilter';
import { KoodiUri } from '../../../types/types';
import { JarjestamislupaFilter } from './JarjestamislupaFilter';
import { KieliFilter } from './KieliFilter';
import { KohderyhmaFilter } from './KohderyhmaFilter';
import { OppilaitostyyppiFilter } from './OppilaitostyyppiFilter';

type SearchViewProps = {
    hakuParametrit: HakuParametrit;
    onResult(request: HaeRequest, result: Hakutulos[]): void;
};

type SearchState = {
    oppilaitosTypes: Record<string, boolean>;
    vuosiluokat: string[];
    sijainti: SijaintiFilterValue;
    anyJarjestamislupa: boolean;
    jarjestamisluvat: string[];
    kielet: string[];
    openFilters: string[];
};

export function SearchView({ hakuParametrit, onResult }: SearchViewProps) {
    const [loading, setLoading] = useState<boolean>(false);
    const defaultOppilaitosTypes = hakuParametrit.oppilaitostyypit.koodit.reduce<Record<string, boolean>>((accu, k) => {
        accu[k.koodiUri] = false;
        return accu;
    }, {});

    const defaultSearchState: SearchState = {
        oppilaitosTypes: defaultOppilaitosTypes,
        vuosiluokat: [],
        sijainti: makeDefaultSearchFilterValue(hakuParametrit.maakunnat),
        anyJarjestamislupa: false,
        jarjestamisluvat: [],
        kielet: [],
        openFilters: [],
    };

    const [searchParameters, setSearchParameters] = useUrlHashBackedState<SearchState>(defaultSearchState);
    const { oppilaitosTypes } = searchParameters;
    const [error, setError] = useState<boolean>(false);
    const canFilterByVuosiluokat =
        hakuParametrit.oppilaitostyypit.ryhmat
            .find((_) => _.nimi === 'Perusopetus')
            ?.koodit.some((koodiUri) => oppilaitosTypes[koodiUri]) ?? false;
    if (!canFilterByVuosiluokat && searchParameters.vuosiluokat.length > 0) {
        setSearchParameters({ ...searchParameters, vuosiluokat: [] });
    }

    function resetSearchParams() {
        setSearchParameters(defaultSearchState);
    }

    async function hae() {
        try {
            setLoading(true);
            setError(false);

            const haeOsoitteetRequest = {
                organisaatiotyypit: ['organisaatiotyyppi_01'], // koulutustoimija
                oppilaitostyypit: Object.keys(oppilaitosTypes).reduce<Array<string>>(
                    (accu, key) => (oppilaitosTypes[key] ? accu.concat([key]) : accu),
                    []
                ),
                vuosiluokat: canFilterByVuosiluokat ? searchParameters.vuosiluokat : [],
                kunnat: mapSijaintiFilterToKunnat(hakuParametrit, searchParameters.sijainti),
                anyJarjestamislupa: searchParameters.anyJarjestamislupa,
                jarjestamisluvat: searchParameters.jarjestamisluvat,
                kielet: searchParameters.kielet,
            };
            const osoitteet = await haeOsoitteet(haeOsoitteetRequest);
            onResult(haeOsoitteetRequest, osoitteet);
        } catch (e) {
            setError(true);
        }
        setLoading(false);
    }

    function onSijaintiFilterChanged(sijainti: SijaintiFilterValue) {
        setSearchParameters({ ...searchParameters, sijainti });
    }
    function onJarjestamisluvatFilterChanged(jarjestamisluvat: string[], anyJarjestamislupa: boolean): void {
        setSearchParameters({ ...searchParameters, jarjestamisluvat, anyJarjestamislupa });
    }
    function onKieliFilterChanged(kielet: string[]): void {
        setSearchParameters({ ...searchParameters, kielet });
    }
    function onOppilaitostyyppiFilterChanged(oppilaitosTypes: Record<string, boolean>, vuosiluokat: string[]) {
        setSearchParameters({ ...searchParameters, oppilaitosTypes, vuosiluokat });
    }
    function onToggleOpenFn(filterId: string) {
        return () => {
            const openFilters = isFilterOpen(filterId)
                ? searchParameters.openFilters.filter((_) => _ !== filterId)
                : searchParameters.openFilters.concat(filterId);
            setSearchParameters({ ...searchParameters, openFilters });
        };
    }
    function isFilterOpen(filterId: string) {
        return searchParameters.openFilters.includes(filterId);
    }

    return (
        <div className={styles.SearchView}>
            <div>
                <h1 className={styles.Title}>Osoitepalvelu</h1>
            </div>
            <KohderyhmaFilter />
            <div>
                <div className={styles.SectionTitle}>
                    <h2>Rajaa hakua</h2>
                </div>
                <div className={styles.Rajaukset}>
                    <OppilaitostyyppiFilter
                        hakuParametrit={hakuParametrit}
                        oppilaitosTypes={searchParameters.oppilaitosTypes}
                        vuosiluokat={searchParameters.vuosiluokat}
                        onChange={onOppilaitostyyppiFilterChanged}
                        open={isFilterOpen('oppilaitostyyppi')}
                        onToggleOpen={onToggleOpenFn('oppilaitostyyppi')}
                    />
                    <JarjestamislupaFilter
                        jarjestamisluvat={hakuParametrit.jarjestamisluvat}
                        anyJarjestamislupa={searchParameters.anyJarjestamislupa}
                        value={searchParameters.jarjestamisluvat}
                        onChange={onJarjestamisluvatFilterChanged}
                        open={isFilterOpen('jarjestamislupa')}
                        onToggleOpen={onToggleOpenFn('jarjestamislupa')}
                    />
                    <SijaintiFilter
                        maakunnat={hakuParametrit.maakunnat}
                        kunnat={hakuParametrit.kunnat}
                        value={searchParameters.sijainti}
                        onChange={onSijaintiFilterChanged}
                        open={isFilterOpen('sijainti')}
                        onToggleOpen={onToggleOpenFn('sijainti')}
                    />
                    <KieliFilter
                        kielet={hakuParametrit.kielet}
                        value={searchParameters.kielet}
                        onChange={onKieliFilterChanged}
                        open={isFilterOpen('kielet')}
                        onToggleOpen={onToggleOpenFn('kielet')}
                    />
                </div>
            </div>
            {error && (
                <div className={styles.ErrorRow}>
                    <ErrorBanner onClose={() => setError(false)}>
                        Haun suorituksessa tapahtui virhe. Yritä uudelleen.
                    </ErrorBanner>
                </div>
            )}
            <div className={styles.ButtonRow}>
                <Button onClick={hae}>Hae</Button>
                <LinklikeButton onClick={resetSearchParams}>Tyhjennä</LinklikeButton>
            </div>
            {loading && (
                <div className={styles.LoadingOverlay}>
                    <Spin />
                </div>
            )}
        </div>
    );
}

function useUrlHashBackedState<T>(initialState: T): [T, (newState: T) => void] {
    const history = useHistory();
    const hasStateInUrl = history.location.hash !== '';
    const trueInitialState = hasStateInUrl ? (parseHashState(history.location.hash) as T) : initialState;

    const [state, setState] = useState<T>(trueInitialState);
    const setLocalStorageState: (newState) => void = useCallback((newState) => {
        history.replace({ hash: stringifyHashState(newState) });
        setState(newState);
    }, []);
    return [state, setLocalStorageState];
}

function parseHashState(hash: string) {
    return JSON.parse(decodeURIComponent(hash.substring(1)));
}

function stringifyHashState(state: Record<string, object>) {
    return encodeURIComponent(JSON.stringify(state));
}

function mapSijaintiFilterToKunnat(hakuParametrit: HakuParametrit, sijainti: SijaintiFilterValue): KoodiUri[] {
    const KUNTA_ULKOMAA = 'kunta_200';
    const maakuntaLookup: Map<KoodiUri, KoodiUri[]> = new Map(
        hakuParametrit.maakunnat.map((maakuntaKoodi) => [maakuntaKoodi.koodiUri, maakuntaKoodi.kunnat])
    );
    const maakunnatAsKunnat = sijainti.maakunnat.flatMap((maakuntaUri) => maakuntaLookup.get(maakuntaUri) ?? []);
    const ulkomaaAsKunnat = sijainti.ulkomaa ? [KUNTA_ULKOMAA] : [];
    return unique([...sijainti.kunnat, ...maakunnatAsKunnat, ...ulkomaaAsKunnat]);
}

function unique<T>(elements: T[]) {
    return Array.from(new Set(elements));
}
