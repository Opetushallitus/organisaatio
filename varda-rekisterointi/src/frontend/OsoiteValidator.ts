import {Osoite, Virheet} from "./types";
import {isNonEmpty, hasLengthBetween} from "./StringUtils";

type OsoiteKentta = keyof Osoite;
const minLenght = 3;
const maxLength = 100;
const zipCodePattern = /^posti_\d{5}$/

export function validoiOsoite(osoite: Osoite): Virheet {
    const virheet: Virheet = {};
    if (!hasLengthBetween(osoite.katuosoite, minLenght, maxLength)) {
        virheet['katuosoite'] = 'VIRHEELLINEN_OSOITE';
    }
    if (!zipCodePattern.test(osoite.postinumeroUri) || !isNonEmpty(osoite.postitoimipaikka)) {
        virheet['postinumeroUri'] = 'VIRHEELLINEN_POSTINUMERO';
    }
    return virheet;
}
