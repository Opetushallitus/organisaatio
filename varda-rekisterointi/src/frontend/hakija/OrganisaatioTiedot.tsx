import React, { useContext } from 'react';
import useAxios from 'axios-hooks';
import DateSelect from '../DateSelect';
import FormFieldContainer from '../FormFieldContainer';
import { Organisaatio, Koodi } from '../types';
import KoodiSelect from '../KoodiSelect';
import { toLocalizedText } from '../LocalizableTextUtils';
import { hasLength } from '../StringUtils';
import Spinner from '../Spinner';
import { LanguageContext } from '../contexts';
import classNames from 'classnames/bind';

type Props = {
    readOnly?: boolean,
    kaikkiKunnat: Koodi[],
    initialOrganisaatio: Organisaatio,
    organisaatio: Organisaatio,
    setOrganisaatio: (organisaatio: Partial<Organisaatio>) => void,
    errors: Record<string, string>,
}

export default function OrganisaatioTiedot({readOnly, kaikkiKunnat, initialOrganisaatio, organisaatio, setOrganisaatio, errors}: Props) {
    const { language, i18n } = useContext(LanguageContext);
    const [{data: yritysmuodot, loading: yritysmuotoLoading, error: yritysmuodotError}]
        = useAxios<Koodi[]>('/varda-rekisterointi/api/koodisto/YRITYSMUOTO/koodi?onlyValid=true');
    const [{data: organisaatiotyypit, loading: organisaatiotyypitLoading, error: organisaatiotyypitError}]
        = useAxios<Koodi[]>('/varda-rekisterointi/api/koodisto/ORGANISAATIOTYYPPI/koodi?onlyValid=true');

    if (organisaatiotyypitLoading || yritysmuotoLoading) {
        return <Spinner />;
    }
    if (organisaatiotyypitError || yritysmuodotError) {
        return <div>error, reload page</div>;
    }

    const tyypit = organisaatiotyypit.filter(koodi => {
        if (organisaatio.tyypit) {
            return organisaatio.tyypit.some(tyyppi => tyyppi === koodi.uri);
        }
        return false;
    }).map(koodi => toLocalizedText(koodi.nimi, language, koodi.arvo)).join(', ');

    const nimiDisabled = readOnly || !!initialOrganisaatio.ytjNimi;
    const ytunnusDisabled = readOnly || hasLength(initialOrganisaatio.ytunnus);
    const yritysmuotoDisabled = readOnly || hasLength(initialOrganisaatio.yritysmuoto);
    const kotipaikkaDisabled = readOnly || hasLength(initialOrganisaatio.kotipaikkaUri);
    const alkuPvmDisabled = readOnly || hasLength(initialOrganisaatio.alkuPvm);

    const baseClasses = { 'oph-input': true };

    return (
        <>
            <FormFieldContainer label={i18n.translate('ORGANISAATION_NIMI')} errorText={errors.nimi}>
                <input className={classNames({ ...baseClasses, 'oph-input-has-error': !!errors.nimi })}
                       type="text"
                       id="nimi"
                       value={organisaatio.ytjNimi.nimi}
                       disabled={nimiDisabled}
                       onChange={event => setOrganisaatio({ ytjNimi: { nimi: event.currentTarget.value, alkuPvm: organisaatio.alkuPvm, kieli: organisaatio.ytjNimi.kieli }})} />
            </FormFieldContainer>
            <FormFieldContainer label={i18n.translate('YTUNNUS')} labelFor="ytunnus" errorText={errors.ytunnus}>
                <input className={classNames({ ...baseClasses, 'oph-input-has-error': !!errors.ytunnus })}
                       type="text"
                       id="ytunnus"
                       value={organisaatio.ytunnus}
                       disabled={ytunnusDisabled}
                       onChange={event => setOrganisaatio({ ytunnus: event.currentTarget.value })} />
            </FormFieldContainer>
            <FormFieldContainer label={i18n.translate('YRITYSMUOTO')} labelFor="yritysmuoto" errorText={errors.yritysmuoto}>
                <div className="oph-input-container">
                    <KoodiSelect selectable={yritysmuodot} selected={organisaatio.yritysmuoto}
                                 disabled={yritysmuotoDisabled}
                                 required={!yritysmuotoDisabled}
                                 hasError={!!errors.yritysmuoto}
                                 valueFn={koodi => koodi.nimi.fi || koodi.uri}
                                 onChange={yritysmuoto => setOrganisaatio({ yritysmuoto: yritysmuoto })} />
                </div>
            </FormFieldContainer>
            <FormFieldContainer label={i18n.translate('ORGANISAATIOTYYPPI')}>
                <div className="oph-input-container">{tyypit}</div>
            </FormFieldContainer>
            <FormFieldContainer label={i18n.translate('KOTIPAIKKA')} errorText={errors.kotipaikkaUri}>
                <div className="oph-input-container">
                    <KoodiSelect selectable={kaikkiKunnat} selected={organisaatio.kotipaikkaUri}
                                disabled={kotipaikkaDisabled}
                                required={!kotipaikkaDisabled}
                                hasError={!!errors.kotipaikkaUri}
                                onChange={kotipaikkaUri => setOrganisaatio({ kotipaikkaUri: kotipaikkaUri })} />
                </div>
            </FormFieldContainer>
            <FormFieldContainer label={i18n.translate('TOIMINNAN_ALKAMISAIKA')} errorText={errors.alkuPvm}>
                <div className="oph-input-container">
                    <DateSelect value={organisaatio.alkuPvm}
                                disabled={alkuPvmDisabled}
                                hasError={!!errors.alkuPvm}
                                onChange={alkuPvm => setOrganisaatio({ alkuPvm: alkuPvm, ytjNimi: { alkuPvm: alkuPvm, nimi: organisaatio.ytjNimi.nimi, kieli: organisaatio.ytjNimi.kieli }})} />
                </div>
            </FormFieldContainer>
        </>
    );
}
