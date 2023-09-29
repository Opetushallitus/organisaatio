import { haeOsoitteet, Hakutulos } from './OsoitteetApi';
import React, { useState } from 'react';
import styles from './SearchView.module.css';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import Checkbox from '@opetushallitus/virkailija-ui-components/Checkbox';

type SearchViewProps = {
    onResult(result: Hakutulos[]): void;
};
export function SearchView({ onResult }: SearchViewProps) {
    async function hae() {
        const osoitteet = await haeOsoitteet();
        // TODO: Virheilmoitus
        onResult(osoitteet);
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
                    <RajausAccordion />
                </div>
            </div>
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

function RajausAccordion() {
    const [open, setOpen] = useState(false);
    function toggleOpen() {
        setOpen(!open);
    }
    return (
        <div className={styles.RajausAccordion}>
            <div className={styles.AccordionTitle} onClick={toggleOpen}>
                <h3>Oppilatostyyppi</h3>
                <span>Sähköpostiosoite</span>
                <AccordionButton open={open} disabled={false} />
            </div>
            {open && (
                <div className={styles.AccordionContentContainer}>
                    <div className={styles.AccordionContent}>oben :DD</div>
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
