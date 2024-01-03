import styles from './SearchView.module.css';
import { Checkbox } from './Checkbox';
import React from 'react';

type KohderyhmaFilterProps = {
    value: string[];
    onChange: (value: string[]) => void;
};

export function KohderyhmaFilter({ value, onChange }: KohderyhmaFilterProps) {
    const isChecked = (id: string) => value.includes(id);
    const onChecked = (id: string) => (checked: boolean) => {
        const without = value.filter((_) => _ !== id);
        onChange(checked ? without.concat(id) : without);
    };

    return (
        <div>
            <div className={styles.SectionTitle}>
                <h2>Hae*</h2>
                {/*<Button variant={'text'}>Tyhjennä valinnat</Button>*/}
            </div>
            <div className={styles.KohderyhmaSelections}>
                <div className={styles.KohderyhmaColumn}>
                    <Kohderyhma
                        title="Koulutustoimijat"
                        description="Valtion, kunnat, kuntayhtymät, korkeakoulut, yksityiset yhteisöt tai säätiöt"
                        selected={isChecked('organisaatiotyyppi_01')}
                        onChange={onChecked('organisaatiotyyppi_01')}
                        disabled={false}
                    />
                </div>
                <div className={styles.KohderyhmaColumn}>
                    <Kohderyhma
                        title="Oppilaitokset"
                        description="Viralliset yhteystiedot (mm. peruskouluihin, lukioihin, ammatillisiin oppilaitoksiin)"
                        selected={isChecked('organisaatiotyyppi_02')}
                        onChange={onChecked('organisaatiotyyppi_02')}
                        disabled={false}
                    />
                </div>
                <div className={styles.KohderyhmaColumn}>
                    <Kohderyhma
                        title="Oppilaitosten toimipisteet"
                        description="Sivutoimipisteet tai viipalekoulut (osoite eri kuin hallinnollisella oppilaitoksella)."
                        selected={isChecked('organisaatiotyyppi_03')}
                        onChange={onChecked('organisaatiotyyppi_03')}
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
    onChange: (checked: boolean) => void;
};

function Kohderyhma({ title, description, selected, disabled, onChange }: KohderyhmaProps) {
    const classes = [styles.Kohderyhma];
    if (disabled) {
        classes.push(styles.Disabled);
    } else if (selected) {
        classes.push(styles.Selected);
    }
    return (
        <div className={classes.join(' ')}>
            <div className={styles.KohderyhmaOtsikko}>
                <Checkbox checked={selected} onChange={(value) => onChange(value)}>
                    <h3>{title}</h3>
                </Checkbox>
            </div>
            <p className={styles.KohderyhmaKuvaus}>{description}</p>
        </div>
    );
}
