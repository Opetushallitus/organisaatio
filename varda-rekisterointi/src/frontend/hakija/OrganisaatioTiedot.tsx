import React, { useContext } from 'react';
import useAxios from 'axios-hooks';
import DateSelect from '../DateSelect';
import FormFieldContainer from '../FormFieldContainer';
import { Organisaatio, Koodi } from '../types/types';
import KoodiSelect from '../KoodiSelect';
import { toLocalizedText } from '../LocalizableTextUtils';
import { isNonEmpty } from '../StringUtils';
import Spinner from '../Spinner';
import { LanguageContext } from '../contexts';
import classNames from 'classnames/bind';
import ErrorPage from '../virhe/VirheSivu';
import { yritysmuotoSortFnByLanguage, yritysmuotoValueFn } from './YritysmuotoUtils';

type Props = {
    readOnly?: boolean;
    kaikkiKunnat: Koodi[];
    initialOrganisaatio: Organisaatio;
    organisaatio: Organisaatio;
    setOrganisaatio: (organisaatio: Partial<Organisaatio>) => void;
    errors: Record<string, string>;
};

export default function OrganisaatioTiedot({
    readOnly,
    kaikkiKunnat,
    initialOrganisaatio,
    organisaatio,
    setOrganisaatio,
    errors,
}: Props) {
    const { language, i18n } = useContext(LanguageContext);
    const [{ data: yritysmuodot, loading: yritysmuotoLoading, error: yritysmuodotError }] = useAxios<Koodi[]>(
        '/varda-rekisterointi/api/koodisto/YRITYSMUOTO/koodi?onlyValid=true'
    );
    const [{ data: organisaatiotyypit, loading: organisaatiotyypitLoading, error: organisaatiotyypitError }] = useAxios<
        Koodi[]
    >('/varda-rekisterointi/api/koodisto/ORGANISAATIOTYYPPI/koodi?onlyValid=true');

    if (organisaatiotyypitLoading || yritysmuotoLoading) {
        return <Spinner />;
    }
    if (organisaatiotyypitError || yritysmuodotError) {
        return <ErrorPage>{i18n.translate('ERROR_FETCH')}</ErrorPage>;
    }

    const tyypit = organisaatiotyypit
        .filter((koodi) => {
            if (organisaatio.tyypit) {
                return organisaatio.tyypit.some((tyyppi) => tyyppi === koodi.uri);
            }
            return false;
        })
        .map((koodi) => toLocalizedText(koodi.nimi, language, koodi.arvo))
        .join(', ');

    const nimiDisabled = readOnly || !!initialOrganisaatio.ytjNimi.nimi;
    const ytunnusDisabled = readOnly || isNonEmpty(initialOrganisaatio.ytunnus);
    const yritysmuotoDisabled = readOnly || isNonEmpty(initialOrganisaatio.yritysmuoto);
    const kotipaikkaDisabled = readOnly || isNonEmpty(initialOrganisaatio.kotipaikkaUri);
    const alkuPvmDisabled = readOnly || isNonEmpty(initialOrganisaatio.alkuPvm);
    const selkokielinenKotipaikka = kaikkiKunnat.find((k) => k.uri === organisaatio.kotipaikkaUri);

    // TODO Väliaikainen fix. Tämän voisi poistaa kunhan organisaatiopalvelu siirtyy käyttämään koodistoa yritysmuotojen osalta. Korjaus sitten myös riville 78.
    const yritysmuotoKoodi = yritysmuodot.find(
        (y) =>
            (y.nimi &&
                (y.nimi.fi === initialOrganisaatio.yritysmuoto || y.nimi.sv === initialOrganisaatio.yritysmuoto)) ||
            y.uri === initialOrganisaatio.yritysmuoto
    );

    const baseClasses = { 'oph-input': true };

    return (
        <>
            <FormFieldContainer
                label={i18n.translate('ORGANISAATION_NIMI')}
                labelFor="organisaationnimi"
                errorText={errors.nimi}
                ariaErrorKoosteId="rekisterointi_organisaatio_virheet"
            >
                <input
                    autoFocus
                    className={classNames({ ...baseClasses, 'oph-input-has-error': !!errors.nimi })}
                    type="text"
                    id="organisaationnimi"
                    value={organisaatio.ytjNimi.nimi}
                    readOnly={nimiDisabled}
                    onChange={(event) =>
                        setOrganisaatio({
                            ytjNimi: {
                                nimi: event.currentTarget.value,
                                alkuPvm: organisaatio.alkuPvm,
                                kieli: organisaatio.ytjNimi.kieli,
                            },
                        })
                    }
                />
            </FormFieldContainer>
            <FormFieldContainer
                label={i18n.translate('YTUNNUS')}
                labelFor="organisaationytunnus"
                errorText={errors.ytunnus}
                ariaErrorKoosteId="rekisterointi_organisaatio_virheet"
            >
                <input
                    className={classNames({ ...baseClasses, 'oph-input-has-error': !!errors.ytunnus })}
                    type="text"
                    id="organisaationytunnus"
                    value={organisaatio.ytunnus}
                    readOnly={ytunnusDisabled}
                    onChange={(event) => setOrganisaatio({ ytunnus: event.currentTarget.value })}
                />
            </FormFieldContainer>
            <FormFieldContainer
                label={i18n.translate('YRITYSMUOTO')}
                labelFor="yritysmuoto"
                errorText={errors.yritysmuoto}
                ariaErrorKoosteId="rekisterointi_organisaatio_virheet"
            >
                <div className="oph-input-container">
                    {!yritysmuotoDisabled ? (
                        <KoodiSelect
                            id="yritysmuoto"
                            selectable={yritysmuodot}
                            selected={
                                yritysmuotoDisabled && yritysmuotoKoodi
                                    ? yritysmuotoKoodi.uri
                                    : organisaatio.yritysmuoto
                            }
                            required={!yritysmuotoDisabled}
                            hasError={!!errors.yritysmuoto}
                            valueFn={yritysmuotoValueFn}
                            sortFn={yritysmuotoSortFnByLanguage(language)}
                            onChange={(yritysmuoto) => setOrganisaatio({ yritysmuoto: yritysmuoto })}
                        />
                    ) : (
                        <input
                            className={classNames({ ...baseClasses, 'oph-input-has-error': !!errors.yritysmuoto })}
                            type="text"
                            id="yritysmuoto"
                            value={yritysmuotoKoodi ? yritysmuotoKoodi.nimi[language] : organisaatio.yritysmuoto}
                            readOnly
                        />
                    )}
                </div>
            </FormFieldContainer>
            <FormFieldContainer
                label={i18n.translate('ORGANISAATIOTYYPPI')}
                ariaErrorKoosteId="rekisterointi_organisaatio_virheet"
            >
                <div tabIndex={0} className="oph-input-container">
                    {tyypit}
                </div>
            </FormFieldContainer>
            <FormFieldContainer
                label={i18n.translate('KOTIPAIKKA')}
                labelFor="organisaationkotipaikka"
                errorText={errors.kotipaikkaUri}
                ariaErrorKoosteId="rekisterointi_organisaatio_virheet"
            >
                <div className="oph-input-container">
                    {!kotipaikkaDisabled ? (
                        <KoodiSelect
                            id="organisaationkotipaikka"
                            selectable={kaikkiKunnat}
                            selected={organisaatio.kotipaikkaUri}
                            required={!kotipaikkaDisabled}
                            hasError={!!errors.kotipaikkaUri}
                            onChange={(kotipaikkaUri) => setOrganisaatio({ kotipaikkaUri: kotipaikkaUri })}
                        />
                    ) : (
                        <input
                            className={classNames({ ...baseClasses, 'oph-input-has-error': !!errors.kotipaikkaUri })}
                            type="text"
                            id="organisaationkotipaikka"
                            value={
                                selkokielinenKotipaikka
                                    ? selkokielinenKotipaikka.nimi[language]
                                    : organisaatio.kotipaikkaUri
                            }
                            readOnly
                        />
                    )}
                </div>
            </FormFieldContainer>
            <FormFieldContainer
                label={i18n.translate('TOIMINNAN_ALKAMISAIKA')}
                labelFor="organisaationalkuPvm"
                errorText={errors.alkuPvm}
                ariaLisatietoId="datepickerohje"
                ariaLisatietoLokalisaatio={i18n.translate('RUUDUNLUKIJA_DATEPICKER_OHJE')}
                ariaErrorKoosteId="rekisterointi_organisaatio_virheet"
            >
                <div className="oph-input-container">
                    <DateSelect
                        id="organisaationalkuPvm"
                        value={organisaatio.alkuPvm}
                        readOnly={alkuPvmDisabled}
                        hasError={!!errors.alkuPvm}
                        onChange={(alkuPvm) =>
                            setOrganisaatio({
                                alkuPvm: alkuPvm,
                                ytjNimi: {
                                    alkuPvm: alkuPvm,
                                    nimi: organisaatio.ytjNimi.nimi,
                                    kieli: organisaatio.ytjNimi.kieli,
                                },
                            })
                        }
                    />
                </div>
            </FormFieldContainer>
        </>
    );
}
