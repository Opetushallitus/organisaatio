import React, { useContext, useState } from 'react';
import useAxios from 'axios-hooks';
import FormFieldContainer from '../FormFieldContainer';
import { Organisaatio, Koodi, Language, Osoite, Yhteystiedot } from '../types/types';
import { toLocalizedText } from '../LocalizableTextUtils';
import { isNonEmpty } from '../StringUtils';
import Spinner from '../Spinner';
import { LanguageContext } from '../contexts';
import classNames from 'classnames';
import ErrorPage from '../virhe/VirheSivu';
import * as PuhelinnumeroValidator from '../PuhelinnumeroValidator';

type Props = {
    readOnly?: boolean;
    initialOrganisaatio: Organisaatio;
    organisaatio: Organisaatio;
    setOrganisaatio: (organisaatio: Partial<Organisaatio>) => void;
    errors: Record<string, string>;
};

type OsoiteKentta = Extract<keyof Yhteystiedot, 'postiosoite' | 'kayntiosoite'>;

function koodiByArvoToLocalizedText(koodit: Koodi[], language: Language, arvo?: string) {
    const koodi = koodit.find((koodi) => koodi.arvo === arvo);
    return koodi ? toLocalizedText(koodi.nimi, language) : '';
}

export default function OrganisaatioYhteystiedot({
    readOnly,
    initialOrganisaatio,
    organisaatio,
    setOrganisaatio,
    errors,
}: Props) {
    const { language, i18n } = useContext(LanguageContext);
    const [{ data: postinumerot, loading: postinumerotLoading, error: postinumerotError }] = useAxios<Koodi[]>(
        '/varda-rekisterointi/api/koodisto/POSTI/koodi?onlyValid=true'
    );
    const [kayntiosoiteSamaKuinPostiosoite, setKayntiosoiteSamaKuinPostiosoite] = useState(false);

    if (postinumerotLoading) {
        return <Spinner />;
    }
    if (postinumerotError || !postinumerot) {
        return <ErrorPage>{i18n.translate('ERROR_FETCH')}</ErrorPage>;
    }

    const initialPuhelinnumero = initialOrganisaatio.yhteystiedot.puhelinnumero;
    const puhelinnumero = organisaatio.yhteystiedot.puhelinnumero;
    const initialSahkoposti = initialOrganisaatio.yhteystiedot.sahkoposti;
    const sahkoposti = organisaatio.yhteystiedot.sahkoposti;

    const initialKayntiosoite = initialOrganisaatio.yhteystiedot.kayntiosoite.katuosoite;
    const kayntiosoite = organisaatio.yhteystiedot.kayntiosoite.katuosoite;
    const initialKayntiosoitteenPostinumeroUri = initialOrganisaatio.yhteystiedot.kayntiosoite.postinumeroUri;
    const kayntiosoitteenPostinumeroUri = organisaatio.yhteystiedot.kayntiosoite.postinumeroUri;
    const kayntiosoitteenPostinumero = kayntiosoitteenPostinumeroUri.replace('posti_', '');
    const kayntiosoitteenPostitoimipaikka = organisaatio.yhteystiedot.kayntiosoite.postitoimipaikka;

    const initialPostiosoite = initialOrganisaatio.yhteystiedot.postiosoite.katuosoite;
    const postiosoite = organisaatio.yhteystiedot.postiosoite.katuosoite;
    const initialPostinumeroUri = initialOrganisaatio.yhteystiedot.postiosoite.postinumeroUri;
    const postinumeroUri = organisaatio.yhteystiedot.postiosoite.postinumeroUri;
    const postinumero = postinumeroUri.replace('posti_', '');
    const postitoimipaikka = organisaatio.yhteystiedot.postiosoite.postitoimipaikka;

    const puhelinnumeroDisabled =
        readOnly || (isNonEmpty(initialPuhelinnumero) && PuhelinnumeroValidator.validate(initialPuhelinnumero));
    const sahkopostiDisabled = readOnly || isNonEmpty(initialSahkoposti);
    const kayntiosoiteDisabled = readOnly || isNonEmpty(initialKayntiosoite) || kayntiosoiteSamaKuinPostiosoite;
    const kayntiosoitteenPostinumeroDisabled =
        readOnly || isNonEmpty(initialKayntiosoitteenPostinumeroUri) || kayntiosoiteSamaKuinPostiosoite;
    const samaKuinPostiosoiteDisabled = readOnly || isNonEmpty(initialKayntiosoite);
    const postiosoiteDisabled = readOnly || isNonEmpty(initialPostiosoite);
    const postinumeroDisabled = readOnly || isNonEmpty(initialPostinumeroUri);

    const handleKayntiosoiteSamaKuinPostiosoite = (value: boolean): void => {
        setKayntiosoiteSamaKuinPostiosoite(value);
        if (value) {
            const yhteystiedot = organisaatio.yhteystiedot;
            yhteystiedot.kayntiosoite = { ...yhteystiedot.postiosoite };
            setOrganisaatio({ yhteystiedot: yhteystiedot });
        }
    };

    const handleOsoiteMuutos = (kentta: OsoiteKentta, muutos: Partial<Osoite>): void => {
        const yhteystiedot = organisaatio.yhteystiedot;
        yhteystiedot[kentta] = { ...yhteystiedot[kentta], ...muutos };
        if (kentta === 'postiosoite' && kayntiosoiteSamaKuinPostiosoite) {
            yhteystiedot.kayntiosoite = { ...yhteystiedot.postiosoite };
        }
        setOrganisaatio({ yhteystiedot });
    };

    const baseClasses = { 'oph-input': true };

    return (
        <>
            <FormFieldContainer
                label={i18n.translate('PUHELINNUMERO')}
                labelFor="organisaation-puhelinnumero"
                errorText={errors['yhteystiedot.puhelinnumero']}
                ariaErrorKoosteId="rekisterointi_organisaatio_virheet"
            >
                <input
                    className={classNames({
                        ...baseClasses,
                        'oph-input-has-error': !!errors['yhteystiedot.puhelinnumero'],
                    })}
                    type="text"
                    id="organisaation-puhelinnumero"
                    value={puhelinnumero}
                    readOnly={puhelinnumeroDisabled}
                    onChange={(event) =>
                        setOrganisaatio({
                            yhteystiedot: {
                                ...organisaatio.yhteystiedot,
                                ...{ puhelinnumero: event.currentTarget.value },
                            },
                        })
                    }
                />
            </FormFieldContainer>
            <FormFieldContainer
                label={i18n.translate('ORGANISAATION_SAHKOPOSTI')}
                labelFor="organisaation-sahkoposti"
                errorText={errors['yhteystiedot.sahkoposti']}
                ariaErrorKoosteId="rekisterointi_organisaatio_virheet"
            >
                <input
                    className={classNames({
                        ...baseClasses,
                        'oph-input-has-error': !!errors['yhteystiedot.sahkoposti'],
                    })}
                    type="text"
                    id="organisaation-sahkoposti"
                    value={sahkoposti}
                    readOnly={sahkopostiDisabled}
                    onChange={(event) =>
                        setOrganisaatio({
                            yhteystiedot: {
                                ...organisaatio.yhteystiedot,
                                ...{ sahkoposti: event.currentTarget.value },
                            },
                        })
                    }
                />
            </FormFieldContainer>
            <FormFieldContainer
                label={i18n.translate('POSTIOSOITE')}
                labelFor="organisaation-postiosoite"
                errorText={errors['yhteystiedot.postiosoite.katuosoite']}
                ariaErrorKoosteId="rekisterointi_organisaatio_virheet"
            >
                <input
                    className={classNames({
                        ...baseClasses,
                        'oph-input-has-error': !!errors['yhteystiedot.postiosoite.katuosoite'],
                    })}
                    type="text"
                    id="organisaation-postiosoite"
                    value={postiosoite}
                    readOnly={postiosoiteDisabled}
                    onChange={(event) => handleOsoiteMuutos('postiosoite', { katuosoite: event.currentTarget.value })}
                />
            </FormFieldContainer>
            <FormFieldContainer
                label={i18n.translate('POSTINUMERO')}
                labelFor="organisaation-postinumero"
                errorText={errors['yhteystiedot.postiosoite.postinumeroUri']}
                ariaErrorKoosteId="rekisterointi_organisaatio_virheet"
            >
                <input
                    className={classNames({
                        ...baseClasses,
                        'oph-input-has-error': !!errors['yhteystiedot.postiosoite.postinumeroUri'],
                    })}
                    type="text"
                    id="organisaation-postinumero"
                    value={postinumero}
                    readOnly={postinumeroDisabled}
                    onChange={(event) =>
                        handleOsoiteMuutos('postiosoite', {
                            postinumeroUri: `posti_${event.currentTarget.value}`,
                            postitoimipaikka:
                                event.currentTarget.value.length === 5
                                    ? koodiByArvoToLocalizedText(postinumerot, language, event.currentTarget.value)
                                    : '',
                        })
                    }
                />
            </FormFieldContainer>
            <FormFieldContainer
                label={i18n.translate('POSTITOIMIPAIKKA')}
                ariaErrorKoosteId="rekisterointi_organisaatio_virheet"
            >
                <div tabIndex={0} className="oph-input-container">
                    {postitoimipaikka}
                </div>
            </FormFieldContainer>
            <FormFieldContainer
                label={i18n.translate('KAYNTIOSOITE')}
                labelFor="organisaation-kayntiosoite"
                errorText={errors['yhteystiedot.kayntiosoite.katuosoite']}
                ariaErrorKoosteId="rekisterointi_organisaatio_virheet"
            >
                <div className="oph-input-container">
                    {samaKuinPostiosoiteDisabled ? null : (
                        <label>
                            <input
                                type="checkbox"
                                className="oph-checkable-input"
                                checked={kayntiosoiteSamaKuinPostiosoite}
                                onChange={(event) => handleKayntiosoiteSamaKuinPostiosoite(event.currentTarget.checked)}
                            />
                            <span className="oph-checkable-text">{i18n.translate('SAMA_KUIN_POSTIOSOITE')}</span>
                        </label>
                    )}
                    <input
                        className={classNames({
                            ...baseClasses,
                            'oph-input-has-error': !!errors['yhteystiedot.kayntiosoite.katuosoite'],
                        })}
                        type="text"
                        id="organisaation-kayntiosoite"
                        value={kayntiosoite}
                        readOnly={kayntiosoiteDisabled}
                        onChange={(event) =>
                            handleOsoiteMuutos('kayntiosoite', { katuosoite: event.currentTarget.value })
                        }
                    />
                </div>
            </FormFieldContainer>
            <FormFieldContainer
                label={i18n.translate('KAYNTIOSOITTEEN_POSTINUMERO')}
                labelFor="kayntiosoitteen-postinumero"
                errorText={errors['yhteystiedot.kayntiosoite.postinumeroUri']}
                ariaErrorKoosteId="rekisterointi_organisaatio_virheet"
            >
                <input
                    className={classNames({
                        ...baseClasses,
                        'oph-input-has-error': !!errors['yhteystiedot.kayntiosoite.postinumeroUri'],
                    })}
                    type="text"
                    id="kayntiosoitteen-postinumero"
                    value={kayntiosoitteenPostinumero}
                    readOnly={kayntiosoitteenPostinumeroDisabled}
                    onChange={(event) =>
                        handleOsoiteMuutos('kayntiosoite', {
                            postinumeroUri: `posti_${event.currentTarget.value}`,
                            postitoimipaikka:
                                event.currentTarget.value.length === 5
                                    ? koodiByArvoToLocalizedText(postinumerot, language, event.currentTarget.value)
                                    : '',
                        })
                    }
                />
            </FormFieldContainer>
            <FormFieldContainer
                label={i18n.translate('KAYNTIOSOITTEEN_POSTITOIMIPAIKKA')}
                ariaErrorKoosteId="rekisterointi_organisaatio_virheet"
            >
                <div tabIndex={0} className="oph-input-container">
                    {kayntiosoitteenPostitoimipaikka}
                </div>
            </FormFieldContainer>
        </>
    );
}
