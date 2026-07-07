import { Koodi, Koodisto, Koodistot } from '../types/types';
import { API_CONTEXT } from '../contexts/constants';
import axios from 'axios';
import { Atom, atom } from 'jotai';
import { casMeLangAtom } from './kayttooikeus';
import { KoodistoImpl } from '../contexts/KoodistoContext';
import { isBefore } from 'date-fns/isBefore';
import { API_DATE_FORMAT, parseDateInput } from '../tools/dateUtils';

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
type NormalizedParam = {
    koodi: string;
    onlyValid: boolean;
    disableOption?: (koodi: Koodi) => boolean;
};
const koodistoAtoms: { param: NormalizedParam; value: Atom<Promise<Koodisto>> }[] = [];
const koodistoAtom = ({ koodi, onlyValid = false, disableOption }: Param): Atom<Promise<Koodisto>> => {
    const param = { koodi, onlyValid, disableOption };
    const cached = koodistoAtoms.find(
        ({ param: cachedParam }) =>
            cachedParam.koodi === param.koodi &&
            cachedParam.onlyValid === param.onlyValid &&
            cachedParam.disableOption === param.disableOption
    );
    if (cached) {
        return cached.value;
    }
    const value = atom(async (get) => {
        const lang = await get(casMeLangAtom);
        const koodisto = await getKoodisto(koodi, onlyValid);
        return new KoodistoImpl({ koodisto, kieli: lang, disableOption });
    });
    koodistoAtoms.push({ param, value });
    return value;
};

export const kuntaKoodistoAtom = koodistoAtom({
    koodi: 'KUNTA',
    disableOption: (koodi: Koodi) => {
        const voimassaLoppuPvm = koodi.voimassaLoppuPvm
            ? parseDateInput(koodi.voimassaLoppuPvm, API_DATE_FORMAT)
            : undefined;
        return (
            koodi.tila === 'PASSIIVINEN' ||
            (!!koodi.voimassaLoppuPvm &&
                koodi.voimassaLoppuPvm !== '1990-01-01' &&
                !!voimassaLoppuPvm &&
                isBefore(voimassaLoppuPvm, new Date()))
        );
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

export const koodistotAtom = atom(async (get): Promise<Koodistot> => ({
    kuntaKoodisto: await get(kuntaKoodistoAtom),
    maatJaValtiotKoodisto: await get(maatJaValtiotKoodistoAtom),
    oppilaitoksenOpetuskieletKoodisto: await get(oppilaitoksenOpetuskieletKoodistoAtom),
    oppilaitostyyppiKoodisto: await get(oppilaitostyyppiKoodistoAtom),
    vuosiluokatKoodisto: await get(vuosiluokatKoodistoAtom),
    kayttoRyhmatKoodisto: await get(kayttoRyhmatKoodistoAtom),
    ryhmaTyypitKoodisto: await get(ryhmaTyypitKoodistoAtom),
    organisaatioTyypitKoodisto: await get(organisaatioTyypitKoodistoAtom),
    ryhmanTilaKoodisto: await get(ryhmanTilaKoodistoAtom),
    vardatoimintamuotoKoodisto: await get(vardatoimintamuotoKoodistoAtom),
    vardakasvatusopillinenjarjestelmaKoodisto: await get(vardakasvatusopillinenjarjestelmaKoodistoAtom),
    vardatoiminnallinenpainotusKoodisto: await get(vardatoiminnallinenpainotusKoodistoAtom),
    vardajarjestamismuotoKoodisto: await get(vardajarjestamismuotoKoodistoAtom),
    kielikoodisto: await get(kielikoodistoAtom),
    postinumerotKoodisto: await get(postinumerotKoodistoAtom),
}));
