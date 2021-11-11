import { KoodistoImpl } from './contexts';
import { Koodi } from '../types/types';

const koodit: Koodi[] = [
    {
        uri: 'koodi_1#1',
        nimi: {
            fi: 'Koodi',
            sv: 'Kod',
            en: 'Code',
        },
        arvo: '1',
        versio: 1,
    },
];

describe('KoodistoImpl', () => {
    const impl = new KoodistoImpl(koodit, 'fi');

    it('Finds name using a uri', () => {
        const nimi = impl.uri2Nimi(koodit[0].uri);
        expect(nimi).toEqual(koodit[0].nimi.fi);
    });

    it('Finds uri using a arvo', () => {
        const uri = impl.arvo2Uri(koodit[0].arvo);
        expect(uri).toEqual(koodit[0].uri);
    });

    it('Returns empty string if uri is not found by arvo', () => {
        const uri = impl.arvo2Uri('2');
        expect(uri).toEqual('');
    });

    it('Returns empty nimi when uri doesnt exist', () => {
        const nimi = impl.uri2Nimi('eioo_1#1');
        expect(nimi).toEqual('');
    });

    it('Finds nimi using koodiarvo', () => {
        const nimi = impl.arvo2Nimi(koodit[0].arvo);
        expect(nimi).toEqual(koodit[0].nimi.fi);
    });

    it('Returns empty nimi when arvo doesnt exist', () => {
        const nimi = impl.arvo2Nimi('0');
        expect(nimi).toEqual('');
    });

    it('Returns all names', () => {
        const nimet = impl.nimet();
        expect(nimet.length).toEqual(1);
        expect(nimet[0]).toEqual('Koodi');
    });

    it('Returns all koodis', () => {
        const koodit = impl.koodit();
        expect(koodit.length).toEqual(1);
        expect(koodit[0].uri).toEqual('koodi_1#1');
    });

    it('Returns all selectOptions', () => {
        const options = impl.selectOptions();
        expect(options.length).toEqual(1);
        expect(options[0].label).toEqual('Koodi');
    });
});
