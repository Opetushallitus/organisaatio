import React from 'react';
import useAxios from 'axios-hooks';
import FormFieldContainer from '../FormFieldContainer';
import { Kayttaja, KoodiUri, Koodi } from '../types';
import Select from '../Select';
import KoodiSelectRadio from '../KoodiSelectRadio';
import Spinner from '../Spinner';
import { asiointikielet } from '../LocalizableTextUtils';

type Props = {
    readOnly?: boolean,
    toimintamuoto: string,
    setToimintamuoto: (toimintamuoto: KoodiUri) => void,
    kayttaja: Kayttaja,
    setKayttaja: (kayttaja: Partial<Kayttaja>) => void,
}

export default function KayttajaYhteystiedot({readOnly, toimintamuoto, setToimintamuoto, kayttaja, setKayttaja}: Props) {
    const [{data: toimintamuodot, loading: toimintamuodotLoading, error: toimintamuodotError}]
        = useAxios<Koodi[]>('/varda-rekisterointi/api/koodisto/VARDA_TOIMINTAMUOTO/koodi');

    if (toimintamuodotLoading) {
        return <Spinner />;
    }
    if (toimintamuodotError) {
        return <div>error, reload page</div>;
    }

    return (
        <>
            <FormFieldContainer label="Varhaiskasvatustoimija on" required={!readOnly}>
                <div className="oph-input-container">
                    <KoodiSelectRadio selectable={toimintamuodot}
                                      selected={toimintamuoto}
                                      disabled={readOnly}
                                      onChange={setToimintamuoto} />
                </div>
            </FormFieldContainer>
            <FormFieldContainer label="Etunimi" labelFor="etunimi" required={!readOnly}>
                <input className="oph-input"
                       type="text"
                       id="etunimi"
                       value={kayttaja.etunimi}
                       disabled={readOnly}
                       onChange={event => setKayttaja({ etunimi: event.currentTarget.value })} />
            </FormFieldContainer>
            <FormFieldContainer label="Sukunimi" labelFor="sukunimi" required={!readOnly}>
                <input className="oph-input"
                       type="text"
                       id="sukunimi"
                       value={kayttaja.sukunimi}
                       disabled={readOnly}
                       onChange={event => setKayttaja({ sukunimi: event.currentTarget.value })} />
            </FormFieldContainer>
            <FormFieldContainer label="Sähköposti" labelFor="sahkoposti" required={!readOnly}>
                <input className="oph-input"
                       type="text"
                       id="sahkoposti"
                       value={kayttaja.sahkoposti}
                       disabled={readOnly}
                       onChange={event => setKayttaja({ sahkoposti: event.currentTarget.value })} />
            </FormFieldContainer>
            <FormFieldContainer label="Asiointikieli" labelFor="asiointikieli" required={!readOnly}>
                <div className="oph-input-container">
                    <Select id="asiointikieli"
                            selectable={asiointikielet}
                            selected={kayttaja.asiointikieli}
                            disabled={readOnly}
                            onChange={asiointikieli => setKayttaja({ asiointikieli: asiointikieli })} />
                </div>
            </FormFieldContainer>
            <FormFieldContainer label="Saateteksti" labelFor="saateteksti">
                <textarea className="oph-input"
                          id="saateteksti"
                          value={kayttaja.saateteksti}
                          disabled={readOnly}
                          onChange={event => setKayttaja({ saateteksti: event.currentTarget.value })}></textarea>
            </FormFieldContainer>
        </>
    );
}
