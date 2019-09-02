import React, { useContext } from 'react';
import KayttajaYhteystiedot from './KayttajaYhteystiedot';
import { Kayttaja } from '../types';
import { LanguageContext } from '../contexts';

type Props = {
    toimintamuoto: string,
    setToimintamuoto: (toimintamuoto: string) => void,
    kayttaja: Kayttaja,
    setKayttaja: (kayttaja: Partial<Kayttaja>) => void,
    errors: Record<string, string>,
}

export default function RekisterointiKayttaja(props: Props) {
    const { i18n} = useContext(LanguageContext);
    return (
        <form>
            <fieldset className="oph-fieldset">
                <legend className="oph-label">{i18n.translate('KAYTTAJAN_YHTEYSTIEDOT')}</legend>
                <div>{i18n.translate('KAYTTAJAN_YHTEYSTIEDOT_KUVAUS')}</div>
                <KayttajaYhteystiedot toimintamuoto={props.toimintamuoto}
                                      setToimintamuoto={props.setToimintamuoto}
                                      kayttaja={props.kayttaja}
                                      setKayttaja={props.setKayttaja}
                                      errors={props.errors} />
            </fieldset>
        </form>
    );
}
