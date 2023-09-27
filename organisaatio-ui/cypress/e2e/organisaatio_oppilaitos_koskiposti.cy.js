import { organisaatio } from '../support/data';
import { BASE_PATH, KOSKIPOSTI_BASE, KOSKIPOSTI_TYYPI_OID, LEGACY_API_CONTEXT } from '../../src/contexts/constants';

describe('Oppilaitos koski posti', () => {
    it('Shows koski posti oppilaitos is chosen', () => {
        cy.persistOrganisaatio(organisaatio('PARENT1', { tyypit: [`organisaatiotyyppi_01`] }), 'parentOrganisaatio1');
        //persis child
        cy.get('@parentOrganisaatio1').then((parentOrganisaatio) => {
            cy.persistOrganisaatio(
                organisaatio('CHILD', {
                    parentOid: parentOrganisaatio.body.organisaatio.oid,
                    tyypit: [`organisaatiotyyppi_02`],
                    yhteystietoArvos: [
                        {
                            ...KOSKIPOSTI_BASE,
                            //KOSKI sahkoposti
                            'YhteystietoArvo.arvoText': 'testi@testi.com',
                            'YhteystietoArvo.kieli': 'kieli_fi#1',
                        },
                    ],
                }),
                'child'
            );
        });
        cy.get('@child').then((child) => {
            cy.visit(`${BASE_PATH}/lomake/${child.body.organisaatio.oid}`);
            cy.contains('Oppilaitos', { timeout: 120000 }).should('exist');
            cy.clickRadioOrCheckbox('Oppilaitos');
            cy.contains('LOMAKE_KOSKI_POSTI').should('not.exist');
            cy.clickRadioOrCheckbox('Oppilaitos');
            cy.contains('LOMAKE_KOSKI_POSTI').should('exist');
            cy.clickAccordion('LOMAKE_KOSKI_POSTI');
            cy.get('input[name="koskiposti.fi"]').should('have.value', 'testi@testi.com');
            cy.inputByName('koskiposti.fi', 'muokattufi@testi.com');
            cy.inputByName('koskiposti.sv', 'muokattusv@testi.com');
            cy.inputByName('koskiposti.en', 'muokattuen@testi.com');
            cy.clickSaveButton('PUT');
            cy.contains('Suominimi', { timeout: 10000 }).should('exist');
            cy.clickAccordion('LOMAKE_KOSKI_POSTI');
            cy.contains('YHTEYSTIEDOT_SAHKOPOSTIOSOITE', { timeout: 10000 }).should('exist');
            cy.get('input[name="koskiposti.fi"]').should('not.have.value', 'testi@testi.com');
            cy.get('input[name="koskiposti.fi"]').should('have.value', 'muokattufi@testi.com');
            cy.get('input[name="koskiposti.sv"]').should('have.value', 'muokattusv@testi.com');
            cy.get('input[name="koskiposti.en"]').should('have.value', 'muokattuen@testi.com');
        });
    });
});
