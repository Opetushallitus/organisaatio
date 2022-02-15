import * as React from 'react';
import { useEffect } from 'react';
import { localFiltersAtom, remoteFiltersAtom } from '../../../contexts/SearchFiltersContext';
import { searchOrganisation } from '../../../api/organisaatio';
import styles from './Hakufiltterit.module.css';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import IconWrapper from '../../IconWapper/IconWrapper';
import clearIcon from '@iconify/icons-fa-solid/times-circle';
import Checkbox from '@opetushallitus/virkailija-ui-components/Checkbox';
import { OrganisaatioHakuOrganisaatio } from '../../../types/apiTypes';
import { LISATIEDOT_EXTERNAL_URI } from '../../../contexts/constants';
import { useAtom } from 'jotai';
import { languageAtom } from '../../../api/lokalisaatio';
import Select from '@opetushallitus/virkailija-ui-components/Select';
import {
    oppilaitostyyppiKoodistoAtom,
    ORGANIAATIOTYYPPI_OPPILAITOS,
    organisaatioTyypitKoodistoAtom,
} from '../../../api/koodisto';
import { SelectOptionType } from '../../../types/types';
import { ValueType } from 'react-select';
import { dropKoodiVersionSuffix } from '../../../tools/mappers';
import useDebounce from '../../../tools/useDebounce';

type HakufiltteritProps = {
    setOrganisaatiot: (data: OrganisaatioHakuOrganisaatio[]) => void;
    setLoading: (loading: boolean) => void;
};

type HakufiltteritSelectProps = {
    label: string;
    handleSelectChange: (value: ValueType<SelectOptionType>) => void;
    selectOptions: SelectOptionType[];
    disabled?: boolean;
    value: string;
};

export const enrichWithAllNestedData = (
    data: OrganisaatioHakuOrganisaatio[],
    parentOrganisaatioTypes: string[] = [],
    parentOpilaitosTypes: string[] = [],
    parentOids: string[] = []
): OrganisaatioHakuOrganisaatio[] => {
    const tableDataEnricher = (organisaatioData): [string[], string[], string[]] =>
        organisaatioData
            .reduce(
                (prev: [string[], string[], string[]], c: OrganisaatioHakuOrganisaatio) => {
                    const [prevOrganisaatioTyypit, prevOppilaitosTyypit, prevOids] = prev;
                    const organisaatiotyypit = c.organisaatiotyypit || [];
                    const oppilaitostyyppi = !!c.oppilaitostyyppi ? [dropKoodiVersionSuffix(c.oppilaitostyyppi)] : [];
                    const [subOrganisaatioTyypit, subOppilaitosTyypit, subOids] = !!c.subRows
                        ? tableDataEnricher(c.subRows)
                        : [[], [], []];
                    return [
                        [
                            ...prevOrganisaatioTyypit,
                            ...subOrganisaatioTyypit,
                            ...organisaatiotyypit,
                            ...parentOrganisaatioTypes,
                        ],
                        [...prevOppilaitosTyypit, ...subOppilaitosTyypit, ...oppilaitostyyppi, ...parentOpilaitosTypes],
                        [...prevOids, ...subOids, c.oid, ...c.parentOidPath.split('/'), ...parentOids],
                    ];
                },
                [[], [], []]
            )
            .map((arr) => [...new Set(arr)]);
    return data.map((organisaatio) => {
        const [allOrganisaatioTyypit, allOppilaitosTyypit, allOids] = tableDataEnricher([organisaatio]);
        return {
            ...organisaatio,
            subRows: enrichWithAllNestedData(organisaatio.subRows, allOrganisaatioTyypit, allOppilaitosTyypit, allOids),
            allOrganisaatioTyypit,
            allOppilaitosTyypit,
            allOids,
        };
    });
};

const HakuFilterSelect = (props: HakufiltteritSelectProps) => {
    const { label, handleSelectChange, selectOptions, disabled = false, value } = props;
    const selectedOption = selectOptions.find((option) => option?.value === value);
    return (
        <div className={styles.Kentta}>
            <label>{label}</label>
            <Select
                onChange={handleSelectChange}
                isDisabled={disabled}
                value={selectedOption || null}
                options={selectOptions}
            />
        </div>
    );
};

