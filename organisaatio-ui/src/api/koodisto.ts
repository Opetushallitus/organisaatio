import { Koodi, Koodisto, Koodistot } from '../types/types';
import { API_CONTEXT } from '../contexts/constants';
import axios from 'axios';
import { Atom, atom } from 'jotai';
import { casMeLangAtom } from './kayttooikeus';
import { KoodistoImpl } from '../contexts/KoodistoContext';
import moment from 'moment';
import { atomFamily } from 'jotai/utils';

const baseUrl = `${API_CONTEXT}/koodisto/`;
export const ORGANIAATIOTYYPPI_OPETUSHALLITUS = 'opetushallitus';
export const ORGANIAATIOTYYPPI_KOULUTUSTOIMIJA = 'organisaatiotyyppi_01';
export const ORGANIAATIOTYYPPI_OPPILAITOS = 'organisaatiotyyppi_02';
export const ORGANIAATIOTYYPPI_TOIMIPISTE = 'organisaatiotyyppi_03';
export const ORGANIAATIOTYYPPI_OPPISOPIMUSTOIMIPISTE = 'organisaatiotyyppi_04';
export const ORGANIAATIOTYYPPI_MUU_ORGANISAATIO = 'organisaatiotyyppi_05';
export const ORGANIAATIOTYYPPI_TYOELAMAJARJESTO = 'organisaatiotyyppi_06';
export const ORGANIAATIOTYYPPI_VARHAISKASVATUKSEN_JARJESTAJA = 'organisaatiotyyppi_07';
export const ORGANIAATIOTYYPPI_VARHAISKASVATUKSEN_TOIMIPAIKKA = 'organisaatiotyyppi_08';
export const ORGANIAATIOTYYPPI_KUNTA = 'organisaatiotyyppi_09';
const getKoodisto = async (koodisto: string, onlyValid?: boolean): Promise<Koodi[]> => {
    const validParameter = onlyValid ? '?onlyValid=true' : '';
    const url = `${baseUrl}${koodisto}/koodi${validParameter}`;
    const response = await axios.get<Koodi[]>(url);
    return response.data;
};
type Param = {
    koodi: string;
    onlyValid?: boolean;
    disableOption?: (koodi: Koodi) => boolean;
};
const koodistoAtom = atomFamily(
    ({ koodi, onlyValid = false, disableOption }: Param): Atom<Promise<Koodisto>> =>
        atom(async (get) => {
            const lang = get(casMeLangAtom);
            const koodisto = await getKoodisto(koodi, onlyValid);
            return new KoodistoImpl({ koodisto, kieli: lang, disableOption });
        }),
    (a, b) => a.koodi === b.koodi && a.onlyValid === b.onlyValid && a.disableOption === b.disableOption
);

export const kuntaKoodistoAtom = koodistoAtom({
    koodi: 'KUNTA',
    disableOption: (koodi: Koodi) => {
        return koodi.tila === 'PASSIIVINEN' || moment(koodi.voimassaLoppuPvm, 'yyyy-MM-DD').isBefore(moment());
    },
});
export const kayttoRyhmatKoodistoAtom = koodistoAtom({ koodi: 'KAYTTORYHMAT' });
export const ryhmaTyypitKoodistoAtom = koodistoAtom({ koodi: 'RYHMATYYPIT' });
export const organisaatioTyypitKoodistoAtom = koodistoAtom({ koodi: 'ORGANISAATIOTYYPPI' });
export const ryhmanTilaKoodistoAtom = koodistoAtom({ koodi: 'RYHMANTILA' });
export const oppilaitoksenOpetuskieletKoodistoAtom = koodistoAtom({ koodi: 'OPPILAITOKSENOPETUSKIELI' });
export const maatJaValtiotKoodistoAtom = koodistoAtom({ koodi: 'MAATJAVALTIOT1' });
export const vuosiluokatKoodistoAtom = koodistoAtom({ koodi: 'VUOSILUOKAT' });
export const oppilaitostyyppiKoodistoAtom = koodistoAtom({ koodi: 'OPPILAITOSTYYPPI' });
export const vardatoimintamuotoKoodistoAtom = koodistoAtom({ koodi: 'VARDATOIMINTAMUOTO' });
export const vardakasvatusopillinenjarjestelmaKoodistoAtom = koodistoAtom({
    koodi: 'VARDAKASVATUSOPILLINENJARJESTELMA',
});
export const vardatoiminnallinenpainotusKoodistoAtom = koodistoAtom({ koodi: 'VARDATOIMINNALLINENPAINOTUS' });
export const vardajarjestamismuotoKoodistoAtom = koodistoAtom({ koodi: 'VARDAJARJESTAMISMUOTO' });
export const kielikoodistoAtom = koodistoAtom({ koodi: 'KIELI' });
export const postinumerotKoodistoAtom = koodistoAtom({ koodi: 'POSTI' });

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
