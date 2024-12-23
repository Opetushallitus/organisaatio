import assert from 'assert/strict';
import { describe, it } from 'node:test';

import { KoodistoImpl } from './contexts';
import { Koodi } from './types/types';

const koodi = {
    uri: 'koodi_1#1',
    nimi: {
        fi: 'Koodi',
        sv: 'Kod',
        en: 'Code',
    },
    arvo: '1',
};
const koodit: Koodi[] = [koodi];

describe('KoodistoImpl', () => {
    const impl = new KoodistoImpl(koodit, 'fi');

    it('löytää nimen uri:lla', () => {
        const nimi = impl.uri2Nimi(koodi.uri);
        assert.strictEqual(nimi, koodi.nimi.fi);
    });

    it('palauttaa tyhjän olemattomalla uri:lla', () => {
        const nimi = impl.uri2Nimi('eioo_1#1');
        assert.strictEqual(nimi, '');
    });

    it('löytää nimen arvolla', () => {
        const nimi = impl.arvo2Nimi(koodi.arvo);
        assert.strictEqual(nimi, koodi.nimi.fi);
    });

    it('palauttaa tyhjän olemattomalla arvolla', () => {
        const nimi = impl.arvo2Nimi('0');
        assert.strictEqual(nimi, '');
    });

    it('palauttaa kaikki nimet', () => {
        const nimet = impl.nimet();
        assert.strictEqual(nimet.length, 1);
        assert.strictEqual(nimet[0], 'Koodi');
    });

    it('palauttaa kaikki koodit', () => {
        const koodit = impl.koodit();
        assert.strictEqual(koodit.length, 1);
        assert.strictEqual(koodi.uri, 'koodi_1#1');
    });
});
