import React from 'react';
import KayttajaYhteystiedot from './KayttajaYhteystiedot';
import { Kayttaja } from '../types';

type Props = {
    toimintamuoto: string,
    setToimintamuoto: (toimintamuoto: string) => void,
    kayttaja: Kayttaja,
    setKayttaja: (kayttaja: Partial<Kayttaja>) => void,
    errors: Record<string, string>,
}

export default function RekisterointiKayttaja(props: Props) {
    return (
        <form>
            <fieldset className="oph-fieldset">
                <legend className="oph-label">Varda-pääkäyttäjän yhteystiedot</legend>
                <div>Varda-pääkäyttäjä vastaa organisaaton Varda-käyttöoikeuksista. Lomakkeella ilmoitettu henkilö saa kutsun Varda-pääkäyttäjäksi sen jälkeen, kun kunta on käsittelyt rekisteröitymisen.</div>
                <KayttajaYhteystiedot toimintamuoto={props.toimintamuoto}
                                      setToimintamuoto={props.setToimintamuoto}
                                      kayttaja={props.kayttaja}
                                      setKayttaja={props.setKayttaja}
                                      errors={props.errors} />
            </fieldset>
        </form>
    );
}
