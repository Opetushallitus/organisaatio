import React from 'react';
import { RajausAccordion } from './RajausAccordion';
import { KoodistoKoodi } from './OsoitteetApi';
import { Checkbox } from './Checkbox';
import { SelectDropdown } from './SelectDropdown';
import styles from './SearchView.module.css';

type ValueType = string[];

type JarjestamislupaFilterProps = {
    jarjestamisluvat: KoodistoKoodi[];
    onChange: (jarjestamisluvat: ValueType, anyJarjestamislupa: boolean) => void;
    value: ValueType;
    anyJarjestamislupa: boolean;
    open: boolean;
    onToggleOpen: () => void;
    disabled: boolean;
};

export function JarjestamislupaFilter({
    jarjestamisluvat,
    value,
    anyJarjestamislupa,
    onChange,
    open,
    onToggleOpen,
    disabled,
}: JarjestamislupaFilterProps) {
    const options = jarjestamisluvat.map((koodi) => ({
        value: koodi.koodiUri,
        label: koodi.nimi,
    }));
    function buildSelectionDescription() {
        if (anyJarjestamislupa) {
            return 'Kaikki koulutustoimijat, joilla voimassa oleva järjestämislupa';
        } else {
            const isJarjestamislupaChecked = (jarjestamislupa: KoodistoKoodi): boolean =>
                value.includes(jarjestamislupa.koodiUri);
            return jarjestamisluvat
                .filter(isJarjestamislupaChecked)
                .map((_) => _.nimi)
                .join(', ');
        }
    }

    function jarjestamisluvatOnChange(selection: string[]) {
        const updatedJarjestamisluvat = jarjestamisluvat
            .filter((_) => selection.includes(_.koodiUri))
            .map((_) => _.koodiUri);
        onChange(updatedJarjestamisluvat, anyJarjestamislupa);
    }

    return (
        <RajausAccordion
            header="Ammatillisen koulutuksen järjestämislupa"
            selectionDescription={buildSelectionDescription()}
            open={open}
            onToggleOpen={onToggleOpen}
            disabled={disabled}
        >
            <div>
                <Checkbox checked={anyJarjestamislupa} onChange={(checked) => onChange(checked ? [] : value, checked)}>
                    Kaikki koulutustoimijat, joilla voimassa oleva järjestämislupa
                </Checkbox>
            </div>
            <div className={styles.FlexCol}>
                <h4>Järjestämislupa:</h4>
                <SelectDropdown
                    disabled={anyJarjestamislupa}
                    label="Hae yksittäisten tutkintojen ja koulutusten nimillä"
                    options={options}
                    classNamePrefix="jarjestamislupa-react-select"
                    onChange={jarjestamisluvatOnChange}
                    selections={value}
                />
            </div>
        </RajausAccordion>
    );
}
