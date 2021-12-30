import { API_CONTEXT, BASE_PATH, PUBLIC_API_CONTEXT } from '../../src/contexts/constants';
import { organisaatio } from '../support/data';
import exp from 'constants';

describe('Restrict buttons by roles', () => {
    const baseCasMe = {
        uid: 'mbender',
        oid: '1.2.246.562.24.41721051355',
        firstName: 'Mike',
        lastName: 'Bender',
        groups: ['LANG_fi', 'VIRKAILIJA'],
        roles: '["APP_ORGANISAATIOHALLINTA","APP_ORGANISAATIOHALLINTA_CRUD","LANG_fi","USER_mbender","VIRKAILIJA"]',
        lang: 'fi',
    };
    const taulukkoLisaaUusi = 'TAULUKKO_LISAA_UUSI';
    const lomakeLisaaUusi = 'LISAA_UUSI';
    const tallenna = 'TALLENNA';
    const oppilaitosButtons = ['SIIRRA', 'YHDISTA'];
    const koulutusToimijaButtons = ['PERUSTIETO_PAIVITA_YTJ_TIEDOT'];
    const generalRestrictedButtons = ['MUOKKAA_ORGANISAATION_NIMEA', 'PERUSTIETO_MERKITSE_ORGANISAATIO_LAKKAUTETUKSI'];
    const suljeButton = 'SULJE';
    const allRestrictedButtons = [
        tallenna,
        lomakeLisaaUusi,
        ...oppilaitosButtons,
        ...koulutusToimijaButtons,
        ...generalRestrictedButtons,
    ];
    it('Does not show buttons without required roles', () => {
        cy.intercept('GET', `/kayttooikeus-service/cas/me`, {
            ...baseCasMe,
            roles: '["APP_ORGANISAATIOHALLINTA","APP_ORGANISAATIOHALLINTA_CRUD"]',
        });
        cy.visit(`${BASE_PATH}/organisaatiot`);
        cy.get('h2', { timeout: 20000 }).contains('TAULUKKO_ORGANISAATIOT').should('exist');
        cy.get('button').contains(taulukkoLisaaUusi).should('not.exist');

        cy.persistOrganisaatio(organisaatio('PARENT1', { tyypit: [`organisaatiotyyppi_01`] }), 'parent');
        cy.get('@parent').then((parentOrganisaatio) => {
            cy.persistOrganisaatio(
                organisaatio('CHILD', {
                    parentOid: parentOrganisaatio.body.organisaatio.oid,
                    tyypit: [`organisaatiotyyppi_02`],
                }),
                'child'
            );
        });
        cy.get('@child').then((parentOrganisaatio) => {
            cy.persistOrganisaatio(
                organisaatio('GRANDCHILD', {
                    parentOid: parentOrganisaatio.body.organisaatio.oid,
                    tyypit: [`organisaatiotyyppi_03`],
                }),
                'grandchild'
            );
        });

        cy.get('@parent').then((org) => {
            cy.log('test for Koulutustoimija');
            cy.visit(`${BASE_PATH}/lomake/${org.body.organisaatio.oid}`);
            cy.get('h3', { timeout: 20000 }).contains('Koulutustoimija').should('exist');
            allRestrictedButtons.forEach((a) => cy.get('button').contains(a).should('not.exist'));
            [suljeButton].forEach((a) => cy.get('button').contains(a).should('exist'));
        });
        cy.get('@child').then((org) => {
            cy.log('test for Oppilaitos');
            cy.visit(`${BASE_PATH}/lomake/${org.body.organisaatio.oid}`);
            cy.get('h3', { timeout: 20000 }).contains('Oppilaitos').should('exist');
            allRestrictedButtons.forEach((a) => cy.get('button').contains(a).should('not.exist'));
            [suljeButton].forEach((a) => cy.get('button').contains(a).should('exist'));
        });
        cy.get('@grandchild').then((org) => {
            cy.log('test for Toimipiste');
            cy.visit(`${BASE_PATH}/lomake/${org.body.organisaatio.oid}`);
            cy.get('h3', { timeout: 20000 }).contains('Toimipiste').should('exist');
            allRestrictedButtons.forEach((a) => cy.get('button').contains(a).should('not.exist'));
            [suljeButton].forEach((a) => cy.get('button').contains(a).should('exist'));
        });
    });
    it('Shows buttons with CRUD roles', () => {
        cy.intercept('GET', `/kayttooikeus-service/cas/me`, baseCasMe);
        cy.visit(`${BASE_PATH}/organisaatiot`);
        cy.get('h2', { timeout: 20000 }).contains('TAULUKKO_ORGANISAATIOT').should('exist');
        cy.contains(taulukkoLisaaUusi).should('not.exist');
        cy.persistOrganisaatio(organisaatio('PARENT1', { tyypit: [`organisaatiotyyppi_01`] }), 'parent');
        cy.get('@parent').then((parentOrganisaatio) => {
            cy.persistOrganisaatio(
                organisaatio('CHILD', {
                    parentOid: parentOrganisaatio.body.organisaatio.oid,
                    tyypit: [`organisaatiotyyppi_02`],
                }),
                'child'
            );
        });
        cy.get('@child').then((parentOrganisaatio) => {
            cy.persistOrganisaatio(
                organisaatio('GRANDCHILD', {
                    parentOid: parentOrganisaatio.body.organisaatio.oid,
                    tyypit: [`organisaatiotyyppi_03`],
                }),
                'grandchild'
            );
        });

        cy.get('@parent').then((parent) => {
            cy.intercept('GET', `/kayttooikeus-service/cas/me`, {
                ...baseCasMe,
                roles: `["APP_ORGANISAATIOHALLINTA_CRUD","APP_ORGANISAATIOHALLINTA_CRUD_${parent.body.organisaatio.oid}"]`,
            });
            cy.log('test for Koulutustoimija');
            cy.visit(`${BASE_PATH}/lomake/${parent.body.organisaatio.oid}`);
            cy.get('h3', { timeout: 20000 }).contains('Koulutustoimija').should('exist');
            allRestrictedButtons.forEach((a) => cy.get('button').contains(a).should('not.exist'));
            [suljeButton].forEach((a) => cy.get('button').contains(a).should('exist'));
        });
        cy.get('@child').then((org) => {
            cy.log('test for Oppilaitos');
            cy.visit(`${BASE_PATH}/lomake/${org.body.organisaatio.oid}`);
            cy.get('h3', { timeout: 20000 }).contains('Oppilaitos').should('exist');
            [...oppilaitosButtons, ...koulutusToimijaButtons, ...generalRestrictedButtons].forEach((a) =>
                cy.get('button').contains(a).should('not.exist')
            );
            [lomakeLisaaUusi, suljeButton, tallenna].forEach((a) => cy.get('button').contains(a).should('exist'));
        });
        cy.get('@grandchild').then((org) => {
            cy.log('test for Toimipiste');
            cy.visit(`${BASE_PATH}/lomake/${org.body.organisaatio.oid}`);
            cy.get('h3', { timeout: 20000 }).contains('Toimipiste').should('exist');
            [...oppilaitosButtons, ...koulutusToimijaButtons].forEach((a) =>
                cy.get('button').contains(a).should('not.exist')
            );
            [lomakeLisaaUusi, ...generalRestrictedButtons, suljeButton, tallenna].forEach((a) =>
                cy.get('button').contains(a).should('exist')
            );
        });
    });
    it('Shows buttons with OPH roles', () => {
        cy.intercept('GET', `/kayttooikeus-service/cas/me`, { fixture: 'OPHRoles.json' });
        cy.visit(`${BASE_PATH}/organisaatiot`);
        cy.get('h2', { timeout: 20000 }).contains('TAULUKKO_ORGANISAATIOT').should('exist');
        cy.contains(taulukkoLisaaUusi).should('exist');
        cy.persistOrganisaatio(organisaatio('PARENT1', { tyypit: [`organisaatiotyyppi_01`] }), 'parent');
        cy.get('@parent').then((parentOrganisaatio) => {
            cy.persistOrganisaatio(
                organisaatio('CHILD', {
                    parentOid: parentOrganisaatio.body.organisaatio.oid,
                    tyypit: [`organisaatiotyyppi_02`],
                }),
                'child'
            );
        });
        cy.get('@child').then((parentOrganisaatio) => {
            cy.persistOrganisaatio(
                organisaatio('GRANDCHILD', {
                    parentOid: parentOrganisaatio.body.organisaatio.oid,
                    tyypit: [`organisaatiotyyppi_03`],
                }),
                'grandchild'
            );
        });
        cy.get('@parent').then((org) => {
            cy.log('test for Koulutustoimija');
            cy.visit(`${BASE_PATH}/lomake/${org.body.organisaatio.oid}`);
            cy.get('h3', { timeout: 20000 }).contains('Koulutustoimija').should('exist');
            [...oppilaitosButtons].forEach((a) => cy.get('button').contains(a).should('not.exist'));
            [
                ...koulutusToimijaButtons,
                lomakeLisaaUusi,
                suljeButton,
                ...generalRestrictedButtons,
                tallenna,
            ].forEach((a) => cy.get('button').contains(a).should('exist'));
        });
        cy.get('@child').then((org) => {
            cy.log('test for Oppilaitos');
            cy.visit(`${BASE_PATH}/lomake/${org.body.organisaatio.oid}`);
            cy.get('h3', { timeout: 20000 }).contains('Oppilaitos').should('exist');
            [...koulutusToimijaButtons].forEach((a) => cy.get('button').contains(a).should('not.exist'));
            [...generalRestrictedButtons, ...oppilaitosButtons, lomakeLisaaUusi, suljeButton, tallenna].forEach((a) =>
                cy.get('button').contains(a).should('exist')
            );
        });
        cy.get('@grandchild').then((org) => {
            cy.log('test for Toimipiste');
            cy.visit(`${BASE_PATH}/lomake/${org.body.organisaatio.oid}`);
            cy.get('h3', { timeout: 20000 }).contains('Toimipiste').should('exist');
            [...oppilaitosButtons, ...koulutusToimijaButtons].forEach((a) =>
                cy.get('button').contains(a).should('not.exist')
            );
            [lomakeLisaaUusi, ...generalRestrictedButtons, suljeButton, tallenna].forEach((a) =>
                cy.get('button').contains(a).should('exist')
            );
        });
    });
});
