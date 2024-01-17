import axios from 'axios';
import { API_CONTEXT } from '../../../contexts/constants';
import { KoodiUri } from '../../../types/types';

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

export async function haeHakuParametrit() {
    const response = await axios.get<HakuParametrit>(`${API_CONTEXT}/osoitteet/parametrit`);
    return response.data;
}

export type SendEmailRequest = {
    replyTo?: string;
    copy?: string;
    subject: string;
    body: string;
};

export type SendEmailResponse = {
    emailId: string;
    status: 'QUEUED' | 'SENT';
};

export async function sendEmail(hakutulosId: string, request: SendEmailRequest): Promise<SendEmailResponse> {
    const response = await axios.post<SendEmailResponse>(
        `${API_CONTEXT}/osoitteet/hakutulos/${hakutulosId}/email`,
        request
    );
    return response.data;
}

export type QueuedEmail = {
    hakutulosId: string;
    id: string;
    status: string;
    recipients: string[];
    replyTo: string;
    subject: string;
    body: string;
    lahetysTunniste: string;
    created: string;
    modified: string;
};

export async function getEmail(emailId: string): Promise<QueuedEmail> {
    const response = await axios.get<QueuedEmail>(`${API_CONTEXT}/osoitteet/viesti/${emailId}`);
    return response.data;
}
