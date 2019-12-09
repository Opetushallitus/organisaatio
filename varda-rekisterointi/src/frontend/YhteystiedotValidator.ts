import {Virheet, Yhteystiedot} from "./types";
import {hasLength} from "./StringUtils";
import {validoiOsoite} from "./OsoiteValidator";

type YhteystiedotKentta = keyof Yhteystiedot;
type Osoitetyyppi = Extract<YhteystiedotKentta, 'postiosoite' | 'kayntiosoite'>;

export function validoiYhteystiedot(yhteystiedot: Yhteystiedot) {
    const virheet: Virheet = {};
    for (let kentta of ['sahkoposti', 'puhelinnumero']) {
        const arvo = yhteystiedot[kentta as YhteystiedotKentta];
        if (!hasLength(arvo as string)) {
            virheet[kentta] = 'PAKOLLINEN_TIETO';
        }
    }
    for (let osoitetyyppi of ['postiosoite', 'kayntiosoite']) {
        const osoitevirheet = validoiOsoite(yhteystiedot[osoitetyyppi as Osoitetyyppi]);
        for (let virhekentta in osoitevirheet) {
            virheet[`${osoitetyyppi}.${virhekentta}`] = osoitevirheet[virhekentta];
        }
    }
    return virheet;
}
