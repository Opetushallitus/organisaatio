import { test as authenticatedTest } from './baseTest';

import { JotpaLandingFn } from '../pages/jotpa/JotpaLanding';
import { JotpaOrganisaatioFn } from '../pages/jotpa/JotpaOrganisaatio';
import { JotpaPaakayttajaFn } from '../pages/jotpa/JotpaPaakayttaja';
import { JotpaYhteenvetoFn } from '../pages/jotpa/JotpaYhteenveto';
import { JotpaValmisFn } from '../pages/jotpa/JotpaValmis';

type JotpaTest = {
    pages: {
        landing: ReturnType<typeof JotpaLandingFn>;
        organisaatio: ReturnType<typeof JotpaOrganisaatioFn>;
        paakayttaja: ReturnType<typeof JotpaPaakayttajaFn>;
        yhteenveto: ReturnType<typeof JotpaYhteenvetoFn>;
        valmis: ReturnType<typeof JotpaValmisFn>;
    };
};

export const test = authenticatedTest.extend<JotpaTest>({
    pages: async ({ page }, use) => {
        const pages = {
            landing: JotpaLandingFn(page),
            organisaatio: JotpaOrganisaatioFn(page),
            paakayttaja: JotpaPaakayttajaFn(page),
            yhteenveto: JotpaYhteenvetoFn(page),
            valmis: JotpaValmisFn(page),
        };
        await use(pages);
    },
});
