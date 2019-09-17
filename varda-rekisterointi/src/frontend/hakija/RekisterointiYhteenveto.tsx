import React, { useContext } from 'react';
import { Organisaatio, Kayttaja } from '../types';
import OrganisaatioYhteystiedot from './OrganisaatioYhteystiedot';
import OrganisaatioTiedot from './OrganisaatioTiedot';
import KayttajaYhteystiedot from './KayttajaYhteystiedot';
import { LanguageContext } from '../contexts';
import OrganisaatioSahkopostit from './OrganisaatioSahkopostit';
import Fieldset from '../Fieldset';

type Props = {
    organisaatio: Organisaatio,
    sahkopostit: string[],
    toimintamuoto: string,
    kayttaja: Kayttaja,
}

function nop() {
    return {};
}

export default function RekisterointiYhteenveto(props: Props) {
    const { i18n } = useContext(LanguageContext);
    return (
        <form>
            <Fieldset title={i18n.translate('YHTEENVETO')}
                      description={i18n.translate('YHTEENVETO_KUVAUS')}>
            </Fieldset>
            <Fieldset title={i18n.translate('ORGANISAATION_TIEDOT')}>
                <OrganisaatioTiedot readOnly={true}
                                    initialOrganisaatio={props.organisaatio}
                                    organisaatio={props.organisaatio}
                                    setOrganisaatio={nop}
                                    errors={nop()} />
            </Fieldset>
            <Fieldset title={i18n.translate('ORGANISAATION_YHTEYSTIEDOT')}>
                <OrganisaatioYhteystiedot readOnly={true}
                                          initialOrganisaatio={props.organisaatio}
                                          organisaatio={props.organisaatio}
                                          setOrganisaatio={nop}
                                          errors={nop()} />
            </Fieldset>
            <Fieldset title={i18n.translate('ORGANISAATION_SAHKOPOSTIT')}>
                <OrganisaatioSahkopostit readOnly={true}
                                         sahkopostit={props.sahkopostit}
                                         setSahkopostit={nop}
                                         errors={nop()} />
            </Fieldset>
            <Fieldset title={i18n.translate('KAYTTAJAN_YHTEYSTIEDOT')}>
                <KayttajaYhteystiedot readOnly={true}
                                      toimintamuoto={props.toimintamuoto}
                                      setToimintamuoto={nop}
                                      kayttaja={props.kayttaja}
                                      setKayttaja={nop}
                                      errors={nop()} />
            </Fieldset>
        </form>
    );
}
