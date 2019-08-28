import React, { useContext } from 'react';
import useAxios from 'axios-hooks';
import FormFieldContainer from '../FormFieldContainer';
import { Organisaatio, Koodi, Language } from '../types';
import { getYhteystietoArvo, isPuhelinnumero, isSahkoposti, isKayntiosoite, isPostiosoite, updateYhteystiedot } from '../OrganisaatioYhteystietoUtils';
import { toLocalizedText } from '../LocalizableTextUtils';
import { hasLength } from '../StringUtils';
import Spinner from '../Spinner';
import { LanguageContext } from '../contexts';

type Props = {
    readOnly?: boolean,
    initialOrganisaatio: Organisaatio,
    organisaatio: Organisaatio,
    setOrganisaatio: (organisaatio: Partial<Organisaatio>) => void,
    sahkopostit: string[],
    setSahkopostit: (sahkopostit: string[]) => void,
}

function koodiByArvoToLocalizedText(koodit: Koodi[], language: Language, arvo?: string) {
    const koodi = koodit.find(koodi => koodi.arvo === arvo);
    return koodi ? toLocalizedText(koodi.nimi, language) : '';
}

export default function OrganisaatioYhteystiedot({readOnly, initialOrganisaatio, organisaatio, setOrganisaatio, sahkopostit, setSahkopostit}: Props) {
    const language = useContext(LanguageContext);
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

    return (
        <>
            <FormFieldContainer label="Puhelinnumero" labelFor="puhelinnumero" required={!puhelinnumeroDisabled}>
                <input className="oph-input"
                       type="text"
                       id="puhelinnumero"
                       value={puhelinnumero}
                       disabled={puhelinnumeroDisabled}
                       onChange={event => setOrganisaatio({ yhteystiedot: updateYhteystiedot(organisaatio.yhteystiedot, isPuhelinnumero, {
                           kieli: kieliUri,
                           numero: event.currentTarget.value,
                        })})} />
            </FormFieldContainer>
            <FormFieldContainer label="Organisaation sähköposti" labelFor="organisaation-sahkoposti" required={!sahkopostiDisabled}>
                <input className="oph-input"
                       type="text"
                       id="organisaation-sahkoposti"
                       value={sahkoposti}
                       disabled={sahkopostiDisabled}
                       onChange={event => setOrganisaatio({ yhteystiedot: updateYhteystiedot(organisaatio.yhteystiedot, isSahkoposti, {
                           kieli: kieliUri,
                           email: event.currentTarget.value,
                        })})} />
            </FormFieldContainer>
            <FormFieldContainer label="Sähköpostit prosessin etenemisen viestintään"
                                helpText="Syötä osoitteet, jotka vastaanottavat viestit prosessista yksityisten varhaiskasvatustoimijoiden rekisteröitymisestä Vardaan.">
                <input className="oph-input"
                       type="text"
                       value={sahkopostit[0]}
                       disabled={readOnly}
                       onChange={event => setSahkopostit([ event.currentTarget.value ])} />
            </FormFieldContainer>
            <FormFieldContainer label="Käyntiosoite" labelFor="kayntiosoite" required={!kayntiosoiteDisabled}>
                <input className="oph-input"
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
            <FormFieldContainer label="Käyntiosoitteen postinumero" labelFor="kayntiosoitteen-postinumero" required={!kayntiosoitteenPostinumeroDisabled}>
                <input className="oph-input"
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
            <FormFieldContainer label="Käyntiosoitteen postitoimipaikka" labelFor="kayntiosoitteen-postitoimipaikka" required={!postiosoiteDisabled}>
                <div className="oph-input-container">
                    {kayntiosoitteenPostitoimipaikka}
                </div>
            </FormFieldContainer>
            <FormFieldContainer label="Postiosoite" labelFor="postiosoite">
                <input className="oph-input"
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
            <FormFieldContainer label="Postinumero" labelFor="postinumero" required={!postinumeroDisabled}>
                <input className="oph-input"
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
            <FormFieldContainer label="Postitoimipaikka" labelFor="postitoimipaikka">
                <div className="oph-input-container">
                    {postitoimipaikka}
                </div>
            </FormFieldContainer>
        </>
    );
}
