import { BASE_PATH, PUBLIC_API_CONTEXT } from '../../src/contexts/constants';
import { helsinki } from '../support/data';

function doSort(input) {
    input.yhteystiedot.sort((a, b) => {
        if (a.email) return 1;
        if (a.tyyppi) return 1;
        if (a.osoiteTyyppi != b.osoiteTyyppi) return -1;
        else return a.osoiteTyyppi?.localeCompare(b.osoiteTyyppi) || 0;
    });
    return Object.keys(input);
    // return input.yhteystietoArvos;
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
                        const actual = whatNowHelsinki.body;
                        const expected = {
                            ...actualHelsinki,
                            tyypit: ['organisaatiotyyppi_01', 'organisaatiotyyppi_07', 'organisaatiotyyppi_09'],
                            version: 3,
                        };

                        assert.deepEqual(doSort(actual), doSort(expected));
                    });
                });
            });
        });
    });
});
