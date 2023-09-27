import { organisaatio } from '../support/data';
import { BASE_PATH } from '../../src/contexts/constants';
import * as React from 'react';

const VAKA_TOIMIJA_ORGANISAATIOTYYPPI = 'organisaatiotyyppi_08';

describe('VAKA specific fields', () => {
    it('Shows specific fields when oppilaitos is chosen', () => {
        cy.persistOrganisaatio(organisaatio('PARENT1', { tyypit: [`organisaatiotyyppi_07`] }), 'parentOrganisaatio1');
        cy.get('@parentOrganisaatio1').then((parentOrganisaatio) => {
            cy.persistOrganisaatio(
                organisaatio('CHILD', {
                    parentOid: parentOrganisaatio.body.organisaatio.oid,
                    tyypit: [`organisaatiotyyppi_08`],
                    piilotettu: true,
                    varhaiskasvatuksenToimipaikkaTiedot: {
                        toimintamuoto: 'vardatoimintamuoto_tm02',
                        kasvatusopillinenJarjestelma: 'vardakasvatusopillinenjarjestelma_kj98',
                        varhaiskasvatuksenJarjestamismuodot: [
                            'vardajarjestamismuoto_jm01',
                            'vardajarjestamismuoto_jm02',
                        ],
                        paikkojenLukumaara: 4,
                        varhaiskasvatuksenKielipainotukset: [
                            {
                                kielipainotus: 'kieli_fi',
                                alkupvm: '2021-11-01',
                            },
                            {
                                kielipainotus: 'kieli_99',
                                alkupvm: '2005-11-01',
                            },
                        ],
                        varhaiskasvatuksenToiminnallinenpainotukset: [
                            {
                                toiminnallinenpainotus: 'vardatoiminnallinenpainotus_tp06',
                                alkupvm: '2021-11-01',
                            },
                            {
                                toiminnallinenpainotus: 'vardatoiminnallinenpainotus_tp07',
                                alkupvm: '2020-11-01',
                                loppupvm: '2021-05-05',
                            },
                        ],
                    },
                }),
                'child'
            );
        });
        cy.get('@child').then((child) => {
            cy.visit(`${BASE_PATH}/lomake/${child.body.organisaatio.oid}`);
            cy.contains('Varhaiskasvatuksen toimipaikka', { timeout: 1000000 }).should('exist');
            cy.get(`input[name="${VAKA_TOIMIJA_ORGANISAATIOTYYPPI}"]`).should('be.disabled').should('be.checked');
            cy.contains('LOMAKE_VAKA').should('exist');
            cy.clickAccordion('LOMAKE_VAKA');
            cy.contains('VAKA_TOIMINTAMUOTO').should('exist').next().contains('Perhepäivähoito').should('exist');
            cy.contains('VAKA_JARJESTELMA').should('exist').next().contains('Ei painotusta').should('exist');
            cy.contains('VAKA_PAINOTUS')
                .should('exist')
                .next()
                .contains('Seikkailu')
                .should('exist')
                .next()
                .next()
                .contains('Ympäristö ja luonto')
                .should('exist');
            cy.contains('VAKA_PAIKAT').should('exist').next().contains('4').should('exist');
            cy.contains('VAKA_JARJESTAMISMUOTO')
                .should('exist')
                .next()
                .contains('Kunnan tai kuntayhtymän järjestämä')
                .should('exist')
                .next()
                .next()
                .contains('Ostopalvelu, kunnan tai kuntayhtymän järjestämä')
                .should('exist');
            cy.contains('VAKA_KIELIPAINOTUKSET')
                .should('exist')
                .next()
                .contains('tuntematon 1.11.2005')
                .should('exist')
                .next()
                .next()
                .contains('suomi 1.11.2021')
                .should('exist');
            cy.contains('VAKA_PIILOTETTU').should('exist').next().get('input').should('be.checked');
        });
    });
});
