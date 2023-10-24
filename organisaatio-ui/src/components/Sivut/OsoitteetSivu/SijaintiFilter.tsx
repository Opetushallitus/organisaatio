import { CustomOption, DropdownOption, SelectDropdown, SelectionItem, SelectionList } from './SelectDropdown';
import { RajausAccordion } from './RajausAccordion';
import styles from './SijaintiFilter.module.css';
import React, { CSSProperties } from 'react';
import { KoodistoKoodi, MaakuntaKoodi } from './OsoitteetApi';
import { KoodiUri } from '../../../types/types';
import Select, { ActionMeta, ValueType } from 'react-select';

type AlueId = 'alue_mannersuomi' | 'alue_kokosuomi' | 'alue_ulkomaa';
type UlkomaaAlue = { id: 'alue_ulkomaa'; label: string };
type MaakuntaAlue = { id: AlueId; label: string; maakunnat: KoodiUri[] };

const MAAKUNTA_AHVENANMAA: KoodiUri = 'maakunta_21';

export type SijaintiFilterValue = {
    maakunnat: KoodiUri[];
    ulkomaa: boolean;
    kunnat: KoodiUri[];
};

export function makeDefaultSearchFilterValue(maakunnat: MaakuntaKoodi[]): SijaintiFilterValue {
    return {
        maakunnat: maakunnat.filter((_) => _.koodiUri !== MAAKUNTA_AHVENANMAA).map((_) => _.koodiUri),
        ulkomaa: false,
        kunnat: [],
    };
}

type SijaintiFilterProps = {
    maakunnat: MaakuntaKoodi[];
    kunnat: KoodistoKoodi[];
    value: SijaintiFilterValue;
    onChange: (value: SijaintiFilterValue) => void;
};

export function SijaintiFilter({ maakunnat, kunnat, value, onChange }: SijaintiFilterProps) {
    const alueMannerSuomi: MaakuntaAlue = {
        id: 'alue_mannersuomi',
        label: 'Manner-Suomi (ei Ahvenanmaa)',
        maakunnat: maakunnat.filter((_) => _.koodiUri !== MAAKUNTA_AHVENANMAA).map((_) => _.koodiUri),
    };
    const alueKokoSuomi: MaakuntaAlue = {
        id: 'alue_kokosuomi',
        label: 'Koko Suomi',
        maakunnat: maakunnat.map((_) => _.koodiUri),
    };
    const ulkomaaAlue: UlkomaaAlue = {
        id: 'alue_ulkomaa',
        label: 'Ulkomaa',
    };

    function buildSelectionDescription(): string {
        return [...buildAlueSelectionDescription(), ...buildKuntaSelectionDescription()].join(', ');
    }

    function buildKuntaSelectionDescription(): string[] {
        const isKuntaChecked = (kunta: KoodistoKoodi): boolean => value.kunnat.includes(kunta.koodiUri);
        return kunnat.filter(isKuntaChecked).map((_) => _.nimi);
    }

    function buildAlueSelectionDescription(): string[] {
        const isMaakuntaChecked = (alue: KoodiUri): boolean => value.maakunnat.includes(alue);
        const isMannerSuomiChecked = alueMannerSuomi.maakunnat.every(isMaakuntaChecked);
        const isKokoSuomiChecked = alueKokoSuomi.maakunnat.every(isMaakuntaChecked);
        const alueNimet = isKokoSuomiChecked
            ? [alueKokoSuomi.label]
            : isMannerSuomiChecked
            ? [alueMannerSuomi.label]
            : [];
        const ulkomaatNames = value.ulkomaa ? [ulkomaaAlue.label] : [];
        // Kun koko suomi tai manner-suomi on valittu ei tarvitse näyttää maakuntia, koska
        // - manner-suomi kattaa kaiken paitsi ahvenanmaan
        // - manner-suomi ja ahvenanmaa == koko suomi
        // - koko suomi on kaikki maakunnat
        const maakuntaNimet =
            !isMannerSuomiChecked && !isKokoSuomiChecked
                ? maakunnat.filter((m) => value.maakunnat.includes(m.koodiUri)).map((m) => m.nimi)
                : [];
        return [...alueNimet, ...ulkomaatNames, ...maakuntaNimet];
    }

    const kuntaOptions: DropdownOption[] = kunnat.map((_) => ({ value: _.koodiUri, label: _.nimi }));

    function kuntaOnChange(selection: string[]) {
        const updatedKunnat = kunnat.filter((_) => selection.includes(_.koodiUri)).map((_) => _.koodiUri);
        onChange({
            ...value,
            kunnat: updatedKunnat,
        });
    }
    return (
        <RajausAccordion header="Sijainti" selectionDescription={buildSelectionDescription()}>
            <div className={styles.TwoColumns}>
                <AlueTaiMaakuntaFilter
                    alueKokoSuomi={alueKokoSuomi}
                    alueMannerSuomi={alueMannerSuomi}
                    ulkomaaAlue={ulkomaaAlue}
                    maakunnat={maakunnat}
                    selectedMaakunnat={value.maakunnat}
                    selectedUlkomaa={value.ulkomaa}
                    onChange={(maakunnat, ulkomaa) => {
                        onChange({
                            ...value,
                            maakunnat,
                            ulkomaa,
                        });
                    }}
                />
                <div className={styles.Column}>
                    <h4>Kunta:</h4>
                    <SelectDropdown
                        label="Hae kunnan nimellä"
                        options={kuntaOptions}
                        classNamePrefix="kunta-react-select"
                        onChange={kuntaOnChange}
                        selections={value.kunnat}
                    />
                </div>
            </div>
        </RajausAccordion>
    );
}

