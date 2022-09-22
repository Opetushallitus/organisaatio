import { Virheet, Yhteystiedot } from './types/types';
import { isNonEmpty } from './StringUtils';
import { validoiOsoite } from './OsoiteValidator';
import EmailValidator from 'email-validator';
import * as PuhelinnumeroValidator from './PuhelinnumeroValidator';

type YhteystiedotKentta = keyof Yhteystiedot;
type Osoitetyyppi = Extract<YhteystiedotKentta, 'postiosoite' | 'kayntiosoite'>;

export function validoiYhteystiedot(yhteystiedot: Yhteystiedot) {
    const virheet: Virheet = {};
    for (let kentta of ['sahkoposti', 'puhelinnumero']) {
        const arvo = yhteystiedot[kentta as YhteystiedotKentta];
        if (!isNonEmpty(arvo as string)) {
            virheet[kentta] = 'PAKOLLINEN_TIETO';
        } else {
            if (kentta === 'sahkoposti' && !EmailValidator.validate(arvo as string)) {
                virheet[kentta] = 'VIRHEELLINEN_SAHKOPOSTI';
            }
            if (kentta === 'puhelinnumero' && !PuhelinnumeroValidator.validate(arvo as string)) {
                virheet[kentta] = 'VIRHEELLINEN_PUHELINNUMERO';
            }
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
