import React, { useContext } from 'react';
import { Organisaatio, Kayttaja } from '../types';
import OrganisaatioYhteystiedot from './OrganisaatioYhteystiedot';
import OrganisaatioTiedot from './OrganisaatioTiedot';
import KayttajaYhteystiedot from './KayttajaYhteystiedot';
import { LanguageContext } from '../contexts';
import OrganisaatioSahkopostit from './OrganisaatioSahkopostit';

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
            <fieldset className="oph-fieldset">
                <legend className="oph-label">{i18n.translate('YHTEENVETO')}</legend>
                <div>{i18n.translate('YHTEENVETO_KUVAUS')}</div>
            </fieldset>
            <fieldset className="oph-fieldset">
                <legend className="oph-label">{i18n.translate('ORGANISAATION_TIEDOT')}</legend>
                <OrganisaatioTiedot readOnly={true}
                                    initialOrganisaatio={props.organisaatio}
                                    organisaatio={props.organisaatio}
                                    setOrganisaatio={nop}
                                    errors={nop()} />
            </fieldset>
            <fieldset className="oph-fieldset">
                <legend className="oph-label">{i18n.translate('ORGANISAATION_YHTEYSTIEDOT')}</legend>
                <OrganisaatioYhteystiedot readOnly={true}
                                          initialOrganisaatio={props.organisaatio}
                                          organisaatio={props.organisaatio}
                                          setOrganisaatio={nop}
                                          errors={nop()} />
            </fieldset>
            <fieldset className="oph-fieldset">
                <legend className="oph-label">{i18n.translate('ORGANISAATION_SAHKOPOSTIT')}</legend>
                <OrganisaatioSahkopostit readOnly={true}
                                         sahkopostit={props.sahkopostit}
                                         setSahkopostit={nop}
                                         errors={nop()} />
            </fieldset>
            <fieldset className="oph-fieldset">
                <legend className="oph-label">{i18n.translate('KAYTTAJAN_YHTEYSTIEDOT')}</legend>
                <KayttajaYhteystiedot readOnly={true}
                                      toimintamuoto={props.toimintamuoto}
                                      setToimintamuoto={nop}
                                      kayttaja={props.kayttaja}
                                      setKayttaja={nop}
                                      errors={nop()} />
            </fieldset>
        </form>
    );
}
