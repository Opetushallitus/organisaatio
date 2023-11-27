import { haeOsoitteet, HakuParametrit, Hakutulos, KoodistoKoodi, OppilaitosRyhma } from './OsoitteetApi';
import React, { useCallback, useState } from 'react';
import styles from './SearchView.module.css';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import { ErrorBanner } from './ErrorBanner';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';
import { Checkbox } from './Checkbox';
import { LinklikeButton } from './LinklikeButton';
import { useHistory } from 'react-router-dom';
import { SelectDropdown } from './SelectDropdown';
import { RajausAccordion } from './RajausAccordion';
import { makeDefaultSearchFilterValue, SijaintiFilter, SijaintiFilterValue } from './SijaintiFilter';
import { KoodiUri } from '../../../types/types';
import { JarjestamislupaFilter } from './JarjestamislupaFilter';

type SearchViewProps = {
    hakuParametrit: HakuParametrit;
    onResult(result: Hakutulos[]): void;
};

type SearchState = {
    oppilaitosTypes: Record<string, boolean>;
    vuosiluokat: string[];
    sijainti: SijaintiFilterValue;
    anyJarjestamislupa: boolean;
    jarjestamisluvat: string[];
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

            const osoitteet = await haeOsoitteet({
                organisaatiotyypit: ['organisaatiotyyppi_01'], // koulutustoimija
                oppilaitostyypit: Object.keys(oppilaitosTypes).reduce<Array<string>>(
                    (accu, key) => (oppilaitosTypes[key] ? accu.concat([key]) : accu),
                    []
                ),
                vuosiluokat: canFilterByVuosiluokat ? searchParameters.vuosiluokat : [],
                kunnat: mapSijaintiFilterToKunnat(hakuParametrit, searchParameters.sijainti),
                anyJarjestamislupa: searchParameters.anyJarjestamislupa,
                jarjestamisluvat: searchParameters.jarjestamisluvat,
            });
            onResult(osoitteet);
        } catch (e) {
            setError(true);
        }
        setLoading(false);
    }
    function isChecked(koodiUri: string) {
        return oppilaitosTypes[koodiUri];
    }

    function allIsChecked() {
        return Object.entries(oppilaitosTypes).every(([, isChecked]) => isChecked);
    }

    function toggleIsChecked(koodiUri: string) {
        const value = { [koodiUri]: !oppilaitosTypes[koodiUri] };
        setSearchParameters({ ...searchParameters, oppilaitosTypes: { ...oppilaitosTypes, ...value } });
    }

    function toggleAllIsChecked() {
        if (allIsChecked()) {
            setSearchParameters({ ...searchParameters, oppilaitosTypes: defaultOppilaitosTypes });
        } else {
            const newOppilaitosTypes = Object.fromEntries(Object.entries(oppilaitosTypes).map(([a]) => [a, true]));
            setSearchParameters({ ...searchParameters, oppilaitosTypes: newOppilaitosTypes });
        }
    }

    function searchParametersChanged(): boolean {
        return (
            Object.values(searchParameters.oppilaitosTypes).includes(true) || searchParameters.vuosiluokat.length != 0
        );
    }
    function clearAllSearchSelections() {
        setSearchParameters(defaultSearchState);
    }

    function koodistoLexically(left: KoodistoKoodi, right: KoodistoKoodi) {
        const l = left.nimi.toUpperCase();
        const r = right.nimi.toUpperCase();
        let o = 0;
        if (l < r) {
            o = -1;
        } else if (l > r) {
            o = 1;
        }
        return o;
    }

    function onSijaintiFilterChanged(sijainti: SijaintiFilterValue) {
        setSearchParameters({ ...searchParameters, sijainti });
    }
    function onJarjestamisluvatFilterChanged(jarjestamisluvat: string[], anyJarjestamislupa: boolean): void {
        setSearchParameters({ ...searchParameters, jarjestamisluvat, anyJarjestamislupa });
    }

    function buildSelectionDescription(): string {
        const isRyhmaChecked = (ryhma: OppilaitosRyhma): boolean => ryhma.koodit.every(isChecked);
        const checkedRyhmas = hakuParametrit.oppilaitostyypit.ryhmat.filter(isRyhmaChecked);
        const ryhmaNames = checkedRyhmas.map((ryhma) => ryhma.nimi);
        const kooditIncludedInRyhmat = new Set(checkedRyhmas.map((ryhma) => ryhma.koodit).flat());
        const yksittaisetTyypit = hakuParametrit.oppilaitostyypit.koodit
            .filter((k) => isChecked(k.koodiUri) && !kooditIncludedInRyhmat.has(k.koodiUri))
            .map((k) => k.nimi);
        const vuosiluokat = hakuParametrit.vuosiluokat
            .filter((_) => searchParameters.vuosiluokat.includes(_.koodiUri))
            .map((_) => _.nimi);
        return [...ryhmaNames, ...yksittaisetTyypit, ...vuosiluokat].join(', ');
    }

    return (
        <div className={styles.SearchView}>
            <div>
                <h1 className={styles.Title}>Osoitepalvelu</h1>
            </div>
            <div>
                <div className={styles.SectionTitle}>
                    <h2>Valitse ensin haun kohderyhmä (pakollinen)</h2>
                    {/*<Button variant={'text'}>Tyhjennä valinnat</Button>*/}
                </div>
                <div className={styles.KohderyhmaSelections}>
                    <div className={styles.KohderyhmaColumn}>
                        <Kohderyhma
                            title="Koulutustoimijat"
                            description="Valtion, kunnat, kuntayhtymät, korkeakoulut, yksityiset yhteisöt tai säätiöt"
                            selected={true}
                            disabled={false}
                        />
                    </div>
                </div>
            </div>
            <div>
                <div className={styles.SectionTitle}>
                    <h2>Rajaa hakua</h2>
                </div>
                <div className={styles.Rajaukset}>
                    <RajausAccordion header="Oppilaitostyyppi" selectionDescription={buildSelectionDescription()}>
                        <div className={styles.FlexRow}>
                            <h4 className={styles.FlexGrow}>Valitse valmiiden ryhmien mukaan tai yksitellen</h4>
                            <LinklikeButton onClick={clearAllSearchSelections} disabled={!searchParametersChanged()}>
                                Tyhjennä valinnat
                            </LinklikeButton>
                        </div>
                        <div className={styles.RajausRyhmat}>
                            {hakuParametrit.oppilaitostyypit.ryhmat.map(({ nimi, koodit }) => {
                                const checked = koodit.every(isChecked);
                                const toggleGroup = () => {
                                    const newOppilaitosTypes = Object.fromEntries(koodit.map((k) => [k, !checked]));
                                    setSearchParameters({
                                        ...searchParameters,
                                        oppilaitosTypes: { ...oppilaitosTypes, ...newOppilaitosTypes },
                                    });
                                };

                                return (
                                    <Checkbox key={nimi} checked={checked} onChange={toggleGroup}>
                                        {nimi}
                                    </Checkbox>
                                );
                            })}
                        </div>
                        <div className={styles.SelectAll}>
                            <Checkbox checked={allIsChecked()} onChange={toggleAllIsChecked}>
                                Valitse kaikki
                            </Checkbox>
                        </div>
                        <ul>
                            {hakuParametrit.oppilaitostyypit.koodit.sort(koodistoLexically).map((koodisto) => {
                                return (
                                    <li key={koodisto.koodiUri}>
                                        <Checkbox
                                            key={koodisto.koodiUri}
                                            checked={isChecked(koodisto.koodiUri)}
                                            onChange={() => toggleIsChecked(koodisto.koodiUri)}
                                        >
                                            {koodisto.nimi}
                                        </Checkbox>
                                    </li>
                                );
                            })}
                        </ul>
                        <div className={styles.FlexCol}>
                            <h4>Vuosiluokka</h4>
                            <SelectDropdown
                                label={'Hae perusopetuksen vuosiluokkatiedolla'}
                                options={hakuParametrit.vuosiluokat.map((v) => ({ value: v.koodiUri, label: v.nimi }))}
                                selections={searchParameters.vuosiluokat}
                                disabled={!canFilterByVuosiluokat}
                                onChange={(vuosiluokat) => setSearchParameters({ ...searchParameters, vuosiluokat })}
                            />
                        </div>
                    </RajausAccordion>
                    <JarjestamislupaFilter
                        jarjestamisluvat={hakuParametrit.jarjestamisluvat}
                        anyJarjestamislupa={searchParameters.anyJarjestamislupa}
                        value={searchParameters.jarjestamisluvat}
                        onChange={onJarjestamisluvatFilterChanged}
                    />
                    <SijaintiFilter
                        maakunnat={hakuParametrit.maakunnat}
                        kunnat={hakuParametrit.kunnat}
                        value={searchParameters.sijainti}
                        onChange={onSijaintiFilterChanged}
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

type KohderyhmaProps = {
    title: string;
    description: string;
    selected: boolean;
    disabled: boolean;
};

function Kohderyhma({ title, description, selected, disabled }: KohderyhmaProps) {
    const classes = [styles.Kohderyhma];
    if (disabled) {
        classes.push(styles.Disabled);
    } else if (selected) {
        classes.push(styles.Selected);
    }
    return (
        <div className={classes.join(' ')}>
            <div className={styles.KohderyhmaOtsikko}>
                <Checkbox checked={selected} />
                <h3>{title}</h3>
            </div>
            <p className={styles.KohderyhmaKuvaus}>{description}</p>
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
