const { FinnishBusinessIds } = require('finnish-business-ids');
const { API_CONTEXT, PUBLIC_API_CONTEXT } = require('../../src/contexts/constants');
const moment = require('moment');
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
    return cy.get(`input[name="${name}"]`, { timeout: 10000 }).clear().type(value);
});

Cypress.Commands.add('clickButton', (contains) => {
    return cy
        .contains(contains, { timeout: 10000 })
        .scrollIntoView()
        .click()
        .then(() => {
            cy.log(`${contains} button clicked`);
        });
});
Cypress.Commands.add('clickButtonByName', (name) => {
    return cy
        .get(`[name="${name}"]`, { timeout: 10000 })
        .scrollIntoView()
        .click()
        .then(() => {
            cy.log(`${name} button clicked`);
        });
});
Cypress.Commands.add('clickRadioOrCheckbox', (contains) => {
    return cy
        .get('label')
        .parent()
        .contains(contains, { timeout: 10000 })
        .scrollIntoView()
        .click()
        .then(() => {
            cy.log(`${contains} radio or checkbox clicked`);
        });
});

Cypress.Commands.add('clickAccordion', (contains) => {
    return cy
        .contains(contains, { timeout: 10000 })
        .scrollIntoView()
        .click()
        .then(() => {
            cy.log(`${contains} accordion clicked`);
        });
});

Cypress.Commands.add('selectFromList', (list, contains, input, waitForAnimations = true) => {
    if (input) {
        cy.contains(list).parent().find('input').type(input);
    } else cy.contains(list).parent().find('svg').last().scrollIntoView().click();
    return cy
        .contains(contains, { timeout: 10000 })
        .scrollIntoView()
        .click({ waitForAnimations })
        .then(() => {
            cy.log(`${list}, ${contains} select clicked`);
        });
});

Cypress.Commands.add('enterDate', (label, date) => {
    cy.contains(label).parent().find('input').clear().type(date);
    cy.contains(label).click();
});

Cypress.Commands.add('enterYhteystieto', (kieli, values) => {
    cy.inputByName(`${kieli}.postiOsoite`, values.posti.osoite);
    kieli !== 'en' && cy.inputByName(`${kieli}.postiOsoitePostiNro`, values.posti.postinumeroUri);
    kieli !== 'en' && cy.inputByName(`${kieli}.puhelinnumero`, values.numero);
    cy.inputByName(`${kieli}.email`, values.email);
    return cy.inputByName(`${kieli}.www`, values.www);
});

Cypress.Commands.add('enterAllYhteystiedot', (prefix) => {
    cy.enterYhteystieto('fi', {
        posti: { osoite: `${prefix} FI Osoite 1 a 3`, postinumeroUri: '00100' },
        email: `${prefix}-FI.noreply@test.com`,
        www: 'http://test.com',
        numero: '09123456',
    });
    cy.enterYhteystieto('sv', {
        posti: { osoite: `${prefix} SV Osoite 1 a 3`, postinumeroUri: '00100' },
        email: `${prefix}-SV.noreply@test.com`,
        www: 'http://test.com',
        numero: '09123456',
    });
});

Cypress.Commands.add('clickSaveButton', (method = 'POST') => {
    return cy.get('button').contains('TALLENNA').scrollIntoView().click();
    cy.contains('TALLENNA', { timeout: 10000 });
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
            cy.request('PUT', `${PUBLIC_API_CONTEXT}/${oid}`, mod).as('edit');
            cy.get('@edit').then((response) => {
                cy.log('RESPONSE', response.body);
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
        cy.enterDate('alkuPvm', '2.9.2021');
        cy.clickButton('VAHVISTA');
    }

    cy.enterDate('PERUSTAMISPAIVA', '2.9.2021');
    cy.selectFromList('PAASIJAINTIKUNTA', 'Ranua');
    cy.selectFromList('MAA', 'Andorra');
    cy.selectFromList('OPETUSKIELI', 'ruotsi');
});

Cypress.Commands.add('persistOrganisaatio', (organisaatio, key) => {
    cy.request('POST', `${PUBLIC_API_CONTEXT}/`, organisaatio).as(key);
});
Cypress.Commands.add('getOrganisaatio', (oid, key) => {
    cy.request('GET', `${PUBLIC_API_CONTEXT}/${oid}`).as(key);
});
Cypress.Commands.add('updateOrganisaatio', (organisaatio, key) => {
    cy.request('PUT', `${PUBLIC_API_CONTEXT}/${organisaatio.oid}`, organisaatio).as(key);
});

Cypress.Commands.add('searchOrganisaatio', (ytunnus, key) => {
    cy.request(
        'GET',
        `${PUBLIC_API_CONTEXT}/hae?searchStr=${ytunnus}&aktiiviset=true&suunnitellut=true&lakkautetut=false`
    ).as(key);
});

const getToday = () => {
    return moment().format('D.M.yyyy');
};

Cypress.Commands.add('addNewNimi', (prefix = 'testi', nimi = 'testi', alkuPvm = getToday()) => {
    cy.clickButton('MUOKKAA_ORGANISAATION_NIMEA');
    cy.enterDate('ALKUPVM', alkuPvm);
    cy.inputByName('nimi.fi', `${prefix} Suominimi`);
    cy.inputByName('nimi.sv', `${prefix} Ruotsi`);
    cy.inputByName('nimi.en', `${prefix} Enkku`);
    cy.clickButton('VAHVISTA');
});

Cypress.Commands.add('editNimi', (prefix = 'testi') => {
    cy.clickButton('MUOKKAA_ORGANISAATION_NIMEA');
    cy.get(`input[value="EDIT"]`).parent().click();
    cy.inputByName('nimi.fi', `${prefix} Suominimi muokattu`);
    cy.inputByName('nimi.sv', `${prefix} Ruotsi muokattu`);
    cy.inputByName('nimi.en', `${prefix} Enkku muokattu`);
    cy.clickButton('VAHVISTA');
});
Cypress.Commands.add('editNimiWithCopy', (prefix = 'testi') => {
    cy.clickButton('MUOKKAA_ORGANISAATION_NIMEA');
    cy.get(`input[value="EDIT"]`).parent().click();
    cy.inputByName('nimi.fi', `${prefix} Suominimi kopioitava`);
    cy.get('svg[name="KOPIOI_MUIHIN_NIMIIN"]').click();
    cy.clickButton('VAHVISTA');
});
