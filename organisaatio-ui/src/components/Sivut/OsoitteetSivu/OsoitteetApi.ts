import axios from 'axios';
import { API_CONTEXT } from '../../../contexts/constants';
import { KoodiUri } from '../../../types/types';
import { useEffect, useState } from 'react';

export type Hakutulos = {
    id: string;
    rows: HakutulosRow[];
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
    postiosoite?: string;
    kayntiosoite?: string;
};

export type HaeRequest = {
    organisaatiotyypit: KoodiUri[];
    oppilaitostyypit: KoodiUri[];
    vuosiluokat: KoodiUri[];
    kunnat: KoodiUri[];
    anyJarjestamislupa: boolean;
    jarjestamisluvat: KoodiUri[];
    kielet: KoodiUri[];
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
};

export async function haeOsoitteet(request: HaeRequest): Promise<Hakutulos> {
    const response = await axios.post<Hakutulos>(`${API_CONTEXT}/osoitteet/hae`, request);
    return response.data;
}

type ApiResult<T> =
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
    return useGET<Hakutulos>(`/osoitteet/hakutulos/${hakutulosId}`);
}

export function useHakuParametrit(): ApiResult<HakuParametrit> {
    return useGET<HakuParametrit>(`/osoitteet/parametrit`);
}

export type SendEmailRequest = {
    replyTo?: string;
    copy?: string;
    subject: string;
    body: string;
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
            .get<T>(`${API_CONTEXT}${path}`)
            .then((response) => setState({ state: 'OK', value: response.data }))
            .catch((error) => {
                console.error(error);
                setState({ state: 'ERROR', error });
            });
    }, []);
    return state;
}
