import { Koodi, Koodistot } from '../types/types';
import { API_CONTEXT } from '../contexts/constants';
import axios from 'axios';
import { atom, Getter } from 'jotai';
import { casMeAtom } from './kayttooikeus';
import { KoodistoImpl } from '../contexts/KoodistoContext';
import moment from 'moment';

const baseUrl = `${API_CONTEXT}/koodisto/`;

const getKoodisto = async (koodisto: string, onlyValid?: boolean): Promise<Koodi[]> => {
    const validParameter = onlyValid ? '?onlyValid=true' : '';
    const url = `${baseUrl}${koodisto}/koodi${validParameter}`;
    const response = await axios.get<Koodi[]>(url);
    return response.data;
};
const createKoodisto = async ({
    get,
    koodi,
    onlyValid = false,
    disableOption,
}: {
    get: Getter;
    koodi: string;
    onlyValid?: boolean;
    disableOption?: (Koodi) => boolean;
}) => new KoodistoImpl({ koodisto: await getKoodisto(koodi, onlyValid), kieli: get(casMeAtom).lang, disableOption });

export const kuntaKoodistoAtom = atom(async (get) =>
    createKoodisto({
        get,
        koodi: 'KUNTA',
        disableOption: (koodi: Koodi) => {
            return koodi.tila === 'PASSIIVINEN' || moment(koodi.voimassaLoppuPvm, 'yyyy-MM-DD').isBefore(moment());
        },
    })
);
export const kayttoRyhmatKoodistoAtom = atom(async (get) => createKoodisto({ get, koodi: 'KAYTTORYHMAT' }));
export const ryhmaTyypitKoodistoAtom = atom(async (get) => createKoodisto({ get, koodi: 'RYHMATYYPIT' }));
export const organisaatioTyypitKoodistoAtom = atom(async (get) => createKoodisto({ get, koodi: 'ORGANISAATIOTYYPPI' }));
export const ryhmanTilaKoodistoAtom = atom(async (get) => createKoodisto({ get, koodi: 'RYHMANTILA' }));
export const oppilaitoksenOpetuskieletKoodistoAtom = atom(async (get) =>
    createKoodisto({ get, koodi: 'OPPILAITOKSENOPETUSKIELI' })
);
export const maatJaValtiotKoodistoAtom = atom(async (get) => createKoodisto({ get, koodi: 'MAATJAVALTIOT1' }));
export const vuosiluokatKoodistoAtom = atom(async (get) => createKoodisto({ get, koodi: 'VUOSILUOKAT' }));
export const oppilaitostyyppiKoodistoAtom = atom(async (get) => createKoodisto({ get, koodi: 'OPPILAITOSTYYPPI' }));
export const vardatoimintamuotoKoodistoAtom = atom(async (get) => createKoodisto({ get, koodi: 'VARDATOIMINTAMUOTO' }));
export const vardakasvatusopillinenjarjestelmaKoodistoAtom = atom(async (get) =>
    createKoodisto({ get, koodi: 'VARDAKASVATUSOPILLINENJARJESTELMA' })
);
export const vardatoiminnallinenpainotusKoodistoAtom = atom(async (get) =>
    createKoodisto({ get, koodi: 'VARDATOIMINNALLINENPAINOTUS' })
);
export const vardajarjestamismuotoKoodistoAtom = atom(async (get) =>
    createKoodisto({ get, koodi: 'VARDAJARJESTAMISMUOTO' })
);
export const kielikoodistoAtom = atom(async (get) => createKoodisto({ get, koodi: 'KIELI' }));
export const postinumerotKoodistoAtom = atom(async (get) => createKoodisto({ get, koodi: 'POSTI' }));

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
