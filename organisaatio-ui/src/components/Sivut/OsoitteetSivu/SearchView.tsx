import { ApiResult, haeHakutulos, HaeRequest, HakuParametrit, Hakutulos, Kayttooikeusryhma } from './OsoitteetApi';
import React, { useCallback, useMemo, useState } from 'react';
import styles from './SearchView.module.css';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import { ErrorBanner } from './ErrorBanner';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';
import { LinklikeButton } from './LinklikeButton';
import { useLocation, useNavigate } from 'react-router-dom';
import * as sijaintiFilter from './SijaintiFilter';
import { KoodiUri } from '../../../types/types';
import * as jarjestamislupaFilter from './JarjestamislupaFilter';
import * as kieliFilter from './KieliFilter';
import * as kayttajaFilter from './KayttajaFilter';
import { KohderyhmaFilter } from './KohderyhmaFilter';
import * as oppilaitostyyppiFilter from './OppilaitostyyppiFilter';

type SearchViewProps = {
    hakuParametrit: HakuParametrit;
    kayttooikeusryhmat: ApiResult<Kayttooikeusryhma[]>;
    onResult(result: Hakutulos): void;
    setSelection: (s: Set<string>) => void;
    setHakutulosCache: (h: Hakutulos) => void;
};

export type SearchState = {
    kohderyhmat: string[];
    oppilaitosTypes: Record<string, boolean>;
    vuosiluokat: string[];
    sijainti: sijaintiFilter.Value;
    anyJarjestamislupa: boolean;
    jarjestamisluvat: string[];
    kielet: string[];
    kayttajat: kayttajaFilter.Value;
    openFilters: string[];
    enabledFilters: string[];
};

