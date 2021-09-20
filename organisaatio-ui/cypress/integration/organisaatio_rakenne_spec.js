const parentData = {
    parentOid: '1.2.246.562.10.00000000001',
    ytunnus: '',
    nimi: { fi: 'PARENT Suominimi', sv: 'PARENT Ruotsi', en: 'PARENT Enkku' },
    status: '',
    nimet: [{ nimi: { fi: 'PARENT Suominimi', sv: 'PARENT Ruotsi', en: 'PARENT Enkku' }, alkuPvm: '2021-09-20' }],
    alkuPvm: '2021-09-01T21:00:00.000Z',
    yritysmuoto: '',
    tyypit: ['organisaatiotyyppi_01'],
    kotipaikkaUri: 'kunta_683',
    muutKotipaikatUris: [],
    maaUri: 'maatjavaltiot1_and',
    kieletUris: ['oppilaitoksenopetuskieli_2#2'],
    yhteystiedot: [
        { kieli: 'kieli_fi#1', tyyppi: 'puhelin', numero: '09123456' },
        { kieli: 'kieli_fi#1', email: 'PARENT-FI.noreply@test.com' },
        { kieli: 'kieli_fi#1', www: 'http://test.com' },
        {
            kieli: 'kieli_fi#1',
            osoiteTyyppi: 'posti',
            osoite: 'PARENT FI Osoite 1 a 3',
            postinumeroUri: '00100',
            postitoimipaikka: '',
        },
        {
            kieli: 'kieli_fi#1',
            osoiteTyyppi: 'kaynti',
            osoite: 'PARENT Osoite 1 a 3',
            postinumeroUri: '00100',
            postitoimipaikka: '',
        },
        { kieli: 'kieli_sv#1', tyyppi: 'puhelin', numero: '09123456' },
        { kieli: 'kieli_sv#1', email: 'PARENT-SV.noreply@test.com' },
        { kieli: 'kieli_sv#1', www: 'http://test.com' },
        {
            kieli: 'kieli_sv#1',
            osoiteTyyppi: 'posti',
            osoite: 'PARENT SV Osoite 1 a 3',
            postinumeroUri: '00100',
            postitoimipaikka: '',
        },
        {
            kieli: 'kieli_sv#1',
            osoiteTyyppi: 'kaynti',
            osoite: 'Osoite 1 a 3',
            postinumeroUri: '00100',
            postitoimipaikka: '',
        },
        { kieli: 'kieli_en#1', tyyppi: 'puhelin', numero: '09123456' },
        { kieli: 'kieli_en#1', email: 'PARENT-EN.noreply@test.com' },
        { kieli: 'kieli_en#1', www: 'http://test.com' },
        {
            kieli: 'kieli_en#1',
            osoiteTyyppi: 'posti',
            osoite: 'PARENT EN Osoite 1 a 3',
            postinumeroUri: '00100',
            postitoimipaikka: '',
        },
        {
            kieli: 'kieli_en#1',
            osoiteTyyppi: 'kaynti',
            osoite: 'Osoite 1 a 3',
            postinumeroUri: '00100',
            postitoimipaikka: '',
        },
    ],
};

describe('Organisaatio Rakenne', () => {
    Cypress.Keyboard.defaults({
        keystrokeDelay: 1,
    });

    it('shows UUDEN_TOIMIJAN_LISAAMINEN', () => {
        cy.visit('/');
        cy.get('button').contains('LISAA_UUSI_TOIMIJA').click();
        expect(cy.get('h1').value).to.contain.valueOf('UUDEN_TOIMIJAN_LISAAMINEN');
    });

    it('Can add CHILD organisaatio', () => {
        cy.persistOrganisaatio(parentData, 'parentOrganisaatio');
        cy.get('@parentOrganisaatio').then((response) => {
            console.log('RESPONSE', response.body.organisaatio.oid);
            cy.visit(`/lomake/${response.body.organisaatio.oid}`);
            cy.clickButton('LISAA_UUSI_TOIMIJA');
            cy.contains('UUDEN_TOIMIJAN_LISAAMINEN');
            cy.enterPerustiedot('CHILD', 'Oppilaitos');
            cy.clickButton('JATKA');
            cy.enterAllYhteystiedot('CHILD');
            cy.clickSaveButton();
            cy.contains('CHILD Suominimi');
        });
    });
    it('Should have parent organisation', () => {
        cy.clickAccordion('RAKENNE');
        cy.get('h2').contains('RAKENNE_YLEMMAN_TASON_OTSIKKO').parent().contains('PARENT Suominimi');
    });
});
