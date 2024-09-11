import React from 'react';
import styles from './TaulukkoSivu.module.css';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import { ROOT_OID } from '../../../contexts/constants';
import PohjaSivu from '../PohjaSivu/PohjaSivu';
import OrganisaatioHakuTaulukko from '../../Taulukot/OrganisaatioHakuTaulukko/OrganisaatioHakuTaulukko';
import { useAtom } from 'jotai';
import { casMeAtom } from '../../../api/kayttooikeus';
import { languageAtom } from '../../../api/lokalisaatio';
import { useNavigate } from 'react-router-dom';

const TaulukkoSivu = () => {
    const navigate = useNavigate();
    const handleLisaaUusiToimija = () => {
        return navigate(`/lomake/uusi?parentOid=${ROOT_OID}`);
    };
    const [i18n] = useAtom(languageAtom);
    const [casMe] = useAtom(casMeAtom);

    return (
        <PohjaSivu>
            <div className={styles.PaaOsio}>
                <div className={styles.OtsikkoContainer}>
                    <h2> {i18n.translate('TAULUKKO_ORGANISAATIOT')}</h2>
                    {casMe.canHaveButton('TAULUKKO_LISAA_UUSI_TOIMIJA', '', []) && (
                        <Button style={{ height: '3rem' }} onClick={handleLisaaUusiToimija}>
                            {i18n.translate('TAULUKKO_LISAA_UUSI_TOIMIJA')}
                        </Button>
                    )}
                </div>
                <div className={styles.TaulukkoContainer}>
                    <OrganisaatioHakuTaulukko />
                </div>
            </div>
        </PohjaSivu>
    );
};

export default TaulukkoSivu;
