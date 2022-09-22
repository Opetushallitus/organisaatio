import React from 'react';
import { render, unmountComponentAtNode } from 'react-dom';
import { act } from 'react-dom/test-utils';
import createTheme from '@opetushallitus/virkailija-ui-components/createTheme';
import { ThemeProvider } from 'styled-components';
import RekisterointiListaOtsikko from './RekisterointiListaOtsikko';

const theme = createTheme();
const dummyCallback = (kaikkiValittu: boolean) => {
    /* no-op */
};

let container: Element;

describe('RekisterointiListaOtsikko', () => {
    beforeEach(() => {
        container = document.createElement('table');
        document.body.appendChild(container);
    });
    afterEach(() => {
        unmountComponentAtNode(container);
        container.remove();
    });

    it('näyttää "valitse kaikki" -checkboxin', async () => {
        await act(async () => {
            render(
                <ThemeProvider theme={theme}>
                    <RekisterointiListaOtsikko
                        valintaKaytossa={true}
                        kaikkiValittu={false}
                        kaikkiValittuCallback={dummyCallback}
                    />
                </ThemeProvider>,
                container
            );
        });
        expect(container.querySelector('thead')).not.toBeNull();
        expect(container.querySelector('#valitseKaikki')).not.toBeNull();
    });

    it('ei näytä "valitse kaikki" -checkboxia', async () => {
        await act(async () => {
            render(
                <ThemeProvider theme={theme}>
                    <RekisterointiListaOtsikko
                        valintaKaytossa={false}
                        kaikkiValittu={false}
                        kaikkiValittuCallback={dummyCallback}
                    />
                </ThemeProvider>,
                container
            );
        });
        expect(container.querySelector('thead')).not.toBeNull();
        expect(container.querySelector('#valitseKaikki')).toBeNull();
    });
});
