import React from 'react';
import { render, unmountComponentAtNode } from 'react-dom';
import { act } from 'react-dom/test-utils';
import RekisterointiLista from './RekisterointiLista';
import { Rekisterointihakemus} from './rekisterointihakemus';
import Axios from 'axios';
import { Kayttaja, Organisaatio } from '../types/types';
import createTheme from '@opetushallitus/virkailija-ui-components/createTheme';
import { ThemeProvider } from 'styled-components';

const theme = createTheme();

const dummyKayttaja: Kayttaja = {
    asiointikieli: 'fi',
    etunimi: 'Testi',
    sukunimi: 'Henkilö',
    sahkoposti: 'testi.henkilo@foo.bar',
    saateteksti: 'foo',
};
const dummyOrganisaatio: Organisaatio = {
    ytjNimi: {
        nimi: 'Oy Firma Ab',
        alkuPvm: null,
        kieli: 'fi',
    },
    ytunnus: '12345678-9',
    alkuPvm: '1999-01-01',
    kotipaikkaUri: '',
    maaUri: '',
    oid: '12345',
    tyypit: [],
    kieletUris: [],
    yhteystiedot: {
        puhelinnumero: '',
        sahkoposti: '',
        postiosoite: {
            katuosoite: '',
            postinumeroUri: '',
            postitoimipaikka: '',
        },
        kayntiosoite: {
            katuosoite: '',
            postinumeroUri: '',
            postitoimipaikka: '',
        },
    },
    yritysmuoto: '',
};

let container: Element;
describe('RekisterointiLista', () => {
    beforeEach(() => {
        container = document.createElement('div');
        document.body.appendChild(container);
    });
    afterEach(() => {
        unmountComponentAtNode(container);
        container.remove();
    });
    afterAll(() => {
        jest.restoreAllMocks();
    });

    it('tulostuu tyhjänä', async () => {
        const rekisteroinnit: Rekisterointihakemus[] = [];
        jest.spyOn(Axios, 'get').mockImplementation(() => Promise.resolve({ data: rekisteroinnit }));
        await act(async () => {
            render(
                <ThemeProvider theme={theme}>
                    <RekisterointiLista statusCallback={jest.fn} />
                </ThemeProvider>,
                container
            );
        });
        expect(container.querySelector('table.vardaRekisterointiLista')).not.toBeNull();
    });

    it('tulostaa rivejä', async () => {
        const rekisterointi: Rekisterointihakemus = {
            kunnat: [],
            toimintamuoto: '',
            kayttaja: dummyKayttaja,
            organisaatio: dummyOrganisaatio,
            sahkopostit: [],
            vastaanotettu: '',
            id: 1,
            tyyppi: 'varda',
            tila: 'KASITTELYSSA',
        };
        const rekisteroinnit = [rekisterointi];
        jest.spyOn(Axios, 'get').mockImplementation(() => Promise.resolve({ data: rekisteroinnit }));
        await act(async () => {
            render(
                <ThemeProvider theme={theme}>
                    <RekisterointiLista statusCallback={jest.fn} />
                </ThemeProvider>,
                container
            );
        });
        expect(container.querySelectorAll('table tbody tr')).toHaveLength(rekisteroinnit.length);
    });
});
