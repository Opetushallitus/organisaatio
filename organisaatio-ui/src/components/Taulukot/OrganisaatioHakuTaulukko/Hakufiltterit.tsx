import * as React from 'react';
import { useContext, useEffect } from 'react';
import { LanguageContext } from '../../../contexts/LanguageContext';
import { localFiltersAtom, remoteFiltersAtom } from '../../../contexts/SearchFiltersContext';
import { searchOrganisation } from '../../../api/organisaatio';
import styles from './Hakufiltterit.module.css';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import IconWrapper from '../../IconWapper/IconWrapper';
import clearIcon from '@iconify/icons-fa-solid/times-circle';
import Checkbox from '@opetushallitus/virkailija-ui-components/Checkbox';
import { ApiOrganisaatio } from '../../../types/apiTypes';
import { LISATIEDOT_EXTERNAL_URI } from '../../../contexts/constants';
import { useAtom } from 'jotai';

const SEARCH_LENGTH = 3;
type HakufiltteritProps = {
    setOrganisaatiot: (data: ApiOrganisaatio[]) => void;
    setLoading: (loading: boolean) => void;
};
export function Hakufiltterit({ setOrganisaatiot, setLoading }: HakufiltteritProps) {
    const { i18n } = useContext(LanguageContext);
    const [filters, setFilters] = useAtom(remoteFiltersAtom);
    const [localFilters, setLocalFiltersAtom] = useAtom(localFiltersAtom);
    useEffect(() => {
        if (filters.searchString.length >= SEARCH_LENGTH) {
            (async () => {
                setLoading(true);
                const searchResult = await searchOrganisation({
                    searchStr: filters.searchString,
                    lakkautetut: filters.naytaPassivoidut,
                });
                setOrganisaatiot(searchResult);
                setLoading(false);
            })();
        }
    }, [filters, setLoading, setOrganisaatiot]);
    return (
        <div>
            <div className={styles.FiltteriContainer}>
                <div className={styles.FiltteriInputOsa}>
                    <Input
                        placeholder={i18n.translate('TAULUKKO_TOIMIJA_HAKU_PLACEHOLDER')}
                        value={localFilters.searchString || ''}
                        onChange={(e) => {
                            setLocalFiltersAtom({ ...localFilters, searchString: e.target.value });
                            //setLocalFilters({ ...localFilters, searchString: e.target.value });
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
                                        setLocalFiltersAtom({ ...localFilters, searchString: 'ß' });
                                        //  setLocalFilters({ ...localFilters, searchString: '' });
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
                            setLocalFiltersAtom({ ...localFilters, omatOrganisaatiotSelected: e.target.checked });
                        }}
                    >
                        {i18n.translate('TAULUKKO_CHECKBOX_OMAT_ORGANISAATIOT')}
                    </Checkbox>
                </div>
                <a href={LISATIEDOT_EXTERNAL_URI} className={styles.LisatiedotLinkki}>
                    ?
                </a>
            </div>
        </div>
    );
}
