import React, { useMemo } from 'react';

import { ApiResult, Kayttooikeusryhma, Koulutustoimija } from './OsoitteetApi';
import { RajausAccordion } from './RajausAccordion';
import { SearchState } from './SearchView';

import styles from './Filter.module.css';
import { DropdownOption, SelectDropdown } from './SelectDropdown';
import Loading from '../../Loading/Loading';
import { ErrorBanner } from './ErrorBanner';

type KayttajaFilterProps = {
    koulutustoimijat: Koulutustoimija[];
    kayttooikeusryhmat: ApiResult<Kayttooikeusryhma[]>;
    value: Value;
    open: boolean;
    onToggleOpen: () => void;
    onChange: (value: Partial<SearchState>) => void;
    disabled: boolean;
};

export type Value = {
    koulutustoimijat: string[];
    kayttooikeusryhmat: string[];
};

export const emptyValue = () => ({ koulutustoimijat: [], kayttooikeusryhmat: [] });

export const id = 'kayttajat';

export function Element({
    koulutustoimijat,
    kayttooikeusryhmat,
    onChange,
    value,
    open,
    onToggleOpen,
    disabled,
}: KayttajaFilterProps) {
    const kayttooikeusryhmaOptions = useMemo(() => {
        if (kayttooikeusryhmat.state === 'OK') {
            return kayttooikeusryhmat.value
                .map((k) => ({
                    value: k.nimi.texts.find((n) => n.lang === 'FI')?.text,
                    label: k.nimi.texts.find((n) => n.lang === 'FI')?.text,
                }))
                .filter((o) => o.label && o.value) as DropdownOption[];
        } else {
            return [];
        }
    }, [kayttooikeusryhmat.state]);
    const koulutustoimijatOptions = useMemo(() => koulutustoimijat.map((_) => ({ value: _.oid, label: _.nimi })), [
        koulutustoimijat,
    ]);
    const description = useMemo(
        () =>
            [
                ...value.kayttooikeusryhmat,
                value.koulutustoimijat.flatMap((k) => koulutustoimijat.find((v) => v.oid === k)?.nimi).join(', '),
            ]
                .filter(Boolean)
                .join(', '),
        [koulutustoimijat, value.koulutustoimijat, value.kayttooikeusryhmat]
    );

    function onKayttooikeusryhmatChange(kayttooikeusryhmat: string[]) {
        onChange({
            kayttajat: {
                ...value,
                kayttooikeusryhmat,
            },
        });
    }

    function onKoulutustoimijatChange(koulutustoimijat: string[]) {
        onChange({
            kayttajat: {
                ...value,
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
                    <h4>Käyttöoikeusryhmä:</h4>
                    <SelectDropdown
                        label="Hae käyttäjiä käyttöoikeusryhmillä"
                        options={kayttooikeusryhmaOptions}
                        classNamePrefix="kayttooikeusryhma-react-select"
                        onChange={onKayttooikeusryhmatChange}
                        selections={value.kayttooikeusryhmat ?? []}
                        disabled={kayttooikeusryhmat.state !== 'OK'}
                    />
                    {kayttooikeusryhmat.state === 'LOADING' && <Loading />}
                    {kayttooikeusryhmat.state === 'ERROR' && (
                        <ErrorBanner>
                            <span>Käyttöoikeusryhmien haku epäonnistui.</span>
                        </ErrorBanner>
                    )}
                </div>
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
