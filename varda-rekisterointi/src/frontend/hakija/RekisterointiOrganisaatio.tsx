import React, { useContext } from 'react';
import { Organisaatio } from '../types';
import OrganisaatioYhteystiedot from './OrganisaatioYhteystiedot';
import OrganisaatioTiedot from './OrganisaatioTiedot';
import { LanguageContext } from '../contexts';
import OrganisaatioSahkopostit from './OrganisaatioSahkopostit';
import Fieldset from '../Fieldset';

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
            <Fieldset title={i18n.translate('ORGANISAATION_TIEDOT')}
                      description={i18n.translate('ORGANISAATION_TIEDOT_KUVAUS')}>
                <OrganisaatioTiedot initialOrganisaatio={props.initialOrganisaatio}
                                    organisaatio={props.organisaatio}
                                    setOrganisaatio={props.setOrganisaatio}
                                    errors={props.errors} />
            </Fieldset>
            <Fieldset title={i18n.translate('ORGANISAATION_YHTEYSTIEDOT')}
                      description={i18n.translate('ORGANISAATION_YHTEYSTIEDOT_KUVAUS')}>
                <OrganisaatioYhteystiedot initialOrganisaatio={props.initialOrganisaatio}
                                          organisaatio={props.organisaatio}
                                          setOrganisaatio={props.setOrganisaatio}
                                          errors={props.errors} />
            </Fieldset>
            <Fieldset title={i18n.translate('ORGANISAATION_SAHKOPOSTIT')}
                      description={i18n.translate('ORGANISAATION_SAHKOPOSTIT_KUVAUS')}>
                <OrganisaatioSahkopostit sahkopostit={props.sahkopostit}
                                         setSahkopostit={props.setSahkopostit}
                                         errors={props.errors} />
            </Fieldset>
        </form>
    );
}
