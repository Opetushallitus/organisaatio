import React, { useContext } from 'react';
import useAxios from 'axios-hooks';
import FormFieldContainer from '../FormFieldContainer';
import { Kayttaja, KoodiUri, Koodi } from '../types/types';
import Select from '../Select';
import KoodiSelectRadio from '../KoodiSelectRadio';
import Spinner from '../Spinner';
import { asiointikielet } from '../LocalizableTextUtils';
import classNames from 'classnames/bind';
import { LanguageContext } from '../contexts';
import ErrorPage from '../virhe/VirheSivu';

type Props = {
    readOnly?: boolean;
    toimintamuoto: string;
    setToimintamuoto: (toimintamuoto: KoodiUri) => void;
    kayttaja: Kayttaja;
    setKayttaja: (kayttaja: Partial<Kayttaja>) => void;
    errors: Record<string, string>;
};

export default function KayttajaYhteystiedot({
    readOnly,
    toimintamuoto,
    setToimintamuoto,
    kayttaja,
    setKayttaja,
    errors,
}: Props) {
    const { i18n, language } = useContext(LanguageContext);
    const [{ data: toimintamuodot, loading: toimintamuodotLoading, error: toimintamuodotError }] = useAxios<Koodi[]>(
        '/varda-rekisterointi/api/koodisto/VARDA_TOIMINTAMUOTO/koodi?onlyValid=true'
    );

    if (toimintamuodotLoading) {
        return <Spinner />;
    }
    if (toimintamuodotError || !toimintamuodot) {
        return <ErrorPage>{i18n.translate('ERROR_FETCH')}</ErrorPage>;
    }

    const baseClasses = { 'oph-input': true };

    const selkokielinenAsiointikieli = asiointikielet.find((k) => k.value === kayttaja.asiointikieli);
    return (
        <>
            <FormFieldContainer
                labelFor="varhaiskasvatustoimijan-toimintamuoto"
                label={i18n.translate('VARHAISKASVATUSTOIMIJA')}
                ariaErrorKoosteId="rekisterointi_kayttaja_virheet"
            >
                <div className="oph-input-container">
                    <KoodiSelectRadio
                        autoFocus={!readOnly}
                        id="varhaiskasvatustoimijan-toimintamuoto"
                        selectable={toimintamuodot}
                        selected={toimintamuoto}
                        readOnly={readOnly}
                        onChange={setToimintamuoto}
                    />
                </div>
            </FormFieldContainer>
            <FormFieldContainer
                label={i18n.translate('ETUNIMI')}
                labelFor="paakayttajan-etunimi"
                errorText={errors.etunimi}
                ariaErrorKoosteId="rekisterointi_kayttaja_virheet"
            >
                <input
                    className={classNames({ ...baseClasses, 'oph-input-has-error': !!errors.etunimi })}
                    type="text"
                    id="paakayttajan-etunimi"
                    value={kayttaja.etunimi}
                    readOnly={readOnly}
                    onChange={(event) => setKayttaja({ etunimi: event.currentTarget.value })}
                />
            </FormFieldContainer>
            <FormFieldContainer
                label={i18n.translate('SUKUNIMI')}
                labelFor="paakayttajan-sukunimi"
                errorText={errors.sukunimi}
                ariaErrorKoosteId="rekisterointi_kayttaja_virheet"
            >
                <input
                    className={classNames({ ...baseClasses, 'oph-input-has-error': !!errors.sukunimi })}
                    type="text"
                    id="paakayttajan-sukunimi"
                    value={kayttaja.sukunimi}
                    readOnly={readOnly}
                    onChange={(event) => setKayttaja({ sukunimi: event.currentTarget.value })}
                />
            </FormFieldContainer>
            <FormFieldContainer
                label={i18n.translate('SAHKOPOSTI')}
                labelFor="paakayttajan-sahkoposti"
                errorText={errors.sahkoposti}
                ariaErrorKoosteId="rekisterointi_kayttaja_virheet"
            >
                <input
                    className={classNames({ ...baseClasses, 'oph-input-has-error': !!errors.sahkoposti })}
                    type="text"
                    id="paakayttajan-sahkoposti"
                    value={kayttaja.sahkoposti}
                    readOnly={readOnly}
                    onChange={(event) => setKayttaja({ sahkoposti: event.currentTarget.value })}
                />
            </FormFieldContainer>
            <FormFieldContainer
                label={i18n.translate('ASIOINTIKIELI')}
                labelFor="paakayttajan-asiointikieli"
                errorText={errors.asiointikieli}
                ariaErrorKoosteId="rekisterointi_kayttaja_virheet"
            >
                <div className="oph-input-container">
                    {!readOnly ? (
                        <Select
                            id="paakayttajan-asiointikieli"
                            selectable={asiointikielet}
                            selected={kayttaja.asiointikieli}
                            hasError={!!errors.asiointikieli}
                            onChange={(asiointikieli) => setKayttaja({ asiointikieli: asiointikieli })}
                        />
                    ) : (
                        <input
                            className={classNames({ ...baseClasses, 'oph-input-has-error': !!errors.kotipaikkaUri })}
                            type="text"
                            id="paakayttajan-asiointikieli"
                            value={
                                (selkokielinenAsiointikieli && selkokielinenAsiointikieli.label[language]) ||
                                kayttaja.asiointikieli
                            }
                            readOnly
                        />
                    )}
                </div>
            </FormFieldContainer>
            <FormFieldContainer
                label={i18n.translate('SAATETEKSTI')}
                labelFor="paakayttajan-saateteksti"
                errorText={errors.saateteksti}
                ariaErrorKoosteId="rekisterointi_kayttaja_virheet"
            >
                <textarea
                    className={classNames({ ...baseClasses, 'oph-input-has-error': !!errors.saateteksti })}
                    id="paakayttajan-saateteksti"
                    value={kayttaja.saateteksti}
                    readOnly={readOnly}
                    onChange={(event) => setKayttaja({ saateteksti: event.currentTarget.value })}
                ></textarea>
            </FormFieldContainer>
        </>
    );
}
