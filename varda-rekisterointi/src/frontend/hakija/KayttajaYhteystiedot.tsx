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
import ErrorPage from '../virhe/VirheSivu';

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
        = useAxios<Koodi[]>('/varda-rekisterointi/api/koodisto/VARDA_TOIMINTAMUOTO/koodi?onlyValid=true');

    if (toimintamuodotLoading) {
        return <Spinner />;
    }
    if (toimintamuodotError) {
        return <ErrorPage>{i18n.translate('ERROR_FETCH')}</ErrorPage>;
    }

    const baseClasses = { 'oph-input': true };

    return (
        <>
            <FormFieldContainer labelFor="varhaiskasvatustoimijan-toimintamuoto" label={i18n.translate('VARHAISKASVATUSTOIMIJA')}>
                <div className="oph-input-container">
                    <KoodiSelectRadio id="varhaiskasvatustoimijan-toimintamuoto"
                                      selectable={toimintamuodot}
                                      selected={toimintamuoto}
                                      readOnly={readOnly}
                                      disabled={readOnly}
                                      onChange={setToimintamuoto} />
                </div>
            </FormFieldContainer>
            <FormFieldContainer label={i18n.translate('ETUNIMI')} labelFor="paakayttajan-etunimi" errorText={errors.etunimi}>
                <input className={classNames({ ...baseClasses, 'oph-input-has-error': !!errors.etunimi })}
                       type="text"
                       id="paakayttajan-etunimi"
                       value={kayttaja.etunimi}
                       disabled={readOnly}
                       onChange={event => setKayttaja({ etunimi: event.currentTarget.value })} />
            </FormFieldContainer>
            <FormFieldContainer label={i18n.translate('SUKUNIMI')} labelFor="paakayttajan-sukunimi" errorText={errors.sukunimi}>
                <input className={classNames({ ...baseClasses, 'oph-input-has-error': !!errors.sukunimi })}
                       type="text"
                       id="paakayttajan-sukunimi"
                       value={kayttaja.sukunimi}
                       disabled={readOnly}
                       onChange={event => setKayttaja({ sukunimi: event.currentTarget.value })} />
            </FormFieldContainer>
            <FormFieldContainer label={i18n.translate('SAHKOPOSTI')} labelFor="paakayttajan-sahkoposti" errorText={errors.sahkoposti}>
                <input className={classNames({ ...baseClasses, 'oph-input-has-error': !!errors.sahkoposti })}
                       type="text"
                       id="paakayttajan-sahkoposti"
                       value={kayttaja.sahkoposti}
                       disabled={readOnly}
                       onChange={event => setKayttaja({ sahkoposti: event.currentTarget.value })} />
            </FormFieldContainer>
            <FormFieldContainer label={i18n.translate('ASIOINTIKIELI')} labelFor="paakayttajan-asiointikieli" errorText={errors.asiointikieli}>
                <div className="oph-input-container">
                    <Select id="paakayttajan-asiointikieli"
                            selectable={asiointikielet}
                            selected={kayttaja.asiointikieli}
                            disabled={readOnly}
                            hasError={!!errors.asiointikieli}
                            onChange={asiointikieli => setKayttaja({ asiointikieli: asiointikieli })} />
                </div>
            </FormFieldContainer>
            <FormFieldContainer label={i18n.translate('SAATETEKSTI')} labelFor="paakayttajan-saateteksti" errorText={errors.saateteksti}>
                <textarea className={classNames({ ...baseClasses, 'oph-input-has-error': !!errors.saateteksti })}
                          id="paakayttajan-saateteksti"
                          value={kayttaja.saateteksti}
                          disabled={readOnly}
                          onChange={event => setKayttaja({ saateteksti: event.currentTarget.value })}></textarea>
            </FormFieldContainer>
        </>
    );
}
