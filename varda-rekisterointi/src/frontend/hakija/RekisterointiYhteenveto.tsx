import React from 'react';
import { Organisaatio, Kayttaja } from '../types';
import OrganisaatioYhteystiedot from './OrganisaatioYhteystiedot';
import OrganisaatioTiedot from './OrganisaatioTiedot';
import KayttajaYhteystiedot from './KayttajaYhteystiedot';

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
    return (
        <form>
            <fieldset className="oph-fieldset">
                <legend className="oph-label">Yhteenveto</legend>
                <div>Tarkista että tiedot ovat oikein. Jos havaitset antamissasi tiedoissa virheitä, palaa edelliseen vaiheeseen.</div>
            </fieldset>
            <fieldset className="oph-fieldset">
                <legend className="oph-label">Organisaation tiedot</legend>
                <OrganisaatioTiedot readOnly={true}
                                    initialOrganisaatio={props.organisaatio}
                                    organisaatio={props.organisaatio}
                                    setOrganisaatio={nop}
                                    errors={nop()} />
            </fieldset>
            <fieldset className="oph-fieldset">
                <legend className="oph-label">Organisaation yhteystiedot</legend>
                <OrganisaatioYhteystiedot readOnly={true}
                                          initialOrganisaatio={props.organisaatio}
                                          organisaatio={props.organisaatio}
                                          setOrganisaatio={nop}
                                          sahkopostit={props.sahkopostit}
                                          setSahkopostit={nop}
                                          errors={nop()} />
            </fieldset>
            <fieldset className="oph-fieldset">
                <legend className="oph-label">Varda-pääkäyttäjän yhteystiedot</legend>
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
