import React, { useContext } from 'react';
import KayttajaYhteystiedot from './KayttajaYhteystiedot';
import { Kayttaja } from '../types';
import { LanguageContext } from '../contexts';
import Fieldset from '../Fieldset';

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
            <Fieldset title={i18n.translate('KAYTTAJAN_YHTEYSTIEDOT')}
                      description={[i18n.translate('KAYTTAJAN_YHTEYSTIEDOT_KUVAUS'), i18n.translate('KAYTTAJAN_YHTEYSTIEDOT_KUVAUS2')]}>
                <KayttajaYhteystiedot toimintamuoto={props.toimintamuoto}
                                      setToimintamuoto={props.setToimintamuoto}
                                      kayttaja={props.kayttaja}
                                      setKayttaja={props.setKayttaja}
                                      errors={props.errors} />
            </Fieldset>
        </form>
    );
}
