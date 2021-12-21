import { API_CONTEXT, BASE_PATH, PUBLIC_API_CONTEXT } from '../../src/contexts/constants';
import { organisaatio } from '../support/data';

describe('Restrict buttons by roles', () => {
    const taulukkoLisaaUusi = 'TAULUKKO_LISAA_UUSI';
    const lomakeLisaaUusi = 'LISAA_UUSI';
    const oppilaitosButtons = ['SIIRRA', 'YHDISTA'];
    const koulutusToimijaButtons = ['PERUSTIETO_PAIVITA_YTJ_TIEDOT'];
    const generalRestrictedButtons = [
        'MUOKKAA_ORGANISAATION_NIMEA',
        'TALLENNA',
        'PERUSTIETO_MERKITSE_ORGANISAATIO_LAKKAUTETUKSI',
    ];
    const suljeButton = 'SULJE';
    const allRestrictedButtons = [
        lomakeLisaaUusi,
        ...oppilaitosButtons,
        ...koulutusToimijaButtons,
        ...generalRestrictedButtons,
    ];
    it('Does not show buttons without required roles', () => {
        cy.intercept('GET', `/kayttooikeus-service/cas/me`, { fixture: 'noRoles.json' });
        cy.visit(`${BASE_PATH}/organisaatiot`);
        cy.get('h2').contains('TAULUKKO_ORGANISAATIOT', { timeout: 10000 }).should('exist');
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
            cy.get('h3', { timeout: 10000 }).contains('Koulutustoimija').should('exist');
            allRestrictedButtons.forEach((a) => cy.get('button').contains(a).should('not.exist'));
            [suljeButton].forEach((a) => cy.get('button').contains(a).should('exist'));
            cy.pause();
        });
        cy.get('@child').then((org) => {
            cy.log('test for Oppilaitos');
            cy.visit(`${BASE_PATH}/lomake/${org.body.organisaatio.oid}`);
            cy.get('h3', { timeout: 10000 }).contains('Oppilaitos').should('exist');
            allRestrictedButtons.forEach((a) => cy.get('button').contains(a).should('not.exist'));
            [suljeButton].forEach((a) => cy.get('button').contains(a).should('exist'));
        });
        cy.get('@grandchild').then((org) => {
            cy.log('test for Toimipiste');
            cy.visit(`${BASE_PATH}/lomake/${org.body.organisaatio.oid}`);
            cy.get('h3', { timeout: 10000 }).contains('Toimipiste').should('exist');
            allRestrictedButtons.forEach((a) => cy.get('button').contains(a).should('not.exist'));
            [suljeButton].forEach((a) => cy.get('button').contains(a).should('exist'));
        });
    });
    it('Shows buttons with CRUD roles', () => {
        cy.intercept('GET', `/kayttooikeus-service/cas/me`, { fixture: 'CRUDRoles.json' });
        cy.visit(`${BASE_PATH}/organisaatiot`);
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
        cy.get('@parent').then((org) => {
            cy.log('test for Koulutustoimija');
            cy.visit(`${BASE_PATH}/lomake/${org.body.organisaatio.oid}`);
            cy.get('h3', { timeout: 10000 }).contains('Koulutustoimija').should('exist');
            allRestrictedButtons.forEach((a) => cy.get('button').contains(a).should('not.exist'));
            [suljeButton].forEach((a) => cy.get('button').contains(a).should('exist'));
        });
        cy.get('@child').then((org) => {
            cy.log('test for Oppilaitos');
            cy.visit(`${BASE_PATH}/lomake/${org.body.organisaatio.oid}`);
            cy.get('h3', { timeout: 10000 }).contains('Oppilaitos').should('exist');
            [...oppilaitosButtons, ...koulutusToimijaButtons, ...generalRestrictedButtons].forEach((a) =>
                cy.get('button').contains(a).should('not.exist')
            );
            [lomakeLisaaUusi, suljeButton].forEach((a) => cy.get('button').contains(a).should('exist'));
        });
        cy.get('@grandchild').then((org) => {
            cy.log('test for Toimipiste');
            cy.visit(`${BASE_PATH}/lomake/${org.body.organisaatio.oid}`);
            cy.get('h3', { timeout: 10000 }).contains('Toimipiste').should('exist');
            [...oppilaitosButtons, ...koulutusToimijaButtons].forEach((a) =>
                cy.get('button').contains(a).should('not.exist')
            );
            [lomakeLisaaUusi, ...generalRestrictedButtons, suljeButton].forEach((a) =>
                cy.get('button').contains(a).should('exist')
            );
        });
    });
    it('Shows buttons with OPH roles', () => {
        cy.intercept('GET', `/kayttooikeus-service/cas/me`, { fixture: 'OPHRoles.json' });
        cy.visit(`${BASE_PATH}/organisaatiot`);
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
            cy.get('h3', { timeout: 10000 }).contains('Koulutustoimija').should('exist');
            [...oppilaitosButtons].forEach((a) => cy.get('button').contains(a).should('not.exist'));
            [...koulutusToimijaButtons, lomakeLisaaUusi, suljeButton, ...generalRestrictedButtons].forEach((a) =>
                cy.get('button').contains(a).should('exist')
            );
        });
        cy.get('@child').then((org) => {
            cy.log('test for Oppilaitos');
            cy.visit(`${BASE_PATH}/lomake/${org.body.organisaatio.oid}`);
            cy.get('h3', { timeout: 10000 }).contains('Oppilaitos').should('exist');
            [...koulutusToimijaButtons].forEach((a) => cy.get('button').contains(a).should('not.exist'));
            [...generalRestrictedButtons, ...oppilaitosButtons, lomakeLisaaUusi, suljeButton].forEach((a) =>
                cy.get('button').contains(a).should('exist')
            );
        });
        cy.get('@grandchild').then((org) => {
            cy.log('test for Toimipiste');
            cy.visit(`${BASE_PATH}/lomake/${org.body.organisaatio.oid}`);
            cy.get('h3', { timeout: 10000 }).contains('Toimipiste').should('exist');
            [...oppilaitosButtons, ...koulutusToimijaButtons].forEach((a) =>
                cy.get('button').contains(a).should('not.exist')
            );
            [lomakeLisaaUusi, ...generalRestrictedButtons, suljeButton].forEach((a) =>
                cy.get('button').contains(a).should('exist')
            );
        });
    });
});
