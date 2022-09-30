import {validoiOsoite} from "./OsoiteValidator";
import {Osoite, Virheet, VirheKoodi} from "./types/types";

type OsoiteKentta = keyof Osoite;

function tarkistaKentta(virheet: Virheet, kentta: OsoiteKentta, odotettuVirhe: VirheKoodi) {
    expect(Object.keys(virheet)).toContain(kentta);
    expect(virheet[kentta]).toEqual(odotettuVirhe);
}

describe('OsoiteValidator', () => {
    it('vaatii katuosoitteen', () => {
        const osoite: Osoite = {
            katuosoite: '',
            postinumeroUri: 'posti_12345',
            postitoimipaikka: 'Humppaala'
        };
        tarkistaKentta(validoiOsoite(osoite), 'katuosoite', 'VIRHEELLINEN_OSOITE');
    });

    it('vaatii postinumeroUrlin', () => {
        const osoite: Osoite = {
            katuosoite: 'Jenkkakatu 1',
            postinumeroUri: '',
            postitoimipaikka: 'Humppaala'
        };
        tarkistaKentta(validoiOsoite(osoite), 'postinumeroUri', 'VIRHEELLINEN_POSTINUMERO');
    });

    it('vaatii postitoimipaikan', () => {
        const osoite: Osoite = {
            katuosoite: 'Jenkkakatu 1',
            postinumeroUri: 'posti_huuhaa',
            postitoimipaikka: ''
        };
        const virheet: Virheet = validoiOsoite(osoite);
        // virhe merkitään postinumerolle, koska postitoimipaikka populoidaan sen pohjalta
        tarkistaKentta(validoiOsoite(osoite), 'postinumeroUri', 'VIRHEELLINEN_POSTINUMERO');
    });
});
