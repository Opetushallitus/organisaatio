import axios, { AxiosProgressEvent, GenericAbortSignal } from 'axios';
import { API_CONTEXT } from '../../../contexts/constants';
import { FrontProperties, KoodiUri } from '../../../types/types';
import { useEffect, useState } from 'react';
import { SerializedEditorState } from 'lexical';

interface KayttajaHakutulos {
    id: string;
    type: 'kayttaja';
    rows: KayttajaHakutulosRow[];
}

interface OrganisaatioHakutulos {
    id: string;
    type: 'organisaatio';
    rows: HakutulosRow[];
}

export type Hakutulos = OrganisaatioHakutulos | KayttajaHakutulos;

export type KayttajaHakutulosRow = {
    asiointikieli: 'FI' | 'SV';
    etunimet: string;
    kutsumanimi: string;
    oid: string;
    sahkoposti: string;
    sukunimi: string;
};

export type HakutulosRow = {
    id: number;
    oid: string;
    nimi: string;
    sahkoposti?: string;
    yritysmuoto: string;
    puhelinnumero?: string;
    opetuskieli?: string;
    oppilaitostunnus?: string;
    kunta: string;
    koskiVirheilmoituksenOsoite?: string;
    ytunnus: string;
    postiosoite?: Osoite;
    kayntiosoite?: Osoite;
};

export type Osoite = {
    osoite: string;
    postinumero?: string;
    postitoimipaikka?: string;
};

export type HaeRequest = {
    organisaatiotyypit: KoodiUri[];
    oppilaitostyypit: KoodiUri[];
    vuosiluokat: KoodiUri[];
    kunnat: KoodiUri[];
    anyJarjestamislupa: boolean;
    jarjestamisluvat: KoodiUri[];
    kielet: KoodiUri[];
    organisaatioOids: string[];
    kayttooikeusryhmat: string[];
};

export type KoodistoKoodi = {
    koodiUri: string;
    nimi: string;
};

export type MaakuntaKoodi = KoodistoKoodi & {
    kunnat: KoodiUri[];
};

export type OppilaitosRyhma = {
    nimi: string;
    koodit: string[];
};

export type Koulutustoimija = {
    oid: string;
    nimi: string;
};

export type HakuParametrit = {
    oppilaitostyypit: {
        koodit: KoodistoKoodi[];
        ryhmat: OppilaitosRyhma[];
    };
    vuosiluokat: KoodistoKoodi[];
    maakunnat: MaakuntaKoodi[];
    kunnat: KoodistoKoodi[];
    jarjestamisluvat: KoodistoKoodi[];
    kielet: KoodistoKoodi[];
    koulutustoimijat: Koulutustoimija[];
};

type KayttooikeusryhmaLocalizedText = {
    text: string;
    lang: 'FI' | 'SV' | 'EN';
};

export type Kayttooikeusryhma = {
    tunniste: string;
    nimi: {
        texts: KayttooikeusryhmaLocalizedText[];
    };
};

export type HaeKayttajatRequest = {
    oppilaitostyypit: KoodiUri[];
};

export async function haeHakutulos(request: HaeRequest): Promise<Hakutulos> {
    const response = await axios.post<Hakutulos>(`${API_CONTEXT}/osoitteet/hae`, request);
    return response.data;
}

export type ApiResult<T> =
    | {
          state: 'LOADING';
      }
    | {
          state: 'ERROR';
          error: unknown;
      }
    | {
          state: 'OK';
          value: T;
      };

export function useHakutulos(hakutulosId: string): ApiResult<Hakutulos> {
    return useGET<Hakutulos>(`${API_CONTEXT}/osoitteet/hakutulos/${hakutulosId}`);
}

export function useHakuParametrit(): ApiResult<HakuParametrit> {
    return useGET<HakuParametrit>(`${API_CONTEXT}/osoitteet/parametrit`);
}

export function useKayttooikeusryhmat(frontProperties: FrontProperties): ApiResult<Kayttooikeusryhma[]> {
    return useGET<Kayttooikeusryhma[]>(`${frontProperties.urlVirkailija}/kayttooikeus-service/kayttooikeusryhma`);
}

export type SendEmailRequest = {
    replyTo?: string;
    copy?: string;
    subject: string;
    body: SerializedEditorState;
    attachmentIds?: string[];
    selectedOids?: string[];
};

export type SendEmailResponse = {
    emailId: string;
};

export async function sendEmail(hakutulosId: string, request: SendEmailRequest): Promise<SendEmailResponse> {
    const response = await axios.post<SendEmailResponse>(
        `${API_CONTEXT}/osoitteet/hakutulos/${hakutulosId}/email`,
        request
    );
    return response.data;
}

type UploadAttachmentResponse = string;

export async function uploadAttachment(
    hakutulosId: string,
    file: File,
    onUploadProgress: (percent: number) => void,
    signal: GenericAbortSignal
): Promise<UploadAttachmentResponse> {
    const formData = new FormData();
    formData.append('file', file);
    const response = await axios.post<UploadAttachmentResponse>(
        `${API_CONTEXT}/osoitteet/hakutulos/${hakutulosId}/email/liite`,
        formData,
        {
            headers: {
                'content-type': 'multipart/form-data',
            },
            onUploadProgress: (progressEvent: AxiosProgressEvent) =>
                onUploadProgress(Math.round((progressEvent.progress ?? 0) * 100)),
            signal,
        }
    );
    return response.data;
}

export type GetEmailResponse =
    | {
          status: 'QUEUED';
          emailId: string;
      }
    | {
          status: 'SENT';
          emailId: string;
          lahetysTunniste: string;
      };

export async function getEmail(emailId: string): Promise<GetEmailResponse> {
    const response = await axios.get<GetEmailResponse>(`${API_CONTEXT}/osoitteet/viesti/${emailId}`);
    return response.data;
}

function useGET<T>(path: string): ApiResult<T> {
    const [state, setState] = useState<ApiResult<T>>({ state: 'LOADING' });
    useEffect(() => {
        axios
            .get<T>(path)
            .then((response) => setState({ state: 'OK', value: response.data }))
            .catch((error) => {
                console.error(error);
                setState({ state: 'ERROR', error });
            });
    }, []);
    return state;
}
