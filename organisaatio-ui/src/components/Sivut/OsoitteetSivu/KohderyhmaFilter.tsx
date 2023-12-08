import styles from './SearchView.module.css';
import { Checkbox } from './Checkbox';
import React from 'react';

export function KohderyhmaFilter() {
    return (
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
