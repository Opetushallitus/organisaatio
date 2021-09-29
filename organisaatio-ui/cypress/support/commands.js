// ***********************************************
// This example commands.js shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
//
//
// -- This is a parent command --
// Cypress.Commands.add('login', (email, password) => { ... })
//
//
// -- This is a child command --
// Cypress.Commands.add('drag', { prevSubject: 'element'}, (subject, options) => { ... })
//
//
// -- This is a dual command --
// Cypress.Commands.add('dismiss', { prevSubject: 'optional'}, (subject, options) => { ... })
//
//
// -- This will overwrite an existing command --
// Cypress.Commands.overwrite('visit', (originalFn, url, options) => { ... })
Cypress.Commands.add('inputByName', (name, value) => {
    return cy.get(`input[name="${name}"]`).type(value);
});

Cypress.Commands.add('clickButton', (contains) => {
    return cy
        .get('button')
        .contains(contains)
        .scrollIntoView()
        .click()
        .then(() => {
            cy.log(`${contains} button clicked`);
        });
});

Cypress.Commands.add('clickRadioOrCheckbox', (contains) => {
    return cy
        .get('div')
        .contains(contains)
        .scrollIntoView()
        .click()
        .then(() => {
            cy.log(`${contains} radio or checkbox clicked`);
        });
});

Cypress.Commands.add('clickAccordion', (contains) => {
    return cy
        .get('span')
        .contains(contains)
        .scrollIntoView()
        .click()
        .then(() => {
            cy.log(`${contains} accordion clicked`);
        });
});

Cypress.Commands.add('selectFromList', (list, contains, input) => {
    if (input) {
        cy.get('label').contains(list).parent().find('input').type(input);
    } else cy.get('label').contains(list).parent().find('svg').last().scrollIntoView().click();
    return cy
        .get('div')
        .contains(contains)
        .scrollIntoView()
        .click()
        .then(() => {
            cy.log(`${list}, ${contains} select clicked`);
        });
});

Cypress.Commands.add('enterDate', (label, date) => {
    cy.get('label').contains(label).parent().find('input').type(date);
    cy.get('label').contains(label).click();
});

Cypress.Commands.add('enterYhteystieto', (values) => {
    cy.inputByName('posti.osoite', values.posti.osoite);
    cy.inputByName('posti.postinumeroUri', values.posti.postinumeroUri);
    cy.inputByName('kaynti.osoite', values.kaynti.osoite);
    cy.inputByName('kaynti.postinumeroUri', values.kaynti.postinumeroUri);
    cy.inputByName('email', values.email);
    cy.inputByName('www', values.www);
    return cy.inputByName('numero', values.numero);
});

Cypress.Commands.add('enterAllYhteystiedot', (prefix) => {
    cy.enterYhteystieto({
        posti: { osoite: `${prefix} FI Osoite 1 a 3`, postinumeroUri: '00100' },
        kaynti: { osoite: `${prefix} Osoite 1 a 3`, postinumeroUri: '00100' },
        email: `${prefix}-FI.noreply@test.com`,
        www: 'http://test.com',
        numero: '09123456',
    });
    cy.clickRadioOrCheckbox('Ruotsiksi');
    cy.enterYhteystieto({
        posti: { osoite: `${prefix} SV Osoite 1 a 3`, postinumeroUri: '00100' },
        kaynti: { osoite: 'Osoite 1 a 3', postinumeroUri: '00100' },
        email: `${prefix}-SV.noreply@test.com`,
        www: 'http://test.com',
        numero: '09123456',
    });
    cy.clickRadioOrCheckbox('Englanniksi');
    cy.enterYhteystieto({
        posti: { osoite: `${prefix} EN Osoite 1 a 3`, postinumeroUri: '00100' },
        kaynti: { osoite: 'Osoite 1 a 3', postinumeroUri: '00100' },
        email: `${prefix}-EN.noreply@test.com`,
        www: 'http://test.com',
        numero: '09123456',
    });
});

Cypress.Commands.add('clickSaveButton', () => {
    cy.intercept('POST', '/organisaatio/organisaatio/v4').as('saveOrg');
    cy.get('button').contains('TALLENNA').scrollIntoView().click();
    return cy.wait(['@saveOrg'], { timeout: 10000 });
});

Cypress.Commands.add('enterPerustiedot', (prefix, tyyppi) => {
    cy.clickAccordion('PERUSTIEDOT');
    cy.clickRadioOrCheckbox('EI_YTUNNUS');
    cy.clickButton('MUOKKAA_ORGANISAATION_NIMEA');
    cy.inputByName('fi', `${prefix} Suominimi`);
    cy.inputByName('sv', `${prefix} Ruotsi`);
    cy.inputByName('en', `${prefix} Enkku`);
    cy.clickButton('VAHVISTA');
    cy.clickRadioOrCheckbox(tyyppi);
    cy.enterDate('PERUSTAMISPAIVA', '2.9.2021');
    cy.selectFromList('PAASIJAINTIKUNTA', 'Ranua');
    cy.selectFromList('MAA', 'Andorra');
    cy.selectFromList('OPETUSKIELI', 'ruotsi');
});

Cypress.Commands.add('persistOrganisaatio', (organisaatio, key) => {
    cy.request('POST', '/organisaatio/v4/', organisaatio).as(key);
});
