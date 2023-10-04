import { haeOsoitteet, HakuParametrit, Hakutulos, Koodisto } from './OsoitteetApi';
import React, { useState } from 'react';
import styles from './SearchView.module.css';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import Checkbox from '@opetushallitus/virkailija-ui-components/Checkbox';
import { ErrorBanner } from './ErrorBanner';

type SearchViewProps = {
    hakuParametrit: HakuParametrit;
    onResult(result: Hakutulos[]): void;
};

export function SearchView({ hakuParametrit, onResult }: SearchViewProps) {
    const a = hakuParametrit.oppilaitostyypit.reduce<Record<string, boolean>>((accu, k) => {
        accu[k.koodiUri] = false;
        return accu;
    }, {});

    const [selectedParameters, setSelectedParameters] = useState<Record<string, boolean>>(a);
    const [error, setError] = useState<boolean>(false);

    async function hae() {
        try {
            setError(false);
            const osoitteet = await haeOsoitteet({
                organisaatiotyypit: ['organisaatiotyyppi_01'], // koulutustoimija
                oppilaitostyypit: Object.keys(selectedParameters).reduce<Array<string>>(
                    (accu, key) => (selectedParameters[key] ? accu.concat([key]) : accu),
                    []
                ),
            });
            onResult(osoitteet);
        } catch (e) {
            setError(true);
        }
    }
    function isChecked(koodiUri: string) {
        return !!selectedParameters[koodiUri];
    }
    function toggleIsChecked(koodiUri: string) {
        const value = { [koodiUri]: !selectedParameters[koodiUri] };
        setSelectedParameters({ ...selectedParameters, ...value });
    }
    function koodistoLexically(left: Koodisto, right: Koodisto) {
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
        const s = hakuParametrit.oppilaitostyypit.reduce<string>((accu, k) => {
            return selectedParameters[k.koodiUri] ? `${accu}${k.nimi}, ` : accu;
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
                        <ul>
                            {hakuParametrit.oppilaitostyypit.sort(koodistoLexically).map((koodisto) => {
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
    return (
        <div className={styles.RajausAccordion}>
            <div className={styles.AccordionTitle} onClick={toggleOpen}>
                <h3>{header}</h3>
                <span className={styles.AccordionSelectionDescription}>{selectionDescription}</span>
                <AccordionButton open={open} disabled={false} />
            </div>
            {open && (
                <div className={styles.AccordionContentContainer}>
                    <div className={styles.AccordionContent}>{children}</div>
                </div>
            )}
        </div>
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
