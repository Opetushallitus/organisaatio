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
import { oppilaitostyyppiKoodistoAtom, organisaatioTyypitKoodistoAtom } from '../../../api/koodisto';
import { SelectOptionType } from '../../../types/types';
import { ValueType } from 'react-select';
import { dropKoodiVersionSuffix } from '../../../tools/mappers';

const ORGANISAATIOTYYPPI_OPPILAITOS_VALUE = 'organisaatiotyyppi_02';

const SEARCH_LENGTH = 3;

type HakufiltteritProps = {
    setOrganisaatiot: (data: OrganisaatioHakuOrganisaatio[]) => void;
    setLoading: (loading: boolean) => void;
};

type HakufiltteritSelectProps = {
    label: string;
    handleSelectChange: (selectOptionType: ValueType<SelectOptionType> | ValueType<SelectOptionType>[]) => void;
    selectOptions: SelectOptionType[];
    disabled?: boolean;
    value: SelectOptionType[];
};

export const mapTyyppiFilter = (
    selectedOptions?: ValueType<SelectOptionType> | ValueType<SelectOptionType>[]
): SelectOptionType[] => {
    return selectedOptions ? ([] as SelectOptionType[]).concat(selectedOptions as SelectOptionType[]) : [];
};

const checkIsOppilaitosTyyppiAllowed = (organisaatioTyyppi: SelectOptionType[] = []) =>
    organisaatioTyyppi.map((ot) => ot.value).includes(ORGANISAATIOTYYPPI_OPPILAITOS_VALUE);

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
    return (
        <div className={styles.Kentta}>
            <label>{label}</label>
            <Select onChange={handleSelectChange} isDisabled={disabled} isMulti value={value} options={selectOptions} />
        </div>
    );
};

export function Hakufiltterit({ setOrganisaatiot, setLoading }: HakufiltteritProps) {
    const [i18n] = useAtom(languageAtom);
    const [remoteFilters, setRemoteFilters] = useAtom(remoteFiltersAtom);
    const [localFilters, setLocalFilters] = useAtom(localFiltersAtom);
    const [organisaatioTyypitKoodisto] = useAtom(organisaatioTyypitKoodistoAtom);
    const [oppilaitosTyypitKoodisto] = useAtom(oppilaitostyyppiKoodistoAtom);
    useEffect(() => {
        if (remoteFilters.searchString.length >= SEARCH_LENGTH) {
            (async () => {
                setLoading(true);
                const searchResult = await searchOrganisation({
                    searchStr: remoteFilters.searchString,
                    lakkautetut: remoteFilters.naytaPassivoidut,
                });
                setOrganisaatiot(enrichWithAllNestedData(searchResult));
                setLoading(false);
            })();
        }
    }, [remoteFilters, setLoading, setOrganisaatiot]);

    const handleOppilaitosTyyppiChange = (values) => {
        setLocalFilters({
            ...localFilters,
            oppilaitosTyyppi: mapTyyppiFilter(values),
        });
    };

    const handleOrganisaatiotyyppiChange = (values) => {
        const organisaatioTyyppi = mapTyyppiFilter(values);
        setLocalFilters({
            ...localFilters,
            organisaatioTyyppi,
            oppilaitosTyyppi: checkIsOppilaitosTyyppiAllowed(organisaatioTyyppi) ? localFilters.oppilaitosTyyppi : [],
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
                        value={localFilters.searchString || ''}
                        onChange={(e) => setLocalFilters({ ...localFilters, searchString: e.target.value })}
                        onKeyDown={(e) => {
                            if (e.key === 'Enter') {
                                setRemoteFilters({ ...remoteFilters, searchString: localFilters.searchString });
                            }
                        }}
                        onBlur={() =>
                            remoteFilters.searchString !== localFilters.searchString &&
                            setRemoteFilters({ ...remoteFilters, searchString: localFilters.searchString })
                        }
                        suffix={
                            localFilters.searchString && (
                                <Button
                                    variant={'text'}
                                    style={{ boxShadow: 'none' }}
                                    onClick={() => {
                                        setLocalFilters({ ...localFilters, searchString: '' });
                                        setRemoteFilters({ ...remoteFilters, searchString: '' });
                                    }}
                                >
                                    <IconWrapper color={'#999999'} icon={clearIcon} />
                                </Button>
                            )
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
                        value={localFilters.organisaatioTyyppi}
                    />
                    <HakuFilterSelect
                        label={i18n.translate('TAULUKKO_OPPILAITOSTYYPPI')}
                        handleSelectChange={handleOppilaitosTyyppiChange}
                        selectOptions={oppilaitosTyypitKoodisto.selectOptions()}
                        disabled={!checkIsOppilaitosTyyppiAllowed(localFilters.organisaatioTyyppi)}
                        value={localFilters.oppilaitosTyyppi}
                    />
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