export function SearchView({
    hakuParametrit,
    kayttooikeusryhmat,
    onResult,
    setSelection,
    setHakutulosCache,
}: SearchViewProps) {
    const [loading, setLoading] = useState<boolean>(false);
    const defaultOppilaitosTypes = hakuParametrit.oppilaitostyypit.koodit.reduce<Record<string, boolean>>((accu, k) => {
        accu[k.koodiUri] = false;
        return accu;
    }, {});

    const defaultSearchState = deriveStateFromKohderyhmat(['organisaatiotyyppi_01']);
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

    const isMissingRequiredKayttajaFilters = useMemo(() => {
        return (
            searchParameters.kohderyhmat.includes('palveluiden_kayttajat') &&
            !Object.keys(oppilaitosTypes).reduce<Array<string>>(
                (accu, key) => (oppilaitosTypes[key] ? accu.concat([key]) : accu),
                []
            ).length &&
            !searchParameters.kayttajat.koulutustoimijat.length &&
            !searchParameters.kayttajat.kayttooikeusryhmat.length
        );
    }, [searchParameters.kayttajat, searchParameters.oppilaitosTypes, searchParameters.kohderyhmat]);

    function resetSearchParams() {
        setSearchParameters({
            kohderyhmat: [],
            oppilaitosTypes: defaultOppilaitosTypes,
            vuosiluokat: [],
            sijainti: sijaintiFilter.makeEmptyValue(),
            anyJarjestamislupa: false,
            jarjestamisluvat: [],
            kielet: [],
            kayttajat: kayttajaFilter.emptyValue(),
            enabledFilters: [],
            openFilters: [],
        });
    }

    async function hae() {
        try {
            setLoading(true);
            setError(false);

            setSelection(new Set());
            const request: HaeRequest = {
                organisaatiotyypit: searchParameters.kohderyhmat,
                oppilaitostyypit: Object.keys(oppilaitosTypes).reduce<Array<string>>(
                    (accu, key) => (oppilaitosTypes[key] ? accu.concat([key]) : accu),
                    []
                ),
                vuosiluokat: canFilterByVuosiluokat ? searchParameters.vuosiluokat : [],
                kunnat: mapSijaintiFilterToKunnat(hakuParametrit, searchParameters.sijainti),
                anyJarjestamislupa: searchParameters.anyJarjestamislupa,
                jarjestamisluvat: searchParameters.jarjestamisluvat,
                kielet: searchParameters.kielet,
                organisaatioOids: searchParameters.kayttajat.koulutustoimijat,
                kayttooikeusryhmat: searchParameters.kayttajat.kayttooikeusryhmat,
            };

            const hakutulos = await haeHakutulos(request);
            setHakutulosCache(hakutulos);
            onResult(hakutulos);
        } catch (e) {
            setError(true);
        }
        setLoading(false);
    }

    function onSijaintiFilterChanged(sijainti: sijaintiFilter.Value) {
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
    function onKohderymaFilterChanged(kohderyhmat: string[]): void {
        setSearchStateAsDervivedFromKohderyhmat(kohderyhmat);
    }
    function onFilterChange(change: Partial<SearchState>) {
        setSearchParameters({ ...searchParameters, ...change });
    }
    function setSearchStateAsDervivedFromKohderyhmat(kohderyhmat: string[]) {
        setSearchParameters(deriveStateFromKohderyhmat(kohderyhmat, searchParameters));
    }
    function deriveStateFromKohderyhmat(kohderyhmat: string[], currentState?: SearchState): SearchState {
        const enabledFilters = deriveEnabledFilters(kohderyhmat);
        const openFilters = currentState?.openFilters.filter((f) => enabledFilters.includes(f)) ?? [];
        const oppilaitosTypes = enabledFilters.includes(oppilaitostyyppiFilter.id)
            ? currentState?.oppilaitosTypes ?? defaultOppilaitosTypes
            : defaultOppilaitosTypes;
        const vuosiluokat = enabledFilters.includes(oppilaitostyyppiFilter.id) ? currentState?.vuosiluokat ?? [] : [];
        const sijainti = enabledFilters.includes(sijaintiFilter.id)
            ? sijaintiFilter.isEmptyValue(currentState?.sijainti)
                ? sijaintiFilter.makeDefaultValue(hakuParametrit.maakunnat)
                : currentState?.sijainti ?? sijaintiFilter.makeDefaultValue(hakuParametrit.maakunnat)
            : sijaintiFilter.makeEmptyValue();
        const anyJarjestamislupa = enabledFilters.includes(jarjestamislupaFilter.id)
            ? currentState?.anyJarjestamislupa ?? false
            : false;
        const jarjestamisluvat = enabledFilters.includes(jarjestamislupaFilter.id)
            ? currentState?.jarjestamisluvat ?? []
            : [];
        const kielet = enabledFilters.includes(kieliFilter.id)
            ? !currentState?.kielet.length
                ? kieliFilter.makeDefaultValue()
                : currentState?.kielet ?? kieliFilter.makeDefaultValue()
            : [];

        return {
            kohderyhmat,
            oppilaitosTypes,
            vuosiluokat,
            sijainti: enabledFilters.length ? sijainti : sijaintiFilter.makeDefaultValue(hakuParametrit.maakunnat),
            anyJarjestamislupa,
            jarjestamisluvat,
            kielet: enabledFilters.length ? kielet : kieliFilter.makeDefaultValue(),
            kayttajat: kayttajaFilter.emptyValue(),
            enabledFilters,
            openFilters,
        };
    }

    function enabledFiltersByKohderyhma(o: string) {
        if (o == 'organisaatiotyyppi_01') {
            return [oppilaitostyyppiFilter.id, jarjestamislupaFilter.id, sijaintiFilter.id, kieliFilter.id];
        } else if (o == 'organisaatiotyyppi_02' || o == 'organisaatiotyyppi_03') {
            return [oppilaitostyyppiFilter.id, sijaintiFilter.id, kieliFilter.id];
        } else if (o == 'organisaatiotyyppi_07' || o == 'organisaatiotyyppi_08') {
            return [sijaintiFilter.id, kieliFilter.id];
        } else if (o === 'palveluiden_kayttajat') {
            return [oppilaitostyyppiFilter.id, kayttajaFilter.id];
        } else {
            return [];
        }
    }

    function deriveEnabledFilters(kohderyhmat: string[]) {
        return kohderyhmat.map(enabledFiltersByKohderyhma).flat();
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

    function isFilterEnabled(filterId: string) {
        return searchParameters.enabledFilters.includes(filterId);
    }

    function searchIsEnabled() {
        return !isMissingRequiredKayttajaFilters && searchParameters.kohderyhmat.length > 0;
    }

    return (
        <div className={styles.SearchView}>
            <div>
                <h1 className={styles.Title}>Osoitepalvelu</h1>
            </div>
            <KohderyhmaFilter value={searchParameters.kohderyhmat} onChange={onKohderymaFilterChanged} />
            <div>
                <div className={styles.SectionTitle}>
                    <h2>Haun rajausmahdollisuudet</h2>
                </div>
                <div className={styles.Rajaukset}>
                    <oppilaitostyyppiFilter.Element
                        hakuParametrit={hakuParametrit}
                        oppilaitosTypes={searchParameters.oppilaitosTypes}
                        vuosiluokat={searchParameters.vuosiluokat}
                        onChange={onOppilaitostyyppiFilterChanged}
                        open={isFilterOpen(oppilaitostyyppiFilter.id)}
                        onToggleOpen={onToggleOpenFn(oppilaitostyyppiFilter.id)}
                        disabled={!isFilterEnabled(oppilaitostyyppiFilter.id)}
                    />
                    <jarjestamislupaFilter.Element
                        jarjestamisluvat={hakuParametrit.jarjestamisluvat}
                        anyJarjestamislupa={searchParameters.anyJarjestamislupa}
                        value={searchParameters.jarjestamisluvat}
                        onChange={onJarjestamisluvatFilterChanged}
                        open={isFilterOpen(jarjestamislupaFilter.id)}
                        onToggleOpen={onToggleOpenFn(jarjestamislupaFilter.id)}
                        disabled={!isFilterEnabled(jarjestamislupaFilter.id)}
                    />
                    <sijaintiFilter.Element
                        maakunnat={hakuParametrit.maakunnat}
                        kunnat={hakuParametrit.kunnat}
                        value={searchParameters.sijainti}
                        onChange={onSijaintiFilterChanged}
                        open={isFilterOpen(sijaintiFilter.id)}
                        onToggleOpen={onToggleOpenFn(sijaintiFilter.id)}
                        disabled={!isFilterEnabled(sijaintiFilter.id)}
                    />
                    <kieliFilter.Element
                        kielet={hakuParametrit.kielet}
                        value={searchParameters.kielet}
                        onChange={onKieliFilterChanged}
                        open={isFilterOpen(kieliFilter.id)}
                        onToggleOpen={onToggleOpenFn(kieliFilter.id)}
                        disabled={!isFilterEnabled(kieliFilter.id)}
                    />
                    <kayttajaFilter.Element
                        koulutustoimijat={hakuParametrit.koulutustoimijat}
                        kayttooikeusryhmat={kayttooikeusryhmat}
                        value={searchParameters.kayttajat}
                        onChange={onFilterChange}
                        open={isFilterOpen(kayttajaFilter.id)}
                        onToggleOpen={onToggleOpenFn(kayttajaFilter.id)}
                        disabled={!isFilterEnabled(kayttajaFilter.id)}
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
                <Button onClick={hae} disabled={!searchIsEnabled()}>
                    Hae
                </Button>
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
    const navigate = useNavigate();
    const location = useLocation();
    const hasStateInUrl = location.hash !== '';
    const trueInitialState = hasStateInUrl ? (parseHashState(location.hash) as T) : initialState;

    const [state, setState] = useState<T>(trueInitialState);
    const setLocalStorageState: (newState) => void = useCallback((newState) => {
        navigate({ hash: stringifyHashState(newState) }, { replace: true });
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

function mapSijaintiFilterToKunnat(hakuParametrit: HakuParametrit, sijainti: sijaintiFilter.Value): KoodiUri[] {
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
