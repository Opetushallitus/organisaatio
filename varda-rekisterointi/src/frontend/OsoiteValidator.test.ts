import {validoiOsoite} from "./OsoiteValidator";
import {Osoite, Virheet} from "./types";

type OsoiteKentta = keyof Osoite;

function tarkistaPakollinenTieto(virheet: Virheet, kentta: OsoiteKentta) {
    expect(Object.keys(virheet)).toContain(kentta);
    expect(virheet[kentta]).toEqual('PAKOLLINEN_TIETO');
}

describe('OsoiteValidator', () => {
    it('vaatii katuosoitteen', () => {
        const osoite: Osoite = {
            katuosoite: '',
            postinumeroUri: 'posti_12345',
            postitoimipaikka: 'Humppaala'
        };
        tarkistaPakollinenTieto(validoiOsoite(osoite), 'katuosoite');
    });

    it('vaatii postinumeroUrlin', () => {
        const osoite: Osoite = {
            katuosoite: 'Jenkkakatu 1',
            postinumeroUri: '',
            postitoimipaikka: 'Humppaala'
        };
        tarkistaPakollinenTieto(validoiOsoite(osoite), 'postinumeroUri');
    });

    it('vaatii postitoimipaikan', () => {
        const osoite: Osoite = {
            katuosoite: 'Jenkkakatu 1',
            postinumeroUri: 'posti_huuhaa',
            postitoimipaikka: ''
        };
        const virheet: Virheet = validoiOsoite(osoite);
        // virhe merkitään postinumerolle, koska postitoimipaikka populoidaan sen pohjalta
        expect(Object.keys(virheet)).toContain('postinumeroUri');
        expect(virheet.postinumeroUri).toEqual('VIRHEELLINEN_POSTINUMERO');
    });
});
