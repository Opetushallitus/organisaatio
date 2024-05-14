import { organisaatio } from '../support/data';
import { BASE_PATH } from '../../src/contexts/constants';
import moment from 'moment';
const ui_date_format = 'D.M.yyyy';
describe('Organisaatiotarkastus', () => {
    const now = moment().format('yyyyMMDDssS');
    const today = moment();
    const timesPast = moment().subtract(2, 'years');
    const recent = moment().subtract(23, 'days');
    const prefix = `${now} TARKASTUS IS `;
    before(() => {
        cy.persistOrganisaatio(organisaatio(`${prefix}NOT CHECKED`, { tyypit: [`organisaatiotyyppi_01`] }), 'parent');
        cy.persistOrganisaatio(
            organisaatio(`${prefix}CHECKED LONG AGO`, {
                tyypit: [`organisaatiotyyppi_01`],
                tarkastusPvm: timesPast.unix() * 1000,
            }),
            'parent'
        );
        cy.persistOrganisaatio(
            organisaatio(`${prefix}CHECKED LATELY`, {
                tyypit: [`organisaatiotyyppi_01`],
                tarkastusPvm: recent.unix() * 1000,
            }),
            'parent'
        );
    });
    it('Opens list with organisations', () => {
        cy.visit(`${BASE_PATH}/organisaatiot`);
        cy.get('h2', { timeout: 20000 }).contains('TAULUKKO_ORGANISAATIOT').should('exist');
        cy.get('input').first().type(`${prefix}{enter}`);
    });
    it('Shows flags correctly when tarkastus missing ', () => {
        cy.visit(`${BASE_PATH}/organisaatiot`);
        cy.get('td')
            .contains('TARKASTUS IS NOT CHECKED')
            .parents('tr')
            .find('svg')
            .parent()
            .should('have.attr', 'title', 'TARKASTUS_PUUTTUU');
    });
    it('Shows flags correctly when tarkastus missing', () => {
        cy.visit(`${BASE_PATH}/organisaatiot`);
        cy.get('td')
            .contains('TARKASTUS IS CHECKED LATELY')
            .parents('tr')
            .find('svg')
            .parent()
            .should('have.attr', 'title', `VIIMEINEN_TARKASTUS_${recent.format(ui_date_format)}`);
    });
    it('Shows flags correctly when tarkastus old', () => {
        cy.visit(`${BASE_PATH}/organisaatiot`);
        cy.get('td')
            .contains('TARKASTUS IS CHECKED LONG AGO')
            .parents('tr')
            .find('svg')
            .parent()
            .should('have.attr', 'title', `VIIMEINEN_TARKASTUS_${timesPast.format(ui_date_format)}`);
    });
    it('Opens organisaatio with outdated tarkstus', () => {
        cy.visit(`${BASE_PATH}/organisaatiot`);
        cy.get('td').contains('TARKASTUS IS CHECKED LONG AGO').click();
        cy.get('h1').contains(`LONG AGO`, { timeout: 20000 }).should('exist');
        cy.get('button')
            .contains('LOMAKE_MERKITSE_TARKISTUS')
            .find('svg')
            .parent()
            .should('have.attr', 'title', `VIIMEINEN_TARKASTUS_${timesPast.format(ui_date_format)}`)
            .parents('button')
            .click();
        cy.get('button')
            .contains('LOMAKE_MERKITSE_TARKISTUS')
            .find('svg')
            .parent()
            .should('have.attr', 'title', `VIIMEINEN_TARKASTUS_${today.format(ui_date_format)}`);
    });
    it('Shows list of orgs with updated tarkastusflag', () => {
        cy.visit(`${BASE_PATH}/organisaatiot`);
        cy.get('h2', { timeout: 20000 }).contains('TAULUKKO_ORGANISAATIOT').should('exist');
        cy.get('input').first().clear().type(`${prefix}{enter}`);
        cy.get('td')
            .contains('TARKASTUS IS CHECKED LONG AGO')
            .parents('tr')
            .find('svg')
            .parent()
            .should('have.attr', 'title', `VIIMEINEN_TARKASTUS_${today.format(ui_date_format)}`);
    });
});
