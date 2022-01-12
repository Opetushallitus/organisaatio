import * as React from 'react';
import { useContext, useEffect, useState } from 'react';
import { LanguageContext } from '../../../contexts/LanguageContext';
import { SearchFilterContext } from '../../../contexts/SearchFiltersContext';
import { Filters } from '../../../types/types';
import { searchOrganisation } from '../../../api/organisaatio';
import styles from './OrganisaatioHakuTaulukko.module.css';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import IconWrapper from '../../IconWapper/IconWrapper';
import clearIcon from '@iconify/icons-fa-solid/times-circle';
import Checkbox from '@opetushallitus/virkailija-ui-components/Checkbox';
import { ApiOrganisaatio } from '../../../types/apiTypes';

const SEARCH_LENGTH = 3;
type HakufiltteritProps = {
    setOrganisaatiot: (data: ApiOrganisaatio[]) => void;
    setLoading: (loading: boolean) => void;
    filterResults: (omatOrganisaatiotSelected: boolean) => void;
};
export function Hakufiltterit({ setOrganisaatiot, setLoading, filterResults }: HakufiltteritProps) {
    const { i18n } = useContext(LanguageContext);
    const { searchFilters } = useContext(SearchFilterContext);
    const [filters, setFilters] = useState<Filters>(searchFilters.filters);
    const [localFilters, setLocalFilters] = useState(searchFilters.localFilters);
    useEffect(() => {
        searchFilters.setFilters(filters);
        if (filters.searchString.length >= SEARCH_LENGTH) {
            (async () => {
                setLoading(true);
                const searchResult = await searchOrganisation({
                    searchStr: filters.searchString,
                    lakkautetut: filters.naytaPassivoidut,
                });
                setOrganisaatiot(searchResult);
                filterResults(searchFilters.localFilters.omatOrganisaatiotSelected);
                setLoading(false);
            })();
        }
    }, [filters, searchFilters, setLoading, setOrganisaatiot]);

    return (
        <div>
            <div className={styles.FiltteriContainer}>
                <div className={styles.FiltteriInputOsa}>
                    <Input
                        placeholder={i18n.translate('TAULUKKO_TOIMIJA_HAKU_PLACEHOLDER')}
                        value={localFilters.searchString || ''}
                        onChange={(e) => {
                            setLocalFilters({ ...localFilters, searchString: e.target.value });
                        }}
                        onKeyDown={(e) => {
                            if (e.key === 'Enter') {
                                setFilters({ ...filters, searchString: localFilters.searchString });
                            }
                        }}
                        suffix={
                            filters.searchString && (
                                <Button
                                    variant={'text'}
                                    style={{ boxShadow: 'none' }}
                                    onClick={() => {
                                        setLocalFilters({ ...localFilters, searchString: '' });
                                        setFilters({ ...filters, searchString: '' });
                                    }}
                                >
                                    <IconWrapper color={'#999999'} icon={clearIcon} />
                                </Button>
                            )
                        }
                    />
                    <Checkbox
                        type={'checkbox'}
                        checked={filters.naytaPassivoidut}
                        onChange={(e) => {
                            setFilters({ ...filters, naytaPassivoidut: e.target.checked });
                        }}
                    >
                        {i18n.translate('TAULUKKO_CHECKBOX_NAYTA_PASSIVOIDUT')}
                    </Checkbox>
                    <Checkbox
                        type={'checkbox'}
                        checked={localFilters.omatOrganisaatiotSelected}
                        onChange={(e) => {
                            setLocalFilters({ ...localFilters, omatOrganisaatiotSelected: e.target.checked });
                            filterResults(e.target.checked);
                        }}
                    >
                        {i18n.translate('TAULUKKO_CHECKBOX_OMAT_ORGANISAATIOT')}
                    </Checkbox>
                </div>
                <Button variant={'outlined'} className={styles.LisatiedotNappi}>
                    ?
                </Button>
            </div>
        </div>
    );
}
