import React from 'react';
import { render, unmountComponentAtNode } from 'react-dom';
import { act } from 'react-dom/test-utils';
import createTheme from '@opetushallitus/virkailija-ui-components/createTheme';
import { ThemeProvider } from 'styled-components';
import ApprovalButtonsContainer from './ApprovalButtonsContainer';
import { Rekisterointihakemus } from '../../rekisterointihakemus';
import { dummyHakemus } from '../../../testTypes';

const theme = createTheme();
const dummyTyhjennaCallback = () => {
    /* no-op */
};

let container: Element;
describe('ApprovalButtonsContainerTest', () => {
    beforeEach(() => {
        container = document.createElement('div');
        document.body.appendChild(container);
    });
    afterEach(() => {
        unmountComponentAtNode(container);
        container.remove();
    });

    it('Disables the application buttons when there is no applications', async () => {
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
        const buttons: HTMLCollectionOf<HTMLButtonElement> = container.getElementsByTagName('button');
        expect(buttons).not.toBeNull();
        Array.from(buttons).map((b) => expect(b.hasAttribute('disabled')).toBeTruthy());
    });

    it('Enables the application buttons when there are one or more applications', async () => {
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
        const buttons: HTMLCollectionOf<HTMLButtonElement> = container.getElementsByTagName('button');
        Array.from(buttons).map((b) => expect(b.hasAttribute('disabled')).toBeFalsy());
    });
});
