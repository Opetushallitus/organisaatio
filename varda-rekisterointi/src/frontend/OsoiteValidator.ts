import {Osoite, Virheet} from "./types";
import {hasLength} from "./StringUtils";

type OsoiteKentta = keyof Osoite;

export function validoiOsoite(osoite: Osoite): Virheet {
    const virheet: Virheet = {};
    for (let kentta of ['katuosoite', 'postinumeroUri']) {
        if (!hasLength(osoite[kentta as OsoiteKentta])) {
            virheet[kentta] = 'PAKOLLINEN_TIETO';
        }
    }
    if (!hasLength(osoite.postitoimipaikka)) {
        virheet['postinumeroUri'] = 'VIRHEELLINEN_POSTINUMERO';
    }
    return virheet;
}
