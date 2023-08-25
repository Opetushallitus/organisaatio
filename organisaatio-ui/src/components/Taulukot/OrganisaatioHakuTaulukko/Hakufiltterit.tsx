import * as React from 'react';
import { useEffect, useRef } from 'react';
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
import axios, { CancelTokenSource } from 'axios';
import LoadingBubbles from '../../Loading/LoadingBubbles';

type HakufiltteritProps = {
    setOrganisaatiot: (data: OrganisaatioHakuOrganisaatio[]) => void;
    setLoading: (loading: boolean) => void;
    isLoading: boolean;
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
                    const oppilaitostyyppi = c.oppilaitostyyppi ? [dropKoodiVersionSuffix(c.oppilaitostyyppi)] : [];
                    const [subOrganisaatioTyypit, subOppilaitosTyypit, subOids] = c.subRows
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

export function Hakufiltterit({ setOrganisaatiot, setLoading, isLoading }: HakufiltteritProps) {
    const [i18n] = useAtom(languageAtom);
    const [remoteFilters, setRemoteFilters] = useAtom(remoteFiltersAtom);
    const [localFilters, setLocalFilters] = useAtom(localFiltersAtom);
    const [organisaatioTyypitKoodisto] = useAtom(organisaatioTyypitKoodistoAtom);
    const [oppilaitosTyypitKoodisto] = useAtom(oppilaitostyyppiKoodistoAtom);

    const {
        searchString,
        naytaPassivoidut,
        organisaatiotyyppi,
        oppilaitostyyppi: oppilaitostyyppiWoVersion,
    } = remoteFilters;
    const debouncedSearchString = useDebounce<string>(searchString, 500);
    const searchRef = useRef<string | undefined>();
    const cancelTokenRef = useRef<undefined | CancelTokenSource>();
    useEffect(() => {
        (async () => {
            searchRef.current = debouncedSearchString;
            try {
                setLoading(true);
                cancelTokenRef.current = axios.CancelToken.source();
                const searchResult = await searchOrganisation(
                    {
                        searchStr: debouncedSearchString,
                        lakkautetut: naytaPassivoidut,
                        ...(organisaatiotyyppi ? { organisaatiotyyppi } : {}),
                        ...(oppilaitostyyppiWoVersion ? { oppilaitostyyppi: `${oppilaitostyyppiWoVersion}#1` } : {}),
                        aktiiviset: true,
                        suunnitellut: true,
                    },
                    cancelTokenRef.current.token
                );
                setOrganisaatiot(enrichWithAllNestedData(searchResult));
                setLoading(false);
            } catch (e) {
                console.debug(
                    'Request possibly cancelled due to another search came in while waiting for response, error: ',
                    e
                );
            }
            searchRef.current = debouncedSearchString;
        })();
        return () => {
            cancelTokenRef?.current &&
                cancelTokenRef.current.cancel('Operation canceled due to exiting or another request came.');
        };
    }, [
        debouncedSearchString,
        naytaPassivoidut,
        organisaatiotyyppi,
        oppilaitostyyppiWoVersion,
        setLoading,
        setOrganisaatiot,
    ]);

    const handleOppilaitosTyyppiChange = (value) => {
        const oppilaitostyyppiValue = value?.value || '';
        setRemoteFilters({
            ...remoteFilters,
            oppilaitostyyppi: oppilaitostyyppiValue,
        });
    };

    const handleOrganisaatiotyyppiChange = (value) => {
        const organisaatiotyyppiValue = value?.value || '';
        setRemoteFilters({
            ...remoteFilters,
            organisaatiotyyppi: organisaatiotyyppiValue,
            oppilaitostyyppi:
                organisaatiotyyppiValue === ORGANIAATIOTYYPPI_OPPILAITOS ? remoteFilters.oppilaitostyyppi : '',
        });
    };

    const handleLocalCheckBoxChange = ({ target: { name, checked } }) =>
        setLocalFilters({ ...localFilters, [name]: checked });

    const handleRemoteCheckBoxChange = ({ target: { name, checked } }) =>
        setRemoteFilters({ ...remoteFilters, [name]: checked });

    return (
        <>
            <div className={styles.FiltteriRivi}>
                <div className={styles.InputContainer}>
                    <div>
                        <Input
                            className={styles.SearchInput}
                            placeholder={i18n.translate('TAULUKKO_TOIMIJA_HAKU_PLACEHOLDER')}
                            value={remoteFilters.searchString || ''}
                            onChange={(e) => setRemoteFilters({ ...remoteFilters, searchString: e.target.value })}
                            suffix={
                                <Button
                                    variant={'text'}
                                    style={{ boxShadow: 'none' }}
                                    onClick={() => setRemoteFilters({ ...remoteFilters, searchString: '' })}
                                >
                                    <IconWrapper color={'#999999'} icon={clearIcon} />
                                </Button>
                            }
                        />
                    </div>
                    <div className={styles.LoadingBubblesContainer}>{isLoading && <LoadingBubbles />}</div>
                </div>
                <div>
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
            <div className={styles.FiltteriRivi}>
                <div className={styles.CheckboxContainer}>
                    <Checkbox
                        name={'naytaPassivoidut'}
                        checked={remoteFilters.naytaPassivoidut}
                        onChange={handleRemoteCheckBoxChange}
                        disabled={isLoading}
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
            </div>
        </>
    );
}
