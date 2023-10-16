import { haeOsoitteet, HakuParametrit, Hakutulos, KoodistoKoodi } from './OsoitteetApi';
import React, { useCallback, useState } from 'react';
import styles from './SearchView.module.css';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import { ErrorBanner } from './ErrorBanner';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';
import { Checkbox } from './Checkbox';
import { LinklikeButton } from './LinklikeButton';
import { useHistory } from 'react-router-dom';
import { SelectDropdown } from './SelectDropdown';

type SearchViewProps = {
    hakuParametrit: HakuParametrit;
    onResult(result: Hakutulos[]): void;
};

type SearchState = {
    oppilaitosTypes: Record<string, boolean>;
    vuosiluokat: string[];
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
    };

    const [searchParameters, setSearchParameters] = useUrlHashBackedState<SearchState>(defaultSearchState);
    const { oppilaitosTypes } = searchParameters;
    const [error, setError] = useState<boolean>(false);
    const canFilterByVuosiluokat =
        hakuParametrit.oppilaitostyypit.ryhmat
            .find((_) => _.nimi === 'Perusopetus')
            ?.koodit.some((koodiUri) => oppilaitosTypes[koodiUri]) ?? false;

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

    function clearOpppilaitostyyppiSelection() {
        const newOppilaitosTypes = Object.fromEntries(Object.entries(oppilaitosTypes).map(([a]) => [a, false]));
        setSearchParameters({ ...searchParameters, oppilaitosTypes: newOppilaitosTypes });
    }

    function toggleAllIsChecked() {
        if (allIsChecked()) {
            clearOpppilaitostyyppiSelection();
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
    function buildSelectionDescription() {
        const s = hakuParametrit.oppilaitostyypit.koodit.reduce<string>((accu, k) => {
            return oppilaitosTypes[k.koodiUri] ? `${accu}${k.nimi}, ` : accu;
        }, '');
        return s.slice(0, s.length - 2);
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
                                    setSearchParameters({ ...searchParameters, oppilaitosTypes: newOppilaitosTypes });
                                };

                                return (
                                    <Checkbox key={nimi} checked={checked} onClick={toggleGroup}>
                                        {nimi}
                                    </Checkbox>
                                );
                            })}
                        </div>
                        <div className={styles.SelectAll}>
                            <Checkbox checked={allIsChecked()} onClick={toggleAllIsChecked}>
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
                                            onClick={() => toggleIsChecked(koodisto.koodiUri)}
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
                {/*<Button variant={'outlined'}>Tyhjennä</Button>*/}
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

type RajausAccordionProps = React.PropsWithChildren<{
    header: string;
    selectionDescription: string;
}>;

function RajausAccordion({ header, selectionDescription, children }: RajausAccordionProps) {
    const [open, setOpen] = useState(false);
    function toggleOpen() {
        setOpen(!open);
    }
    function toggleOpenOnSpaceOrEnter(event) {
        if (event.key === 'Enter' || event.key === ' ') {
            toggleOpen();
        }
    }
    return (
        <section className={styles.RajausAccordion}>
            <div
                tabIndex={0}
                role="button"
                aria-pressed="false"
                className={styles.AccordionTitle}
                onKeyDown={toggleOpenOnSpaceOrEnter}
                onClick={toggleOpen}
            >
                <h3>{header}</h3>
                <span aria-live="off" className={styles.AccordionSelectionDescription}>
                    {selectionDescription}
                </span>
                <AccordionButton open={open} disabled={false} />
            </div>
            {open && (
                <div className={styles.AccordionContentContainer}>
                    <div className={styles.AccordionContent}>{children}</div>
                </div>
            )}
        </section>
    );
}

type AccordionButtonProps = {
    open: boolean;
    disabled: boolean;
};
function AccordionButton({ open, disabled }: AccordionButtonProps) {
    const icon = disabled ? (
        <IconAccordionDisabledButton />
    ) : open ? (
        <IconAccordionCloseButton />
    ) : (
        <IconAccordionOpenButton />
    );
    return <div className={styles.AccordionButton}>{icon}</div>;
}

function IconAccordionOpenButton() {
    return (
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M8 12.5L0 4.49998L1.43333 3.06665L8 9.66665L14.5667 3.09998L16 4.53332L8 12.5Z" fill="#AEAEAE" />
        </svg>
    );
}
function IconAccordionCloseButton() {
    return (
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path
                d="M1.43333 12.5L0 11.0667L8 3.06665L16 11.0333L14.5667 12.4667L8 5.89998L1.43333 12.5Z"
                fill="#AEAEAE"
            />
        </svg>
    );
}

function IconAccordionDisabledButton() {
    return (
        <svg width="16" height="17" viewBox="0 0 16 17" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path
                d="M2.35889 15.1668L1.33325 14.1412L6.97428 8.50016L1.33325 2.85914L2.35889 1.8335L7.99992 7.47452L13.6409 1.8335L14.6666 2.85914L9.02556 8.50016L14.6666 14.1412L13.6409 15.1668L7.99992 9.5258L2.35889 15.1668Z"
                fill="#999999"
            />
        </svg>
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
