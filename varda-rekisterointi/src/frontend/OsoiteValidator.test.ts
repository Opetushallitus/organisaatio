import assert from 'assert/strict';
import { describe, it } from 'node:test';

import { validoiOsoite } from './OsoiteValidator';
import { Osoite, Virheet, VirheKoodi } from './types/types';

type OsoiteKentta = keyof Osoite;

function tarkistaKentta(virheet: Virheet, kentta: OsoiteKentta, odotettuVirhe: VirheKoodi) {
    assert.deepStrictEqual(Object.keys(virheet).includes(kentta), true);
    assert.deepStrictEqual(virheet[kentta], odotettuVirhe);
}

describe('OsoiteValidator', () => {
    it('vaatii katuosoitteen', () => {
        const osoite: Osoite = {
            katuosoite: '',
            postinumeroUri: 'posti_12345',
            postitoimipaikka: 'Humppaala',
        };
        tarkistaKentta(validoiOsoite(osoite), 'katuosoite', 'VIRHEELLINEN_OSOITE');
    });

    it('vaatii postinumeroUrlin', () => {
        const osoite: Osoite = {
            katuosoite: 'Jenkkakatu 1',
            postinumeroUri: '',
            postitoimipaikka: 'Humppaala',
        };
        tarkistaKentta(validoiOsoite(osoite), 'postinumeroUri', 'VIRHEELLINEN_POSTINUMERO');
    });

    it('vaatii postitoimipaikan', () => {
        const osoite: Osoite = {
            katuosoite: 'Jenkkakatu 1',
            postinumeroUri: 'posti_huuhaa',
            postitoimipaikka: '',
        };
        // virhe merkitään postinumerolle, koska postitoimipaikka populoidaan sen pohjalta
        tarkistaKentta(validoiOsoite(osoite), 'postinumeroUri', 'VIRHEELLINEN_POSTINUMERO');
    });
});
