import React from 'react';
import { render, unmountComponentAtNode } from 'react-dom';
import { act } from 'react-dom/test-utils';
import createTheme from '@opetushallitus/virkailija-ui-components/createTheme';
import { ThemeProvider } from 'styled-components';
import ApprovalButtonsContainer from './ApprovalButtonsContainer';
import { Rekisterointihakemus } from '../../rekisterointihakemus';
import { dummyHakemus } from '../../../testTypes';

const theme = createTheme();
const buttonIds = ['hylkaaButton', 'hyvaksyButton'];
const dummyTyhjennaCallback = () => {
    /* no-op */
};

let container: Element;
describe('PaatosKontrollit', () => {
    beforeEach(() => {
        container = document.createElement('div');
        document.body.appendChild(container);
    });
    afterEach(() => {
        unmountComponentAtNode(container);
        container.remove();
    });

    it('disabloi Buttonit, kun ei valittuja hakemuksia', async () => {
        await act(async () => {
            render(
                <ThemeProvider theme={theme}>
                    <ApprovalButtonsContainer
                        chosenRekisteroinnit={[]}
                        valitutKasiteltyCallback={dummyTyhjennaCallback}
                    />
                </ThemeProvider>,
                container
            );
        });
        buttonIds.forEach((id) => {
            const button: Element | null = container.querySelector(`#${id}`);
            expect(button).not.toBeNull();
            if (button !== null) {
                expect(button.hasAttribute('disabled')).toBeTruthy();
            }
        });
    });

    it('enabloi Buttonit, kun hakemuksia valittu', async () => {
        const hakemus: Rekisterointihakemus = dummyHakemus;
        await act(async () => {
            render(
                <ThemeProvider theme={theme}>
                    <ApprovalButtonsContainer
                        chosenRekisteroinnit={[hakemus]}
                        valitutKasiteltyCallback={dummyTyhjennaCallback}
                    />
                </ThemeProvider>,
                container
            );
        });
        buttonIds.forEach((id) => {
            const button: Element | null = container.querySelector(`#${id}`);
            expect(button).not.toBeNull();
            if (button !== null) {
                expect(button.hasAttribute('disabled')).toBeFalsy();
            }
        });
    });
});
