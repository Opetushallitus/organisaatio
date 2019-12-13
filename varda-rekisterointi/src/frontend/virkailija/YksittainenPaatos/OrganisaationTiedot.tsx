import React, { useContext } from 'react';
import FormFieldContainer from '../../FormFieldContainer';
import {Koodi, Organisaatio} from '../../types';
import { toLocalizedText } from '../../LocalizableTextUtils';
import { parseISO, format } from 'date-fns';


import {KuntaKoodistoContext, LanguageContext, MaatJaValtiotKoodistoContext} from '../../contexts';
import useAxios from "axios-hooks";
import Spinner from "../../Spinner";
import ErrorPage from "../../ErrorPage";

type Props = {
    organisaatio: Organisaatio,
}

const UI_FORMAT = 'dd.MM.yyyy';

export default function OrganisaationTiedot({ organisaatio }: Props) {
    const { i18n, language } = useContext(LanguageContext);
    const [{data: yritysmuodot, loading: yritysmuotoLoading, error: yritysmuodotError}]
        = useAxios<Koodi[]>('/varda-rekisterointi/api/koodisto/YRITYSMUOTO/koodi?onlyValid=true');

    const { koodisto: kuntaKoodisto } = useContext(KuntaKoodistoContext);
    const { koodisto: maatJaValtiotKoodisto } = useContext(MaatJaValtiotKoodistoContext);

    if (yritysmuotoLoading) {
        return <Spinner />;
    }
    if (yritysmuodotError) {
        return <ErrorPage>{i18n.translate('ERROR_FETCH')}</ErrorPage>;
    }

    function kotipaikka(organisaatio: Organisaatio): string {
        const osat: string[] = [];
        const kunta = kuntaKoodisto.uri2Nimi(organisaatio.kotipaikkaUri);
        const maa = maatJaValtiotKoodisto.uri2Nimi(organisaatio.maaUri);
        if (kunta) osat.push(kunta);
        if (maa) osat.push(maa);
        return osat.join(", ");
    }

    function yritysmuoto(yritysmuotoUri: string) {
        const osuvaYritysMuoto = yritysmuodot.find(y => y.uri === yritysmuotoUri);
        return osuvaYritysMuoto ? toLocalizedText(osuvaYritysMuoto.nimi, language, osuvaYritysMuoto.arvo) : i18n.translate('EI_TIEDOSSA');
    }

    return (
        <>
            <FormFieldContainer label={i18n.translate('ORGANISAATION_NIMI')}>
                <span>{organisaatio.ytjNimi.nimi}</span>
            </FormFieldContainer>
            <FormFieldContainer label={i18n.translate('YTUNNUS')}>
                <span>{organisaatio.ytunnus}</span>
            </FormFieldContainer>
            <FormFieldContainer label={i18n.translate('YRITYSMUOTO')}>
                <span>{yritysmuoto(organisaatio.yritysmuoto)}</span>
            </FormFieldContainer>
            <FormFieldContainer label={i18n.translate('KOTIPAIKKA')}>
                <span>{kotipaikka(organisaatio)}</span>
            </FormFieldContainer>
            <FormFieldContainer label={i18n.translate('TOIMINNAN_ALKAMISAIKA')}>
                <span>{format(parseISO(organisaatio.alkuPvm || ''), UI_FORMAT)}</span>
            </FormFieldContainer>
        </>
    );
}
