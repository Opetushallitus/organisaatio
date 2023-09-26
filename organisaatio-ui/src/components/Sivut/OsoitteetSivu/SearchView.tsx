import { haeOsoitteet, Hakutulos } from './OsoitteetApi';
import React from 'react';
import styles from './OsoitteetSivu.module.css';
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
        <>
            <h1 className={styles.Title}>Osoitepalvelu</h1>
            <p>
                Osoitepalveluun kerätään yhteystietoja OPH:n muista palveluista. Yhteystietojen ylläpidosta ja
                ajantasaisuudesta huolehtivat koulutustoimijoiden virkailijat itse.
            </p>
            <div className={styles.SubtitleRivi}>
                <h2 className={styles.Subtitle}>
                    Valitse ensin haun kohderyhmä <span>(pakollinen)</span>
                </h2>
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
            <div className={styles.ButtonRow}>
                <Button onClick={hae}>Hae</Button>
                {/*<Button variant={'outlined'}>Tyhjennä</Button>*/}
            </div>
        </>
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
