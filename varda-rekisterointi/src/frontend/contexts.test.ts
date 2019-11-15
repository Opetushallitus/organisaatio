import {KoodistoImpl} from "./contexts";
import {Koodi} from "./types";

const koodit: Koodi[] = [
    {
        uri: "koodi_1#1",
        nimi: {
            fi: "Koodi",
            sv: "Kod",
            en: "Code"
        },
        arvo: "1"
    }
];


describe('KoodistoImpl', () => {
    const impl = new KoodistoImpl(koodit, "fi");

    it('löytää nimen uri:lla', () => {
        const nimi = impl.uri2Nimi(koodit[0].uri);
        expect(nimi).toEqual(koodit[0].nimi.fi);
    });

    it('palauttaa tyhjän olemattomalla uri:lla', () => {
        const nimi = impl.uri2Nimi("eioo_1#1");
        expect(nimi).toEqual("");
    });

    it('löytää nimen arvolla', () => {
        const nimi = impl.arvo2Nimi(koodit[0].arvo);
        expect(nimi).toEqual(koodit[0].nimi.fi);
    });

    it('palauttaa tyhjän olemattomalla arvolla', () => {
        const nimi = impl.arvo2Nimi("0");
        expect(nimi).toEqual("");
    });
});
