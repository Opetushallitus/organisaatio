const { FinnishBusinessIds } = require('finnish-business-ids');
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
    return cy.get(`input[name="${name}"]`).clear().type(value);
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

Cypress.Commands.add('enterYhteystieto', (kieli, values) => {
    cy.inputByName(`${kieli}.postiOsoite`, values.posti.osoite);
    kieli !== 'kieli_en#1' && cy.inputByName(`${kieli}.postiOsoitePostiNro`, values.posti.postinumeroUri);
    kieli !== 'kieli_en#1' && cy.inputByName(`${kieli}.puhelinnumero`, values.numero);
    cy.inputByName(`${kieli}.email`, values.email);
    return cy.inputByName(`${kieli}.www`, values.www);
});

Cypress.Commands.add('enterAllYhteystiedot', (prefix) => {
    cy.enterYhteystieto('kieli_fi#1', {
        posti: { osoite: `${prefix} FI Osoite 1 a 3`, postinumeroUri: '00100' },
        email: `${prefix}-FI.noreply@test.com`,
        www: 'http://test.com',
        numero: '09123456',
    });
    cy.enterYhteystieto('kieli_sv#1', {
        posti: { osoite: `${prefix} SV Osoite 1 a 3`, postinumeroUri: '00100' },
        email: `${prefix}-SV.noreply@test.com`,
        www: 'http://test.com',
        numero: '09123456',
    });
});

Cypress.Commands.add('clickSaveButton', () => {
    cy.intercept('POST', '/organisaatio/organisaatio/v4').as('saveOrg');
    cy.get('button').contains('TALLENNA').scrollIntoView().click();
    return cy.wait(['@saveOrg'], { timeout: 10000 });
});

Cypress.Commands.add('deleteByYTunnus', (ytunnus) => {
    cy.searchOrganisaatio(ytunnus, 'oldOrg');
    cy.get('@oldOrg').then((response) => {
        if (response.body.organisaatiot[0]) {
            const old = response.body.organisaatiot[0];
            const oid = old.oid;
            const mod = {
                oid: old.oid,
                tyypit: old.organisaatiotyypit,
                nimi: old.nimi,
                parentOid: old.parentOid,
                parentOidPath: `|${old.parentOid}|`,
                alkuPvm: '2020-10-10',
                status: 'AKTIIVINEN',
                version: 1,
                ytunnus: FinnishBusinessIds.generateBusinessId(),
                nimet: [{ nimi: old.nimi, alkuPvm: '2020-10-10', version: 0 }],
                kotipaikkaUri: old.kotipaikkaUri.substr(0, old.kotipaikkaUri.indexOf('#')),
            };
            cy.request('PUT', `/organisaatio/v4/${oid}`, mod).as('edit');
            cy.get('@edit').then((response) => {
                console.log('RESPONSE', response.body);
            });
        }
    });
});

Cypress.Commands.add('enterPerustiedot', (prefix, tyyppi, isNew = false) => {
    cy.clickRadioOrCheckbox(tyyppi);
    //cy.clickRadioOrCheckbox('EI_YTUNNUS');
    if (['Koulutustoimija'].includes(tyyppi)) {
        cy.inputByName('ytunnus', FinnishBusinessIds.generateBusinessId());
    }
    if (isNew) {
        cy.inputByName('nimi.fi', `${prefix} Suominimi`);
        cy.inputByName('nimi.sv', `${prefix} Ruotsi`);
        cy.inputByName('nimi.en', `${prefix} Enkku`);
    } else {
        cy.clickButton('MUOKKAA_ORGANISAATION_NIMEA');
        cy.inputByName('nimi.fi', `${prefix} Suominimi`);
        cy.inputByName('nimi.sv', `${prefix} Ruotsi`);
        cy.inputByName('nimi.en', `${prefix} Enkku`);
        cy.clickButton('VAHVISTA');
    }

    cy.enterDate('PERUSTAMISPAIVA', '2.9.2021');
    cy.selectFromList('PAASIJAINTIKUNTA', 'Ranua');
    cy.selectFromList('MAA', 'Andorra');
    cy.selectFromList('OPETUSKIELI', 'ruotsi');
});

Cypress.Commands.add('persistOrganisaatio', (organisaatio, key) => {
    cy.request('POST', '/organisaatio/v4/', organisaatio).as(key);
});

Cypress.Commands.add('searchOrganisaatio', (ytunnus, key) => {
    cy.request(
        'GET',
        `/organisaatio/v4/hae?searchStr=${ytunnus}&aktiiviset=true&suunnitellut=true&lakkautetut=false`
    ).as(key);
});