type AlueTaiMaakuntaFilterProps = {
    alueKokoSuomi: MaakuntaAlue;
    alueMannerSuomi: MaakuntaAlue;
    ulkomaaAlue: UlkomaaAlue;
    maakunnat: KoodistoKoodi[];
    selectedMaakunnat: KoodiUri[];
    selectedUlkomaa: boolean;
    onChange: (alueMaakunnat: KoodiUri[], ulkomaat: boolean) => void;
};
function AlueTaiMaakuntaFilter({
    alueKokoSuomi,
    alueMannerSuomi,
    ulkomaaAlue,
    maakunnat,
    selectedMaakunnat,
    selectedUlkomaa,
    onChange,
}: AlueTaiMaakuntaFilterProps) {
    const maakuntaAlueet = [alueKokoSuomi, alueMannerSuomi];
    function alueTaiMaakuntaChecked(value: string, checked: boolean): void {
        if (value === ulkomaaAlue.id) {
            onChange(selectedMaakunnat, checked);
        }
        const isAlue = !!maakuntaAlueet.find((_) => _.id === value);
        const isMaakunta = !!maakunnat.find((_) => _.koodiUri === value);
        if (isAlue) {
            const alue = maakuntaAlueet.find((_) => _.id === value)!;
            const without = selectedMaakunnat.filter((_) => !alue.maakunnat.includes(_));
            if (checked) {
                onChange([...without, ...alue.maakunnat], selectedUlkomaa);
            } else {
                onChange(without, selectedUlkomaa);
            }
        }
        if (isMaakunta) {
            const without = selectedMaakunnat.filter((_) => _ !== value);
            if (checked) {
                onChange([...without, value], selectedUlkomaa);
            } else {
                onChange(without, selectedUlkomaa);
            }
        }
    }

    const isMaakuntaChecked = (maakuntaUri: string): boolean => selectedMaakunnat.includes(maakuntaUri);
    const selectedAlueet = maakuntaAlueet
        .filter((alue) => alue.maakunnat.every(isMaakuntaChecked))
        .map((alue) => alue.id);
    const ulkomaaSelection = selectedUlkomaa ? [ulkomaaAlue.id] : [];
    const selections = [...selectedMaakunnat, ...ulkomaaSelection, ...selectedAlueet];

    function onChecked(value: string, checked: boolean) {
        alueTaiMaakuntaChecked(value, checked);
    }

    function removeSelection(value: string) {
        onChecked(value, false);
    }
    const label = 'Hae alueen tai maakunnan nimellä';
    const groupedOptions = [
        {
            label: 'ALUE',
            options: [...maakuntaAlueet, ulkomaaAlue].map((alue) => ({
                value: alue.id,
                label: alue.label,
            })),
        },
        {
            label: 'MAAKUNTA',
            options: maakunnat.map((maakunta) => ({ value: maakunta.koodiUri, label: maakunta.nimi })),
        },
    ];

    const reactSelectSelection = groupedOptions.flatMap((_) => _.options).filter((v) => selections.includes(v.value));

    // Jos alue on valittu, älä näytä siihen kuuluvia maakuntia
    const isMannerSuomiChecked = alueMannerSuomi.maakunnat.every(isMaakuntaChecked);
    const isKokoSuomiChecked = alueKokoSuomi.maakunnat.every(isMaakuntaChecked);
    const selectionsWithAreasCombined: DropdownOption[] = [
        isKokoSuomiChecked
            ? [{ value: alueKokoSuomi.id, label: alueKokoSuomi.label }]
            : isMannerSuomiChecked
            ? [{ value: alueMannerSuomi.id, label: alueMannerSuomi.label }]
            : groupedOptions[1].options.filter((option) => isMaakuntaChecked(option.value)),
        selectedUlkomaa ? [{ value: ulkomaaAlue.id, label: ulkomaaAlue.label }] : [],
    ].flat();
    return (
        <div className={styles.Column}>
            <h4>Alue tai maakunta:</h4>
            <Select<DropdownOption>
                aria-label={label}
                className={styles.Select}
                escapeClearsValue={false}
                hideSelectedOptions={false}
                components={{ Option: CustomOption }}
                placeholder={label}
                isMulti
                isClearable={false}
                options={groupedOptions}
                classNamePrefix="alue-react-select"
                classNames={{
                    option: styles.ReactSelectOption,
                    groupHeading: styles.ReactSelectGroupHeading,
                }}
                styles={{
                    option: () => ({}),
                    groupHeading: (css: CSSProperties) => ({
                        ...css,
                        borderBottom: '1px solid rgba(153, 153, 153, 0.5018)',
                        marginLeft: '16px',
                        marginRight: '16px',
                        marginBottom: '8px',
                        paddingLeft: '0px',
                        paddingBottom: '6px',
                    }),
                }}
                closeMenuOnSelect={false}
                value={reactSelectSelection}
                onChange={mapReactSelectOnChangeToChecked(alueTaiMaakuntaChecked)}
                backspaceRemovesValue={false}
                controlShouldRenderValue={false}
            />
            <SelectionList>
                {selectionsWithAreasCombined.map((v) => (
                    <SelectionItem key={v.value} value={v.value} label={v.label} onRemove={removeSelection} />
                ))}
            </SelectionList>
        </div>
    );
}
function mapReactSelectOnChangeToChecked(onChecked: (value: string, checked: boolean) => void) {
    return function (_selection: ValueType<DropdownOption>, action: ActionMeta<DropdownOption>) {
        switch (action.action) {
            case 'select-option':
                onChecked(action.option!.value, true);
                break;
            case 'deselect-option':
                onChecked(action.option!.value, false);
                break;
        }
    };
}
