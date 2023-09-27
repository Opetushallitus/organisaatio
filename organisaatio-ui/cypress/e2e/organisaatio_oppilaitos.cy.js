import { organisaatio } from '../support/data';
import { BASE_PATH } from '../../src/contexts/constants';

describe('Oppilaitos specific fields', () => {
    it('Shows specific fields when oppilaitos is chosen', () => {
        cy.persistOrganisaatio(organisaatio('PARENT1', { tyypit: [`organisaatiotyyppi_01`] }), 'parentOrganisaatio1');
        cy.get('@parentOrganisaatio1').then((parent) => {
            cy.visit(`${BASE_PATH}/lomake/uusi?parentOid=${parent.body.organisaatio.oid}`);
            cy.contains('Oppilaitos', { timeout: 10000 }).should('exist');
            cy.clickRadioOrCheckbox('Oppilaitos');
            cy.contains('PERUSTIETO_OPPILAITOSKOODI').should('exist');
            cy.contains('PERUSTIETO_OPPILAITOSTYYPPI').should('exist');
            cy.contains('PERUSTIETO_MUUT_OPPILAITOSTYYPPI').should('exist');
            cy.get('a').contains('PERUSTIETO_OPPILAITOS_MUUTOKSET').should('exist'); //linkki ulkomaailmaan
            cy.contains('VUOSILUOKAT').should('not.exist');
            cy.selectFromList('PERUSTIETO_OPPILAITOSTYYPPI', 'Peruskoulut');
            cy.contains('VUOSILUOKAT').should('exist');
            cy.selectFromList('PERUSTIETO_OPPILAITOSTYYPPI', 'Lukiot');
            cy.contains('VUOSILUOKAT').should('not.exist');
            cy.selectFromList('PERUSTIETO_OPPILAITOSTYYPPI', 'Perus- ja lukioasteen koulut');
            cy.contains('VUOSILUOKAT').should('exist');
            cy.selectFromList('PERUSTIETO_OPPILAITOSTYYPPI', 'Lukiot');
            cy.contains('VUOSILUOKAT').should('not.exist');
            cy.selectFromList('PERUSTIETO_OPPILAITOSTYYPPI', 'Peruskouluasteen erityiskoulut');
            cy.contains('VUOSILUOKAT').should('exist');
            cy.selectFromList('PERUSTIETO_OPPILAITOSTYYPPI', 'Lukiot');
            cy.contains('VUOSILUOKAT').should('not.exist');
        });
    });
});
