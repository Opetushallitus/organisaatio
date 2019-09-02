import React, { useContext } from 'react';
import useAxios from 'axios-hooks';
import FormFieldContainer from '../FormFieldContainer';
import { Kayttaja, KoodiUri, Koodi } from '../types';
import Select from '../Select';
import KoodiSelectRadio from '../KoodiSelectRadio';
import Spinner from '../Spinner';
import { asiointikielet } from '../LocalizableTextUtils';
import classNames from 'classnames/bind';
import { LanguageContext } from '../contexts';

type Props = {
    readOnly?: boolean,
    toimintamuoto: string,
    setToimintamuoto: (toimintamuoto: KoodiUri) => void,
    kayttaja: Kayttaja,
    setKayttaja: (kayttaja: Partial<Kayttaja>) => void,
    errors: Record<string, string>,
}

export default function KayttajaYhteystiedot({readOnly, toimintamuoto, setToimintamuoto, kayttaja, setKayttaja, errors}: Props) {
    const { i18n } = useContext(LanguageContext);
    const [{data: toimintamuodot, loading: toimintamuodotLoading, error: toimintamuodotError}]
        = useAxios<Koodi[]>('/varda-rekisterointi/api/koodisto/VARDA_TOIMINTAMUOTO/koodi');

    if (toimintamuodotLoading) {
        return <Spinner />;
    }
    if (toimintamuodotError) {
        return <div>error, reload page</div>;
    }

    const baseClasses = { 'oph-input': true };

    return (
        <>
            <FormFieldContainer label={i18n.translate('VARHAISKASVATUSTOIMIJA')} required={!readOnly}>
                <div className="oph-input-container">
                    <KoodiSelectRadio selectable={toimintamuodot}
                                      selected={toimintamuoto}
                                      disabled={readOnly}
                                      onChange={setToimintamuoto} />
                </div>
            </FormFieldContainer>
            <FormFieldContainer label={i18n.translate('ETUNIMI')} labelFor="etunimi" required={!readOnly} errorText={errors.etunimi}>
                <input className={classNames({ ...baseClasses, 'oph-input-has-error': !!errors.etunimi })}
                       type="text"
                       id="etunimi"
                       value={kayttaja.etunimi}
                       disabled={readOnly}
                       onChange={event => setKayttaja({ etunimi: event.currentTarget.value })} />
            </FormFieldContainer>
            <FormFieldContainer label={i18n.translate('SUKUNIMI')} labelFor="sukunimi" required={!readOnly} errorText={errors.sukunimi}>
                <input className={classNames({ ...baseClasses, 'oph-input-has-error': !!errors.sukunimi })}
                       type="text"
                       id="sukunimi"
                       value={kayttaja.sukunimi}
                       disabled={readOnly}
                       onChange={event => setKayttaja({ sukunimi: event.currentTarget.value })} />
            </FormFieldContainer>
            <FormFieldContainer label={i18n.translate('SAHKOPOSTI')} labelFor="sahkoposti" required={!readOnly} errorText={errors.sahkoposti}>
                <input className={classNames({ ...baseClasses, 'oph-input-has-error': !!errors.sahkoposti })}
                       type="text"
                       id="sahkoposti"
                       value={kayttaja.sahkoposti}
                       disabled={readOnly}
                       onChange={event => setKayttaja({ sahkoposti: event.currentTarget.value })} />
            </FormFieldContainer>
            <FormFieldContainer label={i18n.translate('ASIOINTIKIELI')} labelFor="asiointikieli" required={!readOnly} errorText={errors.asiointikieli}>
                <div className="oph-input-container">
                    <Select id="asiointikieli"
                            selectable={asiointikielet}
                            selected={kayttaja.asiointikieli}
                            disabled={readOnly}
                            hasError={!!errors.asiointikieli}
                            onChange={asiointikieli => setKayttaja({ asiointikieli: asiointikieli })} />
                </div>
            </FormFieldContainer>
            <FormFieldContainer label={i18n.translate('SAATETEKSTI')} labelFor="saateteksti" errorText={errors.saateteksti}>
                <textarea className={classNames({ ...baseClasses, 'oph-input-has-error': !!errors.saateteksti })}
                          id="saateteksti"
                          value={kayttaja.saateteksti}
                          disabled={readOnly}
                          onChange={event => setKayttaja({ saateteksti: event.currentTarget.value })}></textarea>
            </FormFieldContainer>
        </>
    );
}
