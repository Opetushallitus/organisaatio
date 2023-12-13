import React from 'react';
import { KoodistoKoodi } from './OsoitteetApi';
import { RajausAccordion } from './RajausAccordion';
import { Checkbox } from './Checkbox';
import styles from './KieliFilter.module.css';

type KieliFilterProps = {
    kielet: KoodistoKoodi[];
    value: string[];
    open: boolean;
    onToggleOpen: () => void;
    onChange: (value: string[]) => void;
};
export function KieliFilter({ value, kielet, open, onToggleOpen, onChange }: KieliFilterProps) {
    function buildSelectionDescription() {
        const isKieliChecked = (_: KoodistoKoodi): boolean => value.includes(_.koodiUri);
        return kielet
            .filter(isKieliChecked)
            .map((_) => _.nimi)
            .join(', ');
    }

    const isChecked = (uri: string) => value.includes(uri);
    const onChecked = (uri: string, checked: boolean) => {
        const without = value.filter((_) => _ !== uri);
        onChange(checked ? without.concat(uri) : without);
    };

    return (
        <RajausAccordion
            header="Organisaation kieli"
            selectionDescription={buildSelectionDescription()}
            open={open}
            onToggleOpen={onToggleOpen}
        >
            <h4>Valitse organisaatiot, joiden kieli on:</h4>
            <div className={styles.KieletList}>
                {kielet.map((koodisto) => {
                    return (
                        <div key={koodisto.koodiUri}>
                            <Checkbox
                                key={koodisto.koodiUri}
                                checked={isChecked(koodisto.koodiUri)}
                                onChange={(checked) => onChecked(koodisto.koodiUri, checked)}
                            >
                                {koodisto.nimi}
                            </Checkbox>
                        </div>
                    );
                })}
            </div>
        </RajausAccordion>
    );
}
