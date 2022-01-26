import { Koodi, Koodistot } from '../types/types';
import { API_CONTEXT } from '../contexts/constants';
import axios from 'axios';
import { atom } from 'jotai';
import { casMeAtom } from './kayttooikeus';
import { KoodistoImpl } from '../contexts/KoodistoContext';

const baseUrl = `${API_CONTEXT}/koodisto/`;

const getKoodisto = async (koodisto: string, onlyValid?: boolean): Promise<Koodi[]> => {
    const validParameter = onlyValid ? '?onlyValid=true' : '';
    const url = `${baseUrl}${koodisto}/koodi${validParameter}`;
    const response = await axios.get<Koodi[]>(url);
    return response.data;
};
const createKoodisto = async (get, koodi, onlyValid = false) =>
    new KoodistoImpl(await getKoodisto(koodi, onlyValid), get(casMeAtom).lang);

export const kuntaKoodistoAtom = atom(async (get) => createKoodisto(get, 'KUNTA'));
export const kayttoRyhmatKoodistoAtom = atom(async (get) => createKoodisto(get, 'KAYTTORYHMAT'));
export const ryhmaTyypitKoodistoAtom = atom(async (get) => createKoodisto(get, 'RYHMATYYPIT'));
export const organisaatioTyypitKoodistoAtom = atom(async (get) => createKoodisto(get, 'ORGANISAATIOTYYPPI'));
export const ryhmanTilaKoodistoAtom = atom(async (get) => createKoodisto(get, 'RYHMANTILA'));
export const oppilaitoksenOpetuskieletKoodistoAtom = atom(async (get) =>
    createKoodisto(get, 'OPPILAITOKSENOPETUSKIELI')
);
export const maatJaValtiotKoodistoAtom = atom(async (get) => createKoodisto(get, 'MAATJAVALTIOT1'));
export const vuosiluokatKoodistoAtom = atom(async (get) => createKoodisto(get, 'VUOSILUOKAT'));
export const oppilaitostyyppiKoodistoAtom = atom(async (get) => createKoodisto(get, 'OPPILAITOSTYYPPI'));
export const vardatoimintamuotoKoodistoAtom = atom(async (get) => createKoodisto(get, 'VARDATOIMINTAMUOTO'));
export const vardakasvatusopillinenjarjestelmaKoodistoAtom = atom(async (get) =>
    createKoodisto(get, 'VARDAKASVATUSOPILLINENJARJESTELMA')
);
export const vardatoiminnallinenpainotusKoodistoAtom = atom(async (get) =>
    createKoodisto(get, 'VARDATOIMINNALLINENPAINOTUS')
);
export const vardajarjestamismuotoKoodistoAtom = atom(async (get) => createKoodisto(get, 'VARDAJARJESTAMISMUOTO'));
export const kielikoodistoAtom = atom(async (get) => createKoodisto(get, 'KIELI'));
export const postinumerotKoodistoAtom = atom(async (get) => createKoodisto(get, 'POSTI'));

export const koodistotAtom = atom<Koodistot>((get) => ({
    kuntaKoodisto: get(kuntaKoodistoAtom),
    maatJaValtiotKoodisto: get(maatJaValtiotKoodistoAtom),
    oppilaitoksenOpetuskieletKoodisto: get(oppilaitoksenOpetuskieletKoodistoAtom),
    oppilaitostyyppiKoodisto: get(oppilaitostyyppiKoodistoAtom),
    vuosiluokatKoodisto: get(vuosiluokatKoodistoAtom),
    kayttoRyhmatKoodisto: get(kayttoRyhmatKoodistoAtom),
    ryhmaTyypitKoodisto: get(ryhmaTyypitKoodistoAtom),
    organisaatioTyypitKoodisto: get(organisaatioTyypitKoodistoAtom),
    ryhmanTilaKoodisto: get(ryhmanTilaKoodistoAtom),
    vardatoimintamuotoKoodisto: get(vardatoimintamuotoKoodistoAtom),
    vardakasvatusopillinenjarjestelmaKoodisto: get(vardakasvatusopillinenjarjestelmaKoodistoAtom),
    vardatoiminnallinenpainotusKoodisto: get(vardatoiminnallinenpainotusKoodistoAtom),
    vardajarjestamismuotoKoodisto: get(vardajarjestamismuotoKoodistoAtom),
    kielikoodisto: get(kielikoodistoAtom),
    postinumerotKoodisto: get(postinumerotKoodistoAtom),
}));
