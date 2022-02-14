import { organisaatio } from '../support/data';
import { BASE_PATH, LEGACY_API_CONTEXT } from '../../src/contexts/constants';
import { KOSKIPOSTI_BASE, KOSKIPOSTI_TYYPI_OID } from '../../src/api/organisaatio';

describe('Oppilaitos koski posti', () => {
    before(() => {
        cy.request({
            method: 'PUT',
            url: `${LEGACY_API_CONTEXT}/yhteystietojentyyppi`,
            body: {
                version: 1,
                oid: KOSKIPOSTI_TYYPI_OID,
                nimi: {
                    teksti: [
                        { value: 'KOSKI-palvelun omien tietojen virheilmoituksen sähköpostiosoite.', kieliKoodi: 'fi' },
                        { value: 'E-postadress för felanmälan i egna uppgifter i KOSKI-tjänsten', kieliKoodi: 'sv' },
                        { value: 'KOSKI-palvelun omien tietojen virheilmoituksen sähköpostiosoite', kieliKoodi: 'en' },
                    ],
                },
                sovellettavatOrganisaatios: ['organisaatiotyyppi_02'],
                allLisatietokenttas: [
                    {
                        version: 0,
                        oid: '1.2.246.562.5.57850489428',
                        nimi: 'Sähköpostiosoite',
                        nimiSv: 'Epostadress',
                        nimiEn: 'Email',
                        tyyppi: 'EMAIL',
                        kaytossa: true,
                        pakollinen: false,
                    },
                    {
                        version: 0,
                        oid: '1.2.246.562.5.25590488208',
                        nimi: 'Ulkomaan osoite',
                        nimiSv: 'Utrikes adress',
                        nimiEn: 'Foreign Address',
                        tyyppi: 'OSOITE_ULKOMAA',
                        kaytossa: false,
                        pakollinen: false,
                    },
                    {
                        version: 0,
                        oid: '1.2.246.562.5.16622623284',
                        nimi: 'Puhelinnumero',
                        nimiSv: 'Telefonnummer',
                        nimiEn: 'Phone',
                        tyyppi: 'PUHELIN',
                        kaytossa: false,
                        pakollinen: false,
                    },
                    {
                        version: 0,
                        oid: '1.2.246.562.5.31448312855',
                        nimi: 'Nimi',
                        nimiSv: 'Namn',
                        nimiEn: 'Name',
                        tyyppi: 'NIMI',
                        kaytossa: false,
                        pakollinen: false,
                    },
                    {
                        version: 0,
                        oid: '1.2.246.562.5.62101101889',
                        nimi: 'Www-osoite',
                        nimiSv: 'Www-adress',
                        nimiEn: 'WWW Address',
                        tyyppi: 'WWW',
                        kaytossa: false,
                        pakollinen: false,
                    },
                    {
                        version: 0,
                        oid: '1.2.246.562.5.26326161317',
                        nimi: 'Käyntiosoite',
                        nimiSv: 'Besöksadress',
                        nimiEn: 'Visiting Address',
                        tyyppi: 'OSOITE',
                        kaytossa: false,
                        pakollinen: false,
                    },
                    {
                        version: 0,
                        oid: '1.2.246.562.5.50555253082',
                        nimi: 'Matkapuhelinnumero',
                        nimiSv: 'Mobiltelefonnummer',
                        nimiEn: 'Mobile',
                        tyyppi: 'PUHELIN',
                        kaytossa: false,
                        pakollinen: false,
                    },
                    {
                        version: 0,
                        oid: '1.2.246.562.5.34866821670',
                        nimi: 'Nimike',
                        nimiSv: 'Benämning',
                        nimiEn: 'Title',
                        tyyppi: 'NIMIKE',
                        kaytossa: false,
                        pakollinen: false,
                    },
                    {
                        version: 0,
                        oid: '1.2.246.562.5.13374672924',
                        nimi: 'Postiosoite',
                        nimiSv: 'Postadress',
                        nimiEn: 'Postal Address',
                        tyyppi: 'OSOITE',
                        kaytossa: false,
                        pakollinen: false,
                    },
                ],
                sovellettavatOppilaitostyyppis: [],
            },
            failOnStatusCode: false,
        });
    });
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
                            //KOSKI sahkoposti
                            'YhteystietoArvo.arvoText': 'testi@testi.com',
                            'YhteystietoArvo.kieli': 'kieli_fi#1',
                            ...KOSKIPOSTI_BASE,
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
