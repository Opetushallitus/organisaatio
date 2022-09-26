import { Kayttaja, Organisaatio, Osoite } from './types/types';
import { Rekisterointihakemus, Tila } from './virkailija/rekisterointihakemus';

export const tyhjaOsoite: Osoite = {
    katuosoite: '',
    postinumeroUri: '',
    postitoimipaikka: '',
};

export const dummyOrganisaatio: Organisaatio = {
    ytjNimi: {
        alkuPvm: null,
        kieli: 'fi',
        nimi: 'Testi',
    },
    kieletUris: [],
    maaUri: '',
    kotipaikkaUri: '',
    yhteystiedot: {
        kayntiosoite: tyhjaOsoite,
        postiosoite: tyhjaOsoite,
        sahkoposti: '',
        puhelinnumero: '',
    },
    ytunnus: '',
    alkuPvm: null,
    tyypit: [],
    yritysmuoto: '',
};

export const dummyKayttaja: Kayttaja = {
    asiointikieli: 'fi',
    sahkoposti: '',
    saateteksti: '',
    etunimi: 'Testi',
    sukunimi: 'Henkilö',
};

export const dummyHakemus: Rekisterointihakemus = {
    kunnat: [],
    sahkopostit: [],
    kayttaja: dummyKayttaja,
    organisaatio: dummyOrganisaatio,
    toimintamuoto: 'päiväkoti',
    tyyppi: 'varda',
    id: 0,
    vastaanotettu: '14.11.2019 10:44',
    tila: Tila.KASITTELYSSA,
};
