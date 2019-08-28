import React, { useContext } from 'react';
import useAxios from 'axios-hooks';
import DateSelect from '../DateSelect';
import FormFieldContainer from '../FormFieldContainer';
import { Organisaatio, Koodi } from '../types';
import KoodiSelect from '../KoodiSelect';
import { toLocalizedText, hasLengthInLang } from '../LocalizableTextUtils';
import LocalizableTextEdit from '../LocalizableTextEdit';
import { hasLength } from '../StringUtils';
import Spinner from '../Spinner';
import { LanguageContext } from '../contexts';
import { toLocalizedKoodi } from '../KoodiUtils';

type Props = {
    readOnly?: boolean,
    initialOrganisaatio: Organisaatio,
    organisaatio: Organisaatio,
    setOrganisaatio: (organisaatio: Partial<Organisaatio>) => void,
}

export default function OrganisaatioTiedot({readOnly, initialOrganisaatio, organisaatio, setOrganisaatio}: Props) {
    const language = useContext(LanguageContext);
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
    const maaDisabled = readOnly || hasLength(initialOrganisaatio.maaUri);
    const alkuPvmDisabled = readOnly || hasLength(initialOrganisaatio.alkuPvm);

    return (
        <>
            <FormFieldContainer label="Organisaation nimi" required={!nimiDisabled}>
                <LocalizableTextEdit value={organisaatio.nimi}
                                     disabled={nimiDisabled}
                                     onChange={nimi => setOrganisaatio({ nimi: nimi, nimet: [ { alkuPvm: organisaatio.alkuPvm, nimi: nimi } ] })} />
            </FormFieldContainer>
            <FormFieldContainer label="Y-tunnus" labelFor="ytunnus" required={!ytunnusDisabled}>
                <input className="oph-input"
                       type="text"
                       id="ytunnus"
                       value={organisaatio.ytunnus}
                       disabled={ytunnusDisabled}
                       onChange={event => setOrganisaatio({ ytunnus: event.currentTarget.value })} />
            </FormFieldContainer>
            <FormFieldContainer label="Yritysmuoto" labelFor="yritysmuoto" required={!yritysmuotoDisabled}>
                <input className="oph-input"
                       type="text"
                       id="yritysmuoto"
                       value={organisaatio.yritysmuoto}
                       disabled={yritysmuotoDisabled}
                       onChange={event => setOrganisaatio({ yritysmuoto: event.currentTarget.value })} />
            </FormFieldContainer>
            <FormFieldContainer label="Organisaatiotyyppi">
                <div className="oph-input-container">{tyypit}</div>
            </FormFieldContainer>
            <FormFieldContainer label="Kotipaikka" required={!kotipaikkaDisabled || !maaDisabled}>
                <div className="oph-input-container">
                    <KoodiSelect selectable={kunnat} selected={organisaatio.kotipaikkaUri}
                                disabled={kotipaikkaDisabled}
                                required={!kotipaikkaDisabled}
                                onChange={kotipaikkaUri => setOrganisaatio({ kotipaikkaUri: kotipaikkaUri })} />
                    <span>{toLocalizedKoodi(maa, language)}</span>
                </div>
            </FormFieldContainer>
            <FormFieldContainer label="Toiminnan alkamisaika" required={!alkuPvmDisabled}>
                <div className="oph-input-container">
                    <DateSelect value={organisaatio.alkuPvm}
                                disabled={alkuPvmDisabled}
                                onChange={alkuPvm => setOrganisaatio({ alkuPvm: alkuPvm, nimet: [ { alkuPvm: alkuPvm, nimi: organisaatio.nimi } ] })} />
                </div>
            </FormFieldContainer>
        </>
    );
}
