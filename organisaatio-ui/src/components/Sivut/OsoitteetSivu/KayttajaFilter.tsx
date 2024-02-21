import React, { useMemo } from 'react';

import { Koulutustoimija } from './OsoitteetApi';
import { RajausAccordion } from './RajausAccordion';
import { SearchState } from './SearchView';

import styles from './Filter.module.css';
import { SelectDropdown } from './SelectDropdown';

type KayttajaFilterProps = {
    koulutustoimijat: Koulutustoimija[];
    value: Value;
    open: boolean;
    onToggleOpen: () => void;
    onChange: (value: Partial<SearchState>) => void;
    disabled: boolean;
};

export type Value = {
    koulutustoimijat: string[];
};

export function emptyValue() {
    return {
        koulutustoimijat: [],
    };
}

export const id = 'kayttajat';

export function Element({ koulutustoimijat, onChange, value, open, onToggleOpen, disabled }: KayttajaFilterProps) {
    const koulutustoimijatOptions = useMemo(() => koulutustoimijat.map((_) => ({ value: _.oid, label: _.nimi })), [
        koulutustoimijat,
    ]);
    const description = useMemo(
        () => value.koulutustoimijat.flatMap((k) => koulutustoimijat.find((v) => v.oid === k)?.nimi).join(', '),
        [koulutustoimijat, value.koulutustoimijat]
    );

    function onKoulutustoimijatChange(koulutustoimijat: string[]) {
        onChange({
            ...value,
            kayttajat: {
                koulutustoimijat,
            },
        });
    }

    return (
        <RajausAccordion
            header="Palveluiden käyttäjien rajaukset"
            selectionDescription={description}
            open={open}
            onToggleOpen={onToggleOpen}
            disabled={disabled}
        >
            <div className={styles.TwoColumns}>
                <div className={styles.Column}>
                    <h4>Koulutustoimija:</h4>
                    <SelectDropdown
                        label="Hae käyttäjiä koulutustoimijan nimellä"
                        options={koulutustoimijatOptions}
                        classNamePrefix="koulutustoimija-react-select"
                        onChange={onKoulutustoimijatChange}
                        selections={value.koulutustoimijat}
                    />
                </div>
            </div>
        </RajausAccordion>
    );
}
