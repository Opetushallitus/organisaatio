import styles from './SearchView.module.css';
import { LinklikeButton } from './LinklikeButton';
import { Checkbox } from './Checkbox';
import { SelectDropdown } from './SelectDropdown';
import { RajausAccordion } from './RajausAccordion';
import React from 'react';
import { HakuParametrit, KoodistoKoodi, OppilaitosRyhma } from './OsoitteetApi';

type OppilaitostyyppiFilterProps = {
    hakuParametrit: HakuParametrit;
    oppilaitosTypes: Record<string, boolean>;
    vuosiluokat: string[];
    open: boolean;
    onToggleOpen: () => void;
    onChange: (oppilaitosTypes: Record<string, boolean>, vuosiluokat: string[]) => void;
    disabled: boolean;
};

export const id = 'oppilaitostyyppi';

export function Element({
    hakuParametrit,
    oppilaitosTypes,
    vuosiluokat,
    open,
    onToggleOpen,
    onChange,
    disabled,
}: OppilaitostyyppiFilterProps) {
    type SearchParameters = {
        oppilaitosTypes: Record<string, boolean>;
        vuosiluokat: string[];
    };

    function setSearchParameters(state: SearchParameters) {
        onChange(state.oppilaitosTypes, state.vuosiluokat);
    }

    function canFilterByVuosiluokat() {
        return (
            hakuParametrit.oppilaitostyypit.ryhmat
                .find((_) => _.nimi === 'Perusopetus')
                ?.koodit.some((koodiUri) => oppilaitosTypes[koodiUri]) ?? false
        );
    }

    const defaultOppilaitosTypes = hakuParametrit.oppilaitostyypit.koodit.reduce<Record<string, boolean>>((accu, k) => {
        accu[k.koodiUri] = false;
        return accu;
    }, {});

    function buildSelectionDescription(): string {
        const isRyhmaChecked = (ryhma: OppilaitosRyhma): boolean => ryhma.koodit.every(isChecked);
        const checkedRyhmas = hakuParametrit.oppilaitostyypit.ryhmat.filter(isRyhmaChecked);
        const ryhmaNames = checkedRyhmas.map((ryhma) => ryhma.nimi);
        const kooditIncludedInRyhmat = new Set(checkedRyhmas.map((ryhma) => ryhma.koodit).flat());
        const yksittaisetTyypit = hakuParametrit.oppilaitostyypit.koodit
            .filter((k) => isChecked(k.koodiUri) && !kooditIncludedInRyhmat.has(k.koodiUri))
            .map((k) => k.nimi);
        const _vuosiluokat = hakuParametrit.vuosiluokat
            .filter((_) => vuosiluokat.includes(_.koodiUri))
            .map((_) => _.nimi);
        return [...ryhmaNames, ...yksittaisetTyypit, ..._vuosiluokat].join(', ');
    }

    function isChecked(koodiUri: string) {
        return oppilaitosTypes[koodiUri];
    }
    function clearAllSearchSelections() {
        setSearchParameters({ oppilaitosTypes: defaultOppilaitosTypes, vuosiluokat: [] });
    }

    function toggleIsChecked(koodiUri: string) {
        const value = { [koodiUri]: !oppilaitosTypes[koodiUri] };
        setSearchParameters({ oppilaitosTypes: { ...oppilaitosTypes, ...value }, vuosiluokat });
    }

    function allIsChecked() {
        return Object.entries(oppilaitosTypes).every(([, isChecked]) => isChecked);
    }

    function toggleAllIsChecked() {
        if (allIsChecked()) {
            setSearchParameters({ oppilaitosTypes: defaultOppilaitosTypes, vuosiluokat });
        } else {
            const newOppilaitosTypes = Object.fromEntries(Object.entries(oppilaitosTypes).map(([a]) => [a, true]));
            setSearchParameters({ oppilaitosTypes: newOppilaitosTypes, vuosiluokat });
        }
    }

    function koodistoLexically(left: KoodistoKoodi, right: KoodistoKoodi) {
        const l = left.nimi.toUpperCase();
        const r = right.nimi.toUpperCase();
        let o = 0;
        if (l < r) {
            o = -1;
        } else if (l > r) {
            o = 1;
        }
        return o;
    }

    function searchParametersChanged(): boolean {
        return Object.values(oppilaitosTypes).includes(true) || vuosiluokat.length != 0;
    }

    return (
        <RajausAccordion
            header="Oppilaitostyyppi"
            selectionDescription={buildSelectionDescription()}
            open={open}
            onToggleOpen={onToggleOpen}
            disabled={disabled}
        >
            <div className={styles.FlexRow}>
                <h4 className={styles.FlexGrow}>Valitse valmiiden ryhmien mukaan tai yksitellen</h4>
                <LinklikeButton onClick={clearAllSearchSelections} disabled={!searchParametersChanged()}>
                    Tyhjennä valinnat
                </LinklikeButton>
            </div>
            <div className={styles.RajausRyhmat}>
                {hakuParametrit.oppilaitostyypit.ryhmat.map(({ nimi, koodit }) => {
                    const checked = koodit.every(isChecked);
                    const toggleGroup = () => {
                        const newOppilaitosTypes = Object.fromEntries(koodit.map((k) => [k, !checked]));
                        setSearchParameters({
                            oppilaitosTypes: { ...oppilaitosTypes, ...newOppilaitosTypes },
                            vuosiluokat,
                        });
                    };

                    return (
                        <Checkbox key={nimi} checked={checked} onChange={toggleGroup} disabled={disabled}>
                            {nimi}
                        </Checkbox>
                    );
                })}
            </div>
            <div className={styles.SelectAll}>
                <Checkbox checked={allIsChecked()} onChange={toggleAllIsChecked} disabled={disabled}>
                    Valitse kaikki
                </Checkbox>
            </div>
            <ul className={styles.OppilaitostyypitList}>
                {hakuParametrit.oppilaitostyypit.koodit.sort(koodistoLexically).map((koodisto) => {
                    return (
                        <li key={koodisto.koodiUri}>
                            <Checkbox
                                key={koodisto.koodiUri}
                                checked={isChecked(koodisto.koodiUri)}
                                onChange={() => toggleIsChecked(koodisto.koodiUri)}
                                disabled={disabled}
                            >
                                {koodisto.nimi}
                            </Checkbox>
                        </li>
                    );
                })}
            </ul>
            <div className={styles.FlexCol}>
                <h4>Vuosiluokka</h4>
                <SelectDropdown
                    label={'Hae perusopetuksen vuosiluokkatiedolla'}
                    options={hakuParametrit.vuosiluokat.map((v) => ({ value: v.koodiUri, label: v.nimi }))}
                    selections={vuosiluokat}
                    disabled={!canFilterByVuosiluokat()}
                    onChange={(vuosiluokat) => setSearchParameters({ oppilaitosTypes, vuosiluokat })}
                />
            </div>
        </RajausAccordion>
    );
}
