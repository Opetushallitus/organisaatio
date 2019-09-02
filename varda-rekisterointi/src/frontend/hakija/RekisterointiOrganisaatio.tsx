import React from 'react';
import { Organisaatio } from '../types';
import OrganisaatioYhteystiedot from './OrganisaatioYhteystiedot';
import OrganisaatioTiedot from './OrganisaatioTiedot';

type Props = {
    initialOrganisaatio: Organisaatio,
    organisaatio: Organisaatio,
    setOrganisaatio: (organisaatio: Partial<Organisaatio>) => void,
    sahkopostit: string[],
    setSahkopostit: (sahkopostit: string[]) => void,
    errors: Record<string, string>,
}

export default function RekisterointiOrganisaatio(props: Props) {
    return (
        <form>
            <fieldset className="oph-fieldset">
                <legend className="oph-label">Organisaation tiedot</legend>
                <div>Tarkista että tiedot ovat oikein. Jos esitäytetyissä tiedoissa on virheitä, tiedot tulee päivittää itse Yritys- ja yhteisötietojärjestelmään.</div>
                <OrganisaatioTiedot initialOrganisaatio={props.initialOrganisaatio}
                                    organisaatio={props.organisaatio}
                                    setOrganisaatio={props.setOrganisaatio}
                                    errors={props.errors} />
            </fieldset>
            <fieldset className="oph-fieldset">
                <legend className="oph-label">Organisaation yhteystiedot</legend>
                <div>Tarkista että tiedot ovat oikein ja täytä tarvittavat kohdat ennen jatkamista.</div>
                <OrganisaatioYhteystiedot initialOrganisaatio={props.initialOrganisaatio}
                                          organisaatio={props.organisaatio}
                                          setOrganisaatio={props.setOrganisaatio}
                                          sahkopostit={props.sahkopostit}
                                          setSahkopostit={props.setSahkopostit}
                                          errors={props.errors} />
            </fieldset>
        </form>
    );
}
