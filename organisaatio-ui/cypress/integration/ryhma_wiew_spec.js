describe('Organisaatiot Page', () => {
    before(() => {
        cy.intercept('GET', '/organisaatio/config/frontproperties', { fixture: 'front.json' });
        cy.intercept('GET', 'http://localhost:9000/kayttooikeus-service/cas/me', { fixture: 'me.json' });
        cy.intercept('GET', '/organisaatio/lokalisointi/kieli', { fixture: 'kieli.json' });
        cy.intercept('GET', '/organisaatio/lokalisointi', { fixture: 'lokalisointi.json' });
        cy.intercept('GET', '/organisaatio/koodisto/KUNTA/koodi', { fixture: 'kunnat.json' });
        cy.intercept('GET', '/organisaatio/koodisto/ORGANISAATIOTYYPPI/koodi', { fixture: 'organisaatiotyypit.json' });
        cy.intercept('GET', '/organisaatio/koodisto/RYHMANTILA/koodi', { fixture: 'ryhmantilat.json' });
        cy.intercept('GET', '/organisaatio/koodisto/RYHMATYYPIT/koodi', { fixture: 'ryhmatyypit.json' });
        cy.intercept('GET', '/organisaatio/koodisto/KAYTTORYHMAT/koodi', { fixture: 'kayttoryhmat.json' });
        cy.intercept('GET', '/organisaatio/koodisto/MAATJAVALTIOT1/koodi', { fixture: 'MAATJAVALTIOT.json' });
        cy.intercept('GET', '/organisaatio/koodisto/POSTI/koodi*', { fixture: 'POSTI.json' });
        cy.intercept('GET', '/organisaatio/koodisto/OPPILAITOKSENOPETUSKIELI/koodi*', {
            fixture: 'OPPILAITOKSENOPETUSKIELI.json',
        });
        cy.intercept('GET', '/organisaatio/organisaatio/v4/*', { fixture: 'humakRyhma.json' });
        cy.intercept('POST', '/organisaatio/organisaatio/v4', { fixture: 'humakRyhma.json' });
        cy.intercept('PUT', '/organisaatio/organisaatio/v4/1.2.246.562.10.48587687889', { fixture: 'humakRyhma.json' });
        cy.visit('/organisaatio/ryhmat/1.2.246.562.28.64635646082');
    });
    it('Shows Humak Ryhma', () => {
        expect(cy.get('#nimiFi').value).to.have.valueOf('Humak, alueyksikkö');
    });
    it('Can edit Swedish name', () => {
        cy.get('#nimiSv').type('Humak Ruotsiksi');
        expect(cy.get('#nimiSv').value).to.have.valueOf("Humak Ruotsiksi'");
    });
    it('Can edit Swedish kuvaus', () => {
        cy.get('#kuvaus2Sv').type('Humak Ruotsiksi');
    });
    it('will show validate error if there is no name set', () => {
        cy.get('#nimiSv').clear();
        cy.get('#nimiFi').clear();
        cy.get('button').last().click();
        cy.get('#nimiFi')
            .should('be.visible')
            .should('have.css', 'border-color')
            .and('match', /228, 78, 78/);
    });

    it('Can save edited organisation', () => {
        cy.get('#nimiFi').type('humak suomi');
        cy.get('#RYHMALOMAKE_RYHMAN_TYYPPI_SELECT input').first().type('Hakukohde{enter}{enter}', { force: true });
        cy.get('#RYHMALOMAKE_RYHMAN_KAYTTOTARKOITUS_SELECT input')
            .first()
            .type('Yleinen{enter}{enter}', { force: true });
        cy.intercept('PUT', '/organisaatio/organisaatio/v4/1.2.246.562.10.48587687889', { fixture: 'humakRyhma.json' });
        cy.get('button').contains('BUTTON_TALLENNA').click();
        cy.location('pathname').should('include', '/ryhmat');
    });

    it('Can edit and save a new ryhma', () => {
        cy.intercept('GET', '/organisaatio/lokalisointi/kieli', { fixture: 'kieli.json' });
        cy.intercept('GET', '/organisaatio/lokalisointi', { fixture: 'lokalisointi.json' });
        cy.intercept('GET', '/organisaatio/koodisto/KUNTA/koodi', { fixture: 'kunnat.json' });
        cy.intercept('GET', '/organisaatio/koodisto/ORGANISAATIOTYYPPI/koodi', { fixture: 'organisaatiotyypit.json' });
        cy.intercept('GET', '/organisaatio/koodisto/RYHMANTILA/koodi', { fixture: 'ryhmantilat.json' });
        cy.intercept('GET', '/organisaatio/koodisto/RYHMATYYPIT/koodi', { fixture: 'ryhmatyypit.json' });
        cy.intercept('GET', '/organisaatio/koodisto/KAYTTORYHMAT/koodi', { fixture: 'kayttoryhmat.json' });
        cy.intercept('GET', '/organisaatio/koodisto/MAATJAVALTIOT1/koodi', { fixture: 'MAATJAVALTIOT.json' });
        cy.intercept('GET', '/organisaatio/koodisto/POSTI/koodi*', { fixture: 'POSTI.json' });
        cy.intercept('GET', '/organisaatio/koodisto/OPPILAITOKSENOPETUSKIELI/koodi*', {
            fixture: 'OPPILAITOKSENOPETUSKIELI.json',
        });
        cy.intercept('GET', '/organisaatio/organisaatio/v4/*', { fixture: 'humakRyhma.json' });
        cy.intercept('POST', '/organisaatio/organisaatio/v4', { fixture: 'humakRyhma.json' });
        cy.visit('/organisaatio/ryhmat/uusi');
        cy.get('#nimiFi').type('Suominimi');
        cy.get('#nimiSv').type('Ruotsinimi');
        cy.get('#nimiEn').type('Enkkunimi');
        cy.get('#kuvaus2Fi').type('Suomi kuvaus');
        cy.get('#kuvaus2Sv').type('Ruotsi kuvaus');
        cy.get('#kuvaus2En').type('Enkku kuvaus');
        cy.get('#RYHMALOMAKE_RYHMAN_TYYPPI_SELECT input').first().type('Hakukohde{enter}{enter}', { force: true });
        cy.get('#RYHMALOMAKE_RYHMAN_KAYTTOTARKOITUS_SELECT input')
            .first()
            .type('Yleinen{enter}{enter}', { force: true });
        cy.get('button').contains('BUTTON_TALLENNA').click();
        cy.location('pathname').should('include', '/ryhmat');
    });
});
