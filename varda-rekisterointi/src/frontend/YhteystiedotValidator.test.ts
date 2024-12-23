import assert from 'assert/strict';
import { describe, it } from 'node:test';

import { validoiYhteystiedot } from './YhteystiedotValidator';
import { Yhteystiedot, Virheet } from './types/types';
import { tyhjaOsoite } from './testTypes';

type YhteystiedotKentta = keyof Yhteystiedot;

function tarkistaPakollinenTieto(virheet: Virheet, kentta: YhteystiedotKentta) {
    assert.deepStrictEqual(Object.keys(virheet).includes(kentta), true);
    assert.deepStrictEqual(virheet[kentta], 'PAKOLLINEN_TIETO');
}

describe('YhteystiedotValidator', () => {
    it('vaatii puhelinnumeron', () => {
        const yhteystiedot: Yhteystiedot = {
            puhelinnumero: '',
            sahkoposti: 'foo@bar.baa.ri',
            postiosoite: tyhjaOsoite,
            kayntiosoite: tyhjaOsoite,
        };
        const virheet = validoiYhteystiedot(yhteystiedot);
        tarkistaPakollinenTieto(virheet, 'puhelinnumero');
    });

    it('vaatii sähköpostin', () => {
        const yhteystiedot: Yhteystiedot = {
            puhelinnumero: '0123456789',
            sahkoposti: '',
            postiosoite: tyhjaOsoite,
            kayntiosoite: tyhjaOsoite,
        };
        const virheet = validoiYhteystiedot(yhteystiedot);
        tarkistaPakollinenTieto(virheet, 'sahkoposti');
    });

    it('tarkistaa sähköpostin muodon', () => {
        const yhteystiedot: Yhteystiedot = {
            puhelinnumero: '0123456789',
            sahkoposti: 'eiolekunnonsposti',
            postiosoite: tyhjaOsoite,
            kayntiosoite: tyhjaOsoite,
        };
        const virheet = validoiYhteystiedot(yhteystiedot);
        assert.deepStrictEqual(virheet['sahkoposti'], 'VIRHEELLINEN_SAHKOPOSTI');
    });

    it('tarkistaa puhelinnumeron muodon', () => {
        const yhteystiedot: Yhteystiedot = {
            puhelinnumero: 'eiolekunnonpuhelinnumero',
            sahkoposti: 'foo@bar.baa.ri',
            postiosoite: tyhjaOsoite,
            kayntiosoite: tyhjaOsoite,
        };
        const virheet = validoiYhteystiedot(yhteystiedot);
        assert.deepStrictEqual(virheet['puhelinnumero'], 'VIRHEELLINEN_PUHELINNUMERO');
    });

    it('tarkistaa osoitteet', () => {
        const yhteystiedot: Yhteystiedot = {
            puhelinnumero: '0123456789',
            sahkoposti: 'sir.spam@a.lot',
            postiosoite: tyhjaOsoite,
            kayntiosoite: tyhjaOsoite,
        };
        const virheet = validoiYhteystiedot(yhteystiedot);
        assert.deepStrictEqual(virheet['postiosoite.katuosoite'], 'VIRHEELLINEN_OSOITE');
    });
});
