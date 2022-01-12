import React, { useContext } from 'react';
import styles from './TaulukkoSivu.module.css';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import { ROOT_OID } from '../../../contexts/constants';
import PohjaSivu from '../PohjaSivu/PohjaSivu';
import OrganisaatioHakuTaulukko from '../../Taulukot/OrganisaatioHakuTaulukko/OrganisaatioHakuTaulukko';
import { LanguageContext } from '../../../contexts/LanguageContext';
import { CasMeContext } from '../../../contexts/CasMeContext';

const TaulukkoSivu = (props) => {
    const handleLisaaUusiToimija = () => {
        return props.history.push(`/lomake/uusi?parentOid=${ROOT_OID}`);
    };
    const { i18n } = useContext(LanguageContext);
    const { me: casMe } = useContext(CasMeContext);

    return (
        <PohjaSivu>
            <div className={styles.PaaOsio}>
                <div className={styles.OtsikkoContainer}>
                    <h2> {i18n.translate('TAULUKKO_ORGANISAATIOT')}</h2>
                    {casMe.canHaveButton('TAULUKKO_LISAA_UUSI_TOIMIJA', '', []) && (
                        <Button style={{ height: '3rem' }} onClick={handleLisaaUusiToimija}>
                            {' '}
                            + {i18n.translate('TAULUKKO_LISAA_UUSI_TOIMIJA')}
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
