import { organisaatio } from '../support/data';
import { BASE_PATH } from '../../src/contexts/constants';

describe('Oppilaitos specific fields', () => {
    it('Shows specific fields when oppilaitos is chosen', () => {
        cy.persistOrganisaatio(organisaatio('PARENT1', { tyypit: [`organisaatiotyyppi_07`] }), 'parentOrganisaatio1');
        cy.get('@parentOrganisaatio1').then((parentOrganisaatio) => {
            cy.persistOrganisaatio(
                organisaatio('CHILD', {
                    parentOid: parentOrganisaatio.body.organisaatio.oid,
                    tyypit: [`organisaatiotyyppi_08`],
                    varhaiskasvatuksenToimipaikkaTiedot: {
                        toimintamuoto: 'vardatoimintamuoto_tm02',
                        kasvatusopillinenJarjestelma: 'vardakasvatusopillinenjarjestelma_kj98',
                        varhaiskasvatuksenJarjestamismuodot: ['vardajarjestamismuoto_jm01'],
                        paikkojenLukumaara: 4,
                        varhaiskasvatuksenKielipainotukset: [
                            {
                                kielipainotus: 'kieli_99',
                                alkupvm: '2021-11-01',
                            },
                        ],
                        varhaiskasvatuksenToiminnallinenpainotukset: [
                            {
                                toiminnallinenpainotus: 'vardatoiminnallinenpainotus_tp06',
                                alkupvm: '2021-11-01',
                            },
                        ],
                    },
                }),
                'child'
            );
        });
        cy.get('@child').then((child) => {
            cy.visit(`${BASE_PATH}/lomake/${child.body.organisaatio.oid}`);
            cy.contains('Varhaiskasvatuksen toimipaikka', { timeout: 10000 }).should('exist');
        });
    });
});
