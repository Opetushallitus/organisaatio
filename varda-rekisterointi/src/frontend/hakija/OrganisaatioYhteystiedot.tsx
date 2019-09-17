import React, { useContext } from 'react';
import useAxios from 'axios-hooks';
import FormFieldContainer from '../FormFieldContainer';
import { Organisaatio, Koodi, Language } from '../types';
import { getYhteystietoArvo, isPuhelinnumero, isSahkoposti, isKayntiosoite, isPostiosoite, updateYhteystiedot } from '../OrganisaatioYhteystietoUtils';
import { toLocalizedText } from '../LocalizableTextUtils';
import { hasLength } from '../StringUtils';
import Spinner from '../Spinner';
import { LanguageContext } from '../contexts';
import classNames from 'classnames/bind';

type Props = {
    readOnly?: boolean,
    initialOrganisaatio: Organisaatio,
    organisaatio: Organisaatio,
    setOrganisaatio: (organisaatio: Partial<Organisaatio>) => void,
    errors: Record<string, string>,
}

function koodiByArvoToLocalizedText(koodit: Koodi[], language: Language, arvo?: string) {
    const koodi = koodit.find(koodi => koodi.arvo === arvo);
    return koodi ? toLocalizedText(koodi.nimi, language) : '';
}

export default function OrganisaatioYhteystiedot({readOnly, initialOrganisaatio, organisaatio, setOrganisaatio, errors}: Props) {
    const { language, i18n } = useContext(LanguageContext);
    const [{data: postinumerot, loading: postinumerotLoading, error: postinumerotError}]
        = useAxios<Koodi[]>('/varda-rekisterointi/api/koodisto/POSTI/koodi');

    if (postinumerotLoading) {
        return <Spinner />;
    }
    if (postinumerotError) {
        return <div>error, reload page</div>;
    }

    const kieliUri = organisaatio.ytjkieli || 'kieli_fi#1';

    const initialPuhelinnumero = getYhteystietoArvo(initialOrganisaatio.yhteystiedot,
        isPuhelinnumero, yhteystieto => yhteystieto.numero);
    const puhelinnumero = getYhteystietoArvo(organisaatio.yhteystiedot,
        isPuhelinnumero, yhteystieto => yhteystieto.numero);
    const initialSahkoposti = getYhteystietoArvo(initialOrganisaatio.yhteystiedot,
        isSahkoposti, yhteystieto => yhteystieto.email);
    const sahkoposti = getYhteystietoArvo(organisaatio.yhteystiedot,
        isSahkoposti, yhteystieto => yhteystieto.email);

    const initialKayntiosoite = getYhteystietoArvo(initialOrganisaatio.yhteystiedot,
        isKayntiosoite, yhteystieto => yhteystieto.osoite);
    const kayntiosoite = getYhteystietoArvo(organisaatio.yhteystiedot,
        isKayntiosoite, yhteystieto => yhteystieto.osoite);
    const initialKayntiosoitteenPostinumeroUri = getYhteystietoArvo(initialOrganisaatio.yhteystiedot,
        isKayntiosoite, yhteystieto => yhteystieto.postinumeroUri);
    const kayntiosoitteenPostinumeroUri = getYhteystietoArvo(organisaatio.yhteystiedot,
        isKayntiosoite, yhteystieto => yhteystieto.postinumeroUri);
    const kayntiosoitteenPostinumero = kayntiosoitteenPostinumeroUri.replace('posti_', '');
    const kayntiosoitteenPostitoimipaikka = getYhteystietoArvo(organisaatio.yhteystiedot,
        isKayntiosoite, yhteystieto => yhteystieto.postitoimipaikka);

    const initialPostiosoite = getYhteystietoArvo(initialOrganisaatio.yhteystiedot,
        isPostiosoite, yhteystieto => yhteystieto.osoite);
    const postiosoite = getYhteystietoArvo(organisaatio.yhteystiedot,
        isPostiosoite, yhteystieto => yhteystieto.osoite);
    const initialPostinumeroUri = getYhteystietoArvo(initialOrganisaatio.yhteystiedot,
        isPostiosoite, yhteystieto => yhteystieto.postinumeroUri);
    const postinumeroUri = getYhteystietoArvo(organisaatio.yhteystiedot,
        isPostiosoite, yhteystieto => yhteystieto.postinumeroUri);
    const postinumero = postinumeroUri.replace('posti_', '');
    const postitoimipaikka = getYhteystietoArvo(organisaatio.yhteystiedot,
        isPostiosoite, yhteystieto => yhteystieto.postitoimipaikka);

    const puhelinnumeroDisabled = readOnly || hasLength(initialPuhelinnumero);
    const sahkopostiDisabled = readOnly || hasLength(initialSahkoposti);
    const kayntiosoiteDisabled = readOnly || hasLength(initialKayntiosoite);
    const kayntiosoitteenPostinumeroDisabled = readOnly || hasLength(initialKayntiosoitteenPostinumeroUri);
    const postiosoiteDisabled = readOnly || hasLength(initialPostiosoite);
    const postinumeroDisabled = readOnly || hasLength(initialPostinumeroUri);

    const baseClasses = { 'oph-input': true };

    return (
        <>
            <FormFieldContainer label={i18n.translate('PUHELINNUMERO')} labelFor="puhelinnumero" required={!puhelinnumeroDisabled} errorText={errors.puhelinnumero}>
                <input className={classNames({ ...baseClasses, 'oph-input-has-error': !!errors.puhelinnumero })}
                       type="text"
                       id="puhelinnumero"
                       value={puhelinnumero}
                       disabled={puhelinnumeroDisabled}
                       onChange={event => setOrganisaatio({ yhteystiedot: updateYhteystiedot(organisaatio.yhteystiedot, isPuhelinnumero, {
                           kieli: kieliUri,
                           numero: event.currentTarget.value,
                        })})} />
            </FormFieldContainer>
            <FormFieldContainer label={i18n.translate('ORGANISAATION_SAHKOPOSTI')} labelFor="organisaation-sahkoposti" required={!sahkopostiDisabled} errorText={errors.sahkoposti}>
                <input className={classNames({ ...baseClasses, 'oph-input-has-error': !!errors.sahkoposti })}
                       type="text"
                       id="organisaation-sahkoposti"
                       value={sahkoposti}
                       disabled={sahkopostiDisabled}
                       onChange={event => setOrganisaatio({ yhteystiedot: updateYhteystiedot(organisaatio.yhteystiedot, isSahkoposti, {
                           kieli: kieliUri,
                           email: event.currentTarget.value,
                        })})} />
            </FormFieldContainer>
            <FormFieldContainer label={i18n.translate('POSTIOSOITE')} labelFor="postiosoite" errorText={errors.postiosoite}>
                <input className={classNames({ ...baseClasses, 'oph-input-has-error': !!errors.postiosoite })}
                       type="text"
                       id="postiosoite"
                       value={postiosoite}
                       disabled={postiosoiteDisabled}
                       onChange={event => setOrganisaatio({ yhteystiedot: updateYhteystiedot(organisaatio.yhteystiedot, isPostiosoite, {
                           kieli: kieliUri,
                           osoiteTyyppi: 'posti',
                           osoite: event.currentTarget.value
                       })})} />
            </FormFieldContainer>
            <FormFieldContainer label={i18n.translate('POSTINUMERO')} labelFor="postinumero" required={!postinumeroDisabled} errorText={errors.postinumero}>
                <input className={classNames({ ...baseClasses, 'oph-input-has-error': !!errors.postinumero })}
                       type="text"
                       id="postinumero"
                       value={postinumero}
                       disabled={postinumeroDisabled}
                       onChange={event => setOrganisaatio({ yhteystiedot: updateYhteystiedot(organisaatio.yhteystiedot, isPostiosoite, {
                           kieli: kieliUri,
                           osoiteTyyppi: 'posti',
                           postinumeroUri: `posti_${event.currentTarget.value}`,
                           postitoimipaikka: event.currentTarget.value.length === 5 ? koodiByArvoToLocalizedText(postinumerot, language, event.currentTarget.value) : '',
                       })})} />
            </FormFieldContainer>
            <FormFieldContainer label={i18n.translate('POSTITOIMIPAIKKA')}>
                <div className="oph-input-container">
                    {postitoimipaikka}
                </div>
            </FormFieldContainer>
            <FormFieldContainer label={i18n.translate('KAYNTIOSOITE')} labelFor="kayntiosoite" required={!kayntiosoiteDisabled} errorText={errors.kayntiosoite}>
                <input className={classNames({ ...baseClasses, 'oph-input-has-error': !!errors.kayntiosoite })}
                       type="text"
                       id="kayntiosoite"
                       value={kayntiosoite}
                       disabled={kayntiosoiteDisabled}
                       onChange={event => setOrganisaatio({ yhteystiedot: updateYhteystiedot(organisaatio.yhteystiedot, isKayntiosoite, {
                           kieli: kieliUri,
                           osoiteTyyppi: 'kaynti',
                           osoite: event.currentTarget.value
                       })})} />
            </FormFieldContainer>
            <FormFieldContainer label={i18n.translate('KAYNTIOSOITTEEN_POSTINUMERO')} labelFor="kayntiosoitteen-postinumero" required={!kayntiosoitteenPostinumeroDisabled} errorText={errors.kayntiosoitteenPostinumero}>
                <input className={classNames({ ...baseClasses, 'oph-input-has-error': !!errors.kayntiosoitteenPostinumero })}
                       type="text"
                       id="kayntiosoitteen-postinumero"
                       value={kayntiosoitteenPostinumero}
                       disabled={kayntiosoitteenPostinumeroDisabled}
                       onChange={event => setOrganisaatio({ yhteystiedot: updateYhteystiedot(organisaatio.yhteystiedot, isKayntiosoite, {
                           kieli: kieliUri,
                           osoiteTyyppi: 'kaynti',
                           postinumeroUri: `posti_${event.currentTarget.value}`,
                           postitoimipaikka: event.currentTarget.value.length === 5 ? koodiByArvoToLocalizedText(postinumerot, language, event.currentTarget.value) : '',
                       })})} />
            </FormFieldContainer>
            <FormFieldContainer label={i18n.translate('KAYNTIOSOITTEEN_POSTITOIMIPAIKKA')}>
                <div className="oph-input-container">
                    {kayntiosoitteenPostitoimipaikka}
                </div>
            </FormFieldContainer>
        </>
    );
}