export function Hakufiltterit({ setOrganisaatiot, setLoading }: HakufiltteritProps) {
    const [i18n] = useAtom(languageAtom);
    const [remoteFilters, setRemoteFilters] = useAtom(remoteFiltersAtom);
    const [localFilters, setLocalFilters] = useAtom(localFiltersAtom);
    const [organisaatioTyypitKoodisto] = useAtom(organisaatioTyypitKoodistoAtom);
    const [oppilaitosTyypitKoodisto] = useAtom(oppilaitostyyppiKoodistoAtom);

    const { searchString, naytaPassivoidut, organisaatiotyyppi, oppilaitostyyppi } = remoteFilters;
    const debouncedSearchString = useDebounce<string>(searchString, 500);

    useEffect(() => {
        (async () => {
            setLoading(true);
            const searchResult = await searchOrganisation({
                searchStr: debouncedSearchString,
                lakkautetut: naytaPassivoidut,
                ...(organisaatiotyyppi ? { organisaatiotyyppi } : {}),
                ...(oppilaitostyyppi ? { oppilaitostyyppi } : {}),
                aktiiviset: true,
                suunnitellut: true,
            });
            setOrganisaatiot(enrichWithAllNestedData(searchResult));
            setLoading(false);
        })();
    }, [debouncedSearchString, naytaPassivoidut, organisaatiotyyppi, oppilaitostyyppi, setLoading, setOrganisaatiot]);

    const handleOppilaitosTyyppiChange = (value) => {
        const oppilaitostyyppi = value?.value || '';
        setRemoteFilters({
            ...remoteFilters,
            oppilaitostyyppi,
        });
    };

    const handleOrganisaatiotyyppiChange = (value) => {
        const organisaatiotyyppi = value?.value || '';
        setRemoteFilters({
            ...remoteFilters,
            organisaatiotyyppi,
            oppilaitostyyppi: organisaatiotyyppi === ORGANIAATIOTYYPPI_OPPILAITOS ? remoteFilters.oppilaitostyyppi : '',
        });
    };

    const handleLocalCheckBoxChange = ({ target: { name, checked } }) =>
        setLocalFilters({ ...localFilters, [name]: checked });

    const handleRemoteCheckBoxChange = ({ target: { name, checked } }) =>
        setRemoteFilters({ ...remoteFilters, [name]: checked });

    return (
        <div>
            <div className={styles.FiltteriRivi}>
                <div className={styles.FiltteriInputOsa}>
                    <Input
                        placeholder={i18n.translate('TAULUKKO_TOIMIJA_HAKU_PLACEHOLDER')}
                        value={remoteFilters.searchString || ''}
                        onChange={(e) => {
                            setRemoteFilters({ ...remoteFilters, searchString: e.target.value });
                        }}
                        suffix={
                            <Button
                                variant={'text'}
                                style={{ boxShadow: 'none' }}
                                onClick={() => {
                                    if (remoteFilters.searchString) {
                                        setRemoteFilters({ ...remoteFilters, searchString: '' });
                                    }
                                }}
                            >
                                <IconWrapper color={'#999999'} icon={clearIcon} />
                            </Button>
                        }
                    />
                    <div className={styles.CheckboxContainer}>
                        <Checkbox
                            name={'naytaPassivoidut'}
                            checked={remoteFilters.naytaPassivoidut}
                            onChange={handleRemoteCheckBoxChange}
                        >
                            {i18n.translate('TAULUKKO_CHECKBOX_NAYTA_PASSIVOIDUT')}
                        </Checkbox>
                        <Checkbox
                            name={'omatOrganisaatiotSelected'}
                            checked={localFilters.omatOrganisaatiotSelected}
                            onChange={handleLocalCheckBoxChange}
                        >
                            {i18n.translate('TAULUKKO_CHECKBOX_OMAT_ORGANISAATIOT')}
                        </Checkbox>
                        <Checkbox
                            name={'showVakaToimijat'}
                            checked={localFilters.showVakaToimijat}
                            onChange={handleLocalCheckBoxChange}
                        >
                            {i18n.translate('TAULUKKO_CHECKBOX_NAYTA_VAKA_TOIMIJAT')}
                        </Checkbox>
                    </div>
                </div>
            </div>
            <div className={styles.FiltteriRivi}>
                <div className={styles.DropdownContainer}>
                    <HakuFilterSelect
                        label={i18n.translate('TAULUKKO_ORGANISAATIOTYYPPI')}
                        handleSelectChange={handleOrganisaatiotyyppiChange}
                        selectOptions={organisaatioTyypitKoodisto.selectOptions()}
                        value={remoteFilters.organisaatiotyyppi}
                    />
                    <HakuFilterSelect
                        label={i18n.translate('TAULUKKO_OPPILAITOSTYYPPI')}
                        handleSelectChange={handleOppilaitosTyyppiChange}
                        selectOptions={oppilaitosTyypitKoodisto.selectOptions()}
                        disabled={organisaatiotyyppi !== ORGANIAATIOTYYPPI_OPPILAITOS}
                        value={remoteFilters.oppilaitostyyppi}
                    />
                    <div className={styles.TyhjennaNappiKentta}>
                        <Button
                            color={'secondary'}
                            variant={'outlined'}
                            onClick={() =>
                                setRemoteFilters({ ...remoteFilters, organisaatiotyyppi: '', oppilaitostyyppi: '' })
                            }
                        >
                            {i18n.translate('ORGANISAATIOHAKUTAULUKKO_TYHJENNA')}
                        </Button>
                    </div>
                </div>
                <div className={styles.LisatiedotLinkkiKentta}>
                    <a
                        href={LISATIEDOT_EXTERNAL_URI}
                        target={'_blank'}
                        rel={'noreferrer'}
                        className={styles.LisatiedotLinkki}
                    >
                        ?
                    </a>
                </div>
            </div>
        </div>
    );
}
