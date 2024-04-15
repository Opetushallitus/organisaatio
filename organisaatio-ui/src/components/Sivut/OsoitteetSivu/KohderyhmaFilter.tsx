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

    const isOrganisationSearch = !!value.find(
        (v) => v === 'organisaatiotyyppi_01' || v === 'organisaatiotyyppi_02' || v === 'organisaatiotyyppi_03'
    );
    const isVarhaiskasvatusSearch = !!value.find((v) => v === 'organisaatiotyyppi_07' || v === 'organisaatiotyyppi_08');
    const isPalvelukayttajaSearch = !!value.find((v) => v === 'palveluiden_kayttajat');

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
                        disabled={isPalvelukayttajaSearch || isVarhaiskasvatusSearch}
                    />
                </div>
                <div className={styles.KohderyhmaColumn}>
                    <Kohderyhma
                        title="Oppilaitokset"
                        description="Viralliset yhteystiedot (mm. peruskouluihin, lukioihin, ammatillisiin oppilaitoksiin)"
                        selected={isChecked('organisaatiotyyppi_02')}
                        onChange={onChecked('organisaatiotyyppi_02')}
                        disabled={isPalvelukayttajaSearch || isVarhaiskasvatusSearch}
                    />
                </div>
                <div className={styles.KohderyhmaColumn}>
                    <Kohderyhma
                        title="Oppilaitosten toimipisteet"
                        description="Sivutoimipisteet tai viipalekoulut (osoite eri kuin hallinnollisella oppilaitoksella)"
                        selected={isChecked('organisaatiotyyppi_03')}
                        onChange={onChecked('organisaatiotyyppi_03')}
                        disabled={isPalvelukayttajaSearch || isVarhaiskasvatusSearch}
                    />
                </div>
                <div className={styles.KohderyhmaColumn}>
                    <Kohderyhma
                        title="Varhaiskasvatustoimijat"
                        description="Kunnat, kuntayhtymät tai yksityiset palveluntuottajat"
                        selected={isChecked('organisaatiotyyppi_07')}
                        onChange={onChecked('organisaatiotyyppi_07')}
                        disabled={isPalvelukayttajaSearch || isOrganisationSearch}
                    />
                </div>
                <div className={styles.KohderyhmaColumn}>
                    <Kohderyhma
                        title="Varhaiskasvatuksen toimipaikat"
                        description="Ryhmäperhepäiväkodit ja perhepäivähoitajat eivät ole haettavissa Osoitepalvelussa"
                        selected={isChecked('organisaatiotyyppi_08')}
                        onChange={onChecked('organisaatiotyyppi_08')}
                        disabled={isPalvelukayttajaSearch || isOrganisationSearch}
                    />
                </div>
                <div className={styles.KohderyhmaColumn}>
                    <Kohderyhma
                        title="Palveluiden käyttäjät"
                        description="Virkailijat, joilla on yksi tai useampi käyttöoikeus Opintopolun palveluihin"
                        selected={isChecked('palveluiden_kayttajat')}
                        onChange={onChecked('palveluiden_kayttajat')}
                        disabled={isOrganisationSearch || isVarhaiskasvatusSearch}
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
                <Checkbox checked={selected} onChange={(e) => onChange(e)} disabled={disabled}>
                    <h3>{title}</h3>
                </Checkbox>
            </div>
            <p className={styles.KohderyhmaKuvaus}>{description}</p>
        </div>
    );
}
