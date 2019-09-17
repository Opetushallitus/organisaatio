import React, { useContext } from 'react';
import { Organisaatio } from '../types';
import OrganisaatioYhteystiedot from './OrganisaatioYhteystiedot';
import OrganisaatioTiedot from './OrganisaatioTiedot';
import { LanguageContext } from '../contexts';
import OrganisaatioSahkopostit from './OrganisaatioSahkopostit';

type Props = {
    initialOrganisaatio: Organisaatio,
    organisaatio: Organisaatio,
    setOrganisaatio: (organisaatio: Partial<Organisaatio>) => void,
    sahkopostit: string[],
    setSahkopostit: (sahkopostit: string[]) => void,
    errors: Record<string, string>,
}

export default function RekisterointiOrganisaatio(props: Props) {
    const { i18n } = useContext(LanguageContext);
    return (
        <form>
            <fieldset className="oph-fieldset">
                <legend className="oph-label">{i18n.translate('ORGANISAATION_TIEDOT')}</legend>
                <div>{i18n.translate('ORGANISAATION_TIEDOT_KUVAUS')}</div>
                <OrganisaatioTiedot initialOrganisaatio={props.initialOrganisaatio}
                                    organisaatio={props.organisaatio}
                                    setOrganisaatio={props.setOrganisaatio}
                                    errors={props.errors} />
            </fieldset>
            <fieldset className="oph-fieldset">
                <legend className="oph-label">{i18n.translate('ORGANISAATION_YHTEYSTIEDOT')}</legend>
                <div>{i18n.translate('ORGANISAATION_YHTEYSTIEDOT_KUVAUS')}</div>
                <OrganisaatioYhteystiedot initialOrganisaatio={props.initialOrganisaatio}
                                          organisaatio={props.organisaatio}
                                          setOrganisaatio={props.setOrganisaatio}
                                          errors={props.errors} />
            </fieldset>
            <fieldset className="oph-fieldset">
                <legend className="oph-label">{i18n.translate('ORGANISAATION_SAHKOPOSTIT')}</legend>
                <div>{i18n.translate('ORGANISAATION_SAHKOPOSTIT_KUVAUS')}</div>
                <OrganisaatioSahkopostit sahkopostit={props.sahkopostit}
                                         setSahkopostit={props.setSahkopostit}
                                         errors={props.errors} />
            </fieldset>
        </form>
    );
}
