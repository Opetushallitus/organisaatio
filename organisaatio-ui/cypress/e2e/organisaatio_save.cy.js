import { BASE_PATH } from '../../src/contexts/constants';
import { helsinki } from '../support/data';

//yhteystiedot element get id fields updated on each save, also the order of members are not guaranteed

function removeUnwanted(input) {
    return { ...input, yhteystiedot: input.yhteystiedot?.length || 0 };
}

function stripId(yhteystiedot) {
    return yhteystiedot.map((a) => {
        return { ...a, id: undefined };
    });
}

describe('Save organisaatio through UI', () => {
    it('should not loose information on save', () => {
        const myHel = helsinki({});
        cy.persistOrganisaatio(myHel, 'firstHel').then((firstHel) => {
            const interHelPut = { ...firstHel.body.organisaatio, ...myHel };
            cy.updateOrganisaatio(interHelPut, 'interHel').then((interHel) => {
                cy.getOrganisaatio(interHel.body.organisaatio.oid, 'actualHelsinki').then((actualHelsinkiResponse) => {
                    const actualHelsinki = actualHelsinkiResponse.body;
                    cy.visit(`${BASE_PATH}/lomake/${actualHelsinki.oid}`);
                    cy.get('h3').contains('Koulutustoimija', { timeout: 10000 }).should('exist');
                    cy.clickRadioOrCheckbox('Kunta');
                    cy.clickSaveButton();
                    cy.get('h3').contains('Koulutustoimija', { timeout: 10000 }).should('exist');
                    cy.getOrganisaatio(actualHelsinki.oid, 'whatNowHelsinki');
                    cy.get('@whatNowHelsinki').then((whatNowHelsinki) => {
                        const actual = {
                            ...whatNowHelsinki.body,
                        };
                        const expected = {
                            ...actualHelsinki,
                            tyypit: ['organisaatiotyyppi_01', 'organisaatiotyyppi_07', 'organisaatiotyyppi_09'],
                            version: 2,
                        };
                        expect(Object.keys(actual)).to.have.members(Object.keys(expected));
                        expect(removeUnwanted(actual)).to.deep.equal(removeUnwanted(expected));
                        expect(stripId(actual.yhteystiedot)).to.have.deep.members(stripId(expected.yhteystiedot));
                    });
                });
            });
        });
    });
});
