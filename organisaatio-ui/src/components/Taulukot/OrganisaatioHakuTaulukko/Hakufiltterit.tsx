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
    const [remoteFilters, setRemoteFilters] = useAtom(remoteFiltersAtom);
    const [localFilters, setLocalFilters] = useAtom(localFiltersAtom);
    useEffect(() => {
        if (remoteFilters.searchString.length >= SEARCH_LENGTH) {
            (async () => {
                setLoading(true);
                const searchResult = await searchOrganisation({
                    searchStr: remoteFilters.searchString,
                    lakkautetut: remoteFilters.naytaPassivoidut,
                });
                setOrganisaatiot(searchResult);
                setLoading(false);
            })();
        }
    }, [remoteFilters, setLoading, setOrganisaatiot]);
    return (
        <div>
            <div className={styles.FiltteriContainer}>
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
                        onBlur={() => setRemoteFilters({ ...remoteFilters, searchString: localFilters.searchString })}
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
                    <Checkbox
                        type={'checkbox'}
                        checked={remoteFilters.naytaPassivoidut}
                        onChange={(e) => setRemoteFilters({ ...remoteFilters, naytaPassivoidut: e.target.checked })}
                    >
                        {i18n.translate('TAULUKKO_CHECKBOX_NAYTA_PASSIVOIDUT')}
                    </Checkbox>
                    <Checkbox
                        type={'checkbox'}
                        checked={localFilters.omatOrganisaatiotSelected}
                        onChange={(e) =>
                            setLocalFilters({ ...localFilters, omatOrganisaatiotSelected: e.target.checked })
                        }
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
