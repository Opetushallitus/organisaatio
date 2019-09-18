import React, { useContext } from 'react';
import useAxios from 'axios-hooks';
import DateSelect from '../DateSelect';
import FormFieldContainer from '../FormFieldContainer';
import { Organisaatio, Koodi, Language } from '../types';
import KoodiSelect from '../KoodiSelect';
import { toLocalizedText, hasLengthInLang } from '../LocalizableTextUtils';
import LocalizableTextEdit from '../LocalizableTextEdit';
import { hasLength } from '../StringUtils';
import Spinner from '../Spinner';
import { LanguageContext } from '../contexts';
import { toLocalizedKoodi } from '../KoodiUtils';
import classNames from 'classnames/bind';

type Props = {
    readOnly?: boolean,
    initialOrganisaatio: Organisaatio,
    organisaatio: Organisaatio,
    setOrganisaatio: (organisaatio: Partial<Organisaatio>) => void,
    errors: Record<string, string>,
}

export default function OrganisaatioTiedot({readOnly, initialOrganisaatio, organisaatio, setOrganisaatio, errors}: Props) {
    const { language, i18n } = useContext(LanguageContext);
    const [{data: organisaatiotyypit, loading: organisaatiotyypitLoading, error: organisaatiotyypitError}]
        = useAxios<Koodi[]>('/varda-rekisterointi/api/koodisto/ORGANISAATIOTYYPPI/koodi');
    const [{data: kunnat, loading: kunnatLoading, error: kunnatError}]
        = useAxios<Koodi[]>('/varda-rekisterointi/api/koodisto/KUNTA/koodi');
    const [{data: maatJaValtiot1, loading: maatJaValtiot1Loading, error: maatJaValtiot1Error}]
        = useAxios<Koodi[]>('/varda-rekisterointi/api/koodisto/MAAT_JA_VALTIOT_1/koodi');

    if (organisaatiotyypitLoading || kunnatLoading || maatJaValtiot1Loading) {
        return <Spinner />;
    }
    if (organisaatiotyypitError || kunnatError || maatJaValtiot1Error) {
        return <div>error, reload page</div>;
    }

    const tyypit = organisaatiotyypit.filter(koodi => {
        if (organisaatio.tyypit) {
            return organisaatio.tyypit.some(tyyppi => tyyppi === koodi.uri);
        }
        return false;
    }).map(koodi => toLocalizedText(koodi.nimi, language, koodi.arvo)).join(', ');
    const maa = maatJaValtiot1.find(koodi => koodi.uri === organisaatio.maaUri);

    const nimiDisabled = readOnly || hasLengthInLang(initialOrganisaatio.nimi, language);
    const ytunnusDisabled = readOnly || hasLength(initialOrganisaatio.ytunnus);
    const yritysmuotoDisabled = readOnly || hasLength(initialOrganisaatio.yritysmuoto);
    const kotipaikkaDisabled = readOnly || hasLength(initialOrganisaatio.kotipaikkaUri);
    const alkuPvmDisabled = readOnly || hasLength(initialOrganisaatio.alkuPvm);

    const baseClasses = { 'oph-input': true };

    return (
        <>
            <FormFieldContainer label={i18n.translate('ORGANISAATION_NIMI')} required={!nimiDisabled} errorText={errors.nimi}>
                <LocalizableTextEdit value={organisaatio.nimi}
                                     disabled={nimiDisabled}
                                     hasError={!!errors.nimi}
                                     onChange={nimi => setOrganisaatio({ nimi: nimi, nimet: [ { alkuPvm: organisaatio.alkuPvm, nimi: nimi } ] })} />
            </FormFieldContainer>
            <FormFieldContainer label={i18n.translate('YTUNNUS')} labelFor="ytunnus" required={!ytunnusDisabled} errorText={errors.ytunnus}>
                <input className={classNames({ ...baseClasses, 'oph-input-has-error': !!errors.ytunnus })}
                       type="text"
                       id="ytunnus"
                       value={organisaatio.ytunnus}
                       disabled={ytunnusDisabled}
                       onChange={event => setOrganisaatio({ ytunnus: event.currentTarget.value })} />
            </FormFieldContainer>
            <FormFieldContainer label={i18n.translate('YRITYSMUOTO')} labelFor="yritysmuoto" required={!yritysmuotoDisabled} errorText={errors.yritysmuoto}>
                <input className={classNames({ ...baseClasses, 'oph-input-has-error': !!errors.yritysmuoto })}
                       type="text"
                       id="yritysmuoto"
                       value={organisaatio.yritysmuoto}
                       disabled={yritysmuotoDisabled}
                       onChange={event => setOrganisaatio({ yritysmuoto: event.currentTarget.value })} />
            </FormFieldContainer>
            <FormFieldContainer label={i18n.translate('ORGANISAATIOTYYPPI')}>
                <div className="oph-input-container">{tyypit}</div>
            </FormFieldContainer>
            <FormFieldContainer label={i18n.translate('KOTIPAIKKA')} required={!kotipaikkaDisabled} errorText={errors.kotipaikkaUri}>
                <div className="oph-input-container">
                    <KoodiSelect selectable={kunnat} selected={organisaatio.kotipaikkaUri}
                                disabled={kotipaikkaDisabled}
                                required={!kotipaikkaDisabled}
                                hasError={!!errors.kotipaikkaUri}
                                optionLabelFn={(koodi: Koodi, language: Language) => toLocalizedText(koodi.nimi, language, koodi.arvo) + ', ' + toLocalizedKoodi(maa, language)}
                                onChange={kotipaikkaUri => setOrganisaatio({ kotipaikkaUri: kotipaikkaUri })} />
                </div>
            </FormFieldContainer>
            <FormFieldContainer label={i18n.translate('TOIMINNAN_ALKAMISAIKA')} required={!alkuPvmDisabled} errorText={errors.alkuPvm}>
                <div className="oph-input-container">
                    <DateSelect value={organisaatio.alkuPvm}
                                disabled={alkuPvmDisabled}
                                hasError={!!errors.alkuPvm}
                                onChange={alkuPvm => setOrganisaatio({ alkuPvm: alkuPvm, nimet: [ { alkuPvm: alkuPvm, nimi: organisaatio.nimi } ] })} />
                </div>
            </FormFieldContainer>
        </>
    );
}
