import { organisaatio } from '../support/data';
import { BASE_PATH } from '../../src/contexts/constants';

describe('Oppilaitos koski posti', () => {
    it('Shows koski posti oppilaitos is chosen', () => {
        cy.persistOrganisaatio(organisaatio('PARENT1', { tyypit: [`organisaatiotyyppi_01`] }), 'parentOrganisaatio1');
        //persis child
        cy.get('@parentOrganisaatio1').then((parentOrganisaatio) => {
            cy.persistOrganisaatio(
                organisaatio('CHILD', {
                    parentOid: parentOrganisaatio.body.organisaatio.oid,
                    tyypit: [`organisaatiotyyppi_02`],
                }),
                'child'
            );
        });
        cy.get('@child').then((child) => {
            cy.visit(`${BASE_PATH}/lomake/${child.body.organisaatio.oid}`);
            cy.contains('Oppilaitos', { timeout: 10000 }).should('exist');
            cy.clickRadioOrCheckbox('Oppilaitos');
            cy.contains('LOMAKE_KOSKI_POSTI').should('not.exist');
            cy.clickRadioOrCheckbox('Oppilaitos');
            cy.contains('LOMAKE_KOSKI_POSTI').should('exist');
            cy.clickAccordion('LOMAKE_KOSKI_POSTI');
            cy.log(child.body.organisaatio.yhteystietoArvos);
        });
    });
});
