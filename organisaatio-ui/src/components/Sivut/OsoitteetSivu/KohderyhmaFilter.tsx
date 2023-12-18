import styles from './SearchView.module.css';
import { Checkbox } from './Checkbox';
import React from 'react';

type KohderyhmaFilterProps = {
    value: string[];
    onChange: (value: string[]) => void;
};

export function KohderyhmaFilter({ value, onChange }: KohderyhmaFilterProps) {
    return (
        <div>
            <div className={styles.SectionTitle}>
                <h2>{value.length > 0 ? 'Hae*' : 'Valitse ensin haun kohderyhmä (pakollinen)'}</h2>
                {/*<Button variant={'text'}>Tyhjennä valinnat</Button>*/}
            </div>
            <div className={styles.KohderyhmaSelections}>
                <div className={styles.KohderyhmaColumn}>
                    <Kohderyhma
                        title="Koulutustoimijat"
                        description="Valtion, kunnat, kuntayhtymät, korkeakoulut, yksityiset yhteisöt tai säätiöt"
                        selected={value.includes('organisaatiotyyppi_01')}
                        onChange={(checked) => {
                            if (checked) {
                                onChange(['organisaatiotyyppi_01']);
                            } else {
                                onChange([]);
                            }
                        }}
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
