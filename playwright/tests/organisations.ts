import { FinnishBusinessIds } from "finnish-business-ids";

import {
  ORGANIAATIOTYYPPI_KOULUTUSTOIMIJA,
  ORGANIAATIOTYYPPI_VARHAISKASVATUKSEN_JARJESTAJA,
} from "../../organisaatio-ui/src/api/koodisto";
import type {
  ApiOrganisaatio,
  NewApiOrganisaatio,
} from "../../organisaatio-ui/src/types/apiTypes";

export const persistOrganisation = async (
  prefix = "",
  override: Partial<NewApiOrganisaatio>
): Promise<{ organisaatio: ApiOrganisaatio }> => {
  const organisation = organisaatio(prefix, override);
  return await fetch("http://localhost:8080/organisaatio-service/api/", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization:
        "Basic " + Buffer.from("devaaja:devaaja").toString("base64"),
    },
    body: JSON.stringify(organisation),
  }).then((r) => r.json() as Promise<{ organisaatio: ApiOrganisaatio }>);
};

export const organisaatio = (
  prefix = "",
  override: Partial<NewApiOrganisaatio>
) => {
  return {
    parentOid: `1.2.246.562.10.00000000001`,
    ytunnus: FinnishBusinessIds.generateBusinessId(),
    nimi: {
      fi: `${prefix} Suominimi`,
      sv: `${prefix} Ruotsi`,
      en: `${prefix} Enkku`,
    },
    status: ``,
    nimet: [
      {
        nimi: {
          fi: `${prefix} Suominimi`,
          sv: `${prefix} Ruotsi`,
          en: `${prefix} Enkku`,
        },
        alkuPvm: `2021-09-20`,
      },
    ],
    alkuPvm: `2021-09-01T21:00:00.000Z`,
    yritysmuoto: ``,
    tyypit: [`organisaatiotyyppi_01`],
    kotipaikkaUri: `kunta_683`,
    muutKotipaikatUris: [],
    maaUri: `maatjavaltiot1_and`,
    kieletUris: [`oppilaitoksenopetuskieli_2#2`],
    yhteystiedot: [
      { kieli: `kieli_fi#1`, tyyppi: `puhelin`, numero: `09123456` },
      {
        kieli: `kieli_fi#1`,
        email: `${prefix.replaceAll(" ", "")}-FI.noreply@test.com`,
      },
      {
        kieli: `kieli_fi#1`,
        www: `http://${prefix.replaceAll(" ", "-")}-test.com`,
      },
      {
        kieli: `kieli_fi#1`,
        osoiteTyyppi: `posti`,
        osoite: `${prefix} FI Osoite 1 a 3`,
        postinumeroUri: `posti_00100`,
        postitoimipaikka: ``,
      },
      {
        kieli: `kieli_fi#1`,
        osoiteTyyppi: `kaynti`,
        osoite: `${prefix} Osoite 1 a 3`,
        postinumeroUri: `posti_00100`,
        postitoimipaikka: ``,
      },
      { kieli: `kieli_sv#1`, tyyppi: `puhelin`, numero: `09123456` },
      {
        kieli: `kieli_sv#1`,
        email: `${prefix.replaceAll(" ", "")}-SV.noreply@test.com`,
      },
      {
        kieli: `kieli_sv#1`,
        www: `http://${prefix.replaceAll(" ", "-")}-test.com`,
      },
      {
        kieli: `kieli_sv#1`,
        osoiteTyyppi: `posti`,
        osoite: `${prefix} SV Osoite 1 a 3`,
        postinumeroUri: `posti_00100`,
        postitoimipaikka: ``,
      },
      {
        kieli: `kieli_sv#1`,
        osoiteTyyppi: `kaynti`,
        osoite: `Osoite 1 a 3`,
        postinumeroUri: `posti_00100`,
        postitoimipaikka: ``,
      },
      { kieli: `kieli_en#1`, tyyppi: `puhelin`, numero: `09123456` },
      {
        kieli: `kieli_en#1`,
        email: `${prefix.replaceAll(" ", "")}-EN.noreply@test.com`,
      },
      {
        kieli: `kieli_en#1`,
        www: `http://${prefix.replaceAll(" ", "-")}-test.com`,
      },
      {
        kieli: `kieli_en#1`,
        osoiteTyyppi: `posti`,
        osoite: `${prefix} EN Osoite 1 a 3`,
        postinumeroUri: `posti_00100`,
        postitoimipaikka: ``,
      },
      {
        kieli: `kieli_en#1`,
        osoiteTyyppi: `kaynti`,
        osoite: `${prefix} Osoite 1 a 3`,
        postinumeroUri: `posti_00100`,
        postitoimipaikka: ``,
      },
    ],
    ...override,
  };
};

export const helsinki = (override) => {
  const prefix = "HELTEST";
  return {
    metadata: {
      data: {
        KANSAINVALISET_KOULUTUSOHJELMAT: {},
        YLEISKUVAUS: {},
        KIELIOPINNOT: {},
        TERVEYDENHUOLTOPALVELUT: {},
        TEHTAVANIMIKE: {},
        SAHKOPOSTIOSOITE: {},
        RAHOITUS: {},
        AIEMMIN_HANKITTU_OSAAMINEN: {},
        "sosiaalinenmedia_7#1": {},
        "sosiaalinenmedia_5#1": {},
        "sosiaalinenmedia_6#1": {},
        OPISKELIJALIIKKUVUUS: {},
        VAKUUTUKSET: {},
        OPISKELIJA_JARJESTOT: {},
        VALINTAMENETTELY: {},
        "sosiaalinenmedia_3#1": {},
        "sosiaalinenmedia_4#1": {
          "kieli_fi#1": "https://twitter.com/HelsinkiOppii",
        },
        "sosiaalinenmedia_1#1": {
          "kieli_fi#1": "https://www.facebook.com/HelsinkiOppii",
        },
        VASTUUHENKILOT: {},
        KUSTANNUKSET: {},
        OPPIMISYMPARISTO: {},
        "sosiaalinenmedia_2#1": {},
        PUHELINNUMERO: {},
        NIMI: {},
        OPISKELIJALIIKUNTA: {},
        VUOSIKELLO: {},
        OPISKELIJARUOKAILU: {},
        VAPAA_AIKA: {},
        ESTEETOMYYS: {},
        TIETOA_ASUMISESTA: {},
        TYOHARJOITTELU: {},
      },
      nimi: {},
      hakutoimistoEctsNimi: {},
      hakutoimistoEctsEmail: {},
      hakutoimistoEctsPuhelin: {},
      hakutoimistoEctsTehtavanimike: {},
      yhteystiedot: [],
      hakutoimistonNimi: {},
      luontiPvm: 1383808129338,
      muokkausPvm: 1383775200000,
    },
    //  oid: '1.2.246.562.10.346830761110',

    alkuPvm: "1978-06-30",
    kuvaus2: {},
    parentOid: "1.2.246.562.10.00000000001",
    yhteystietoArvos: [
      {
        "YhteystietoArvo.arvoText": "kasko.kriisitiedote@hel.fi",
        "YhteystietojenTyyppi.nimi.fi": "Kriisitiedotuksen sähköpostiosoite",
        "YhteystietojenTyyppi.nimi.sv": "Kriskommunikation epost",
        "YhteystietoElementti.tyyppi": "Email",
        "YhteystietojenTyyppi.oid": "1.2.246.562.5.31532764098",
        "YhteystietoElementti.oid": "1.2.246.562.5.30789631784",
        "YhteystietoElementti.nimi": "Sähköpostiosoite",
        "YhteystietoElementti.nimiSv": "Epostadress",
        "YhteystietoElementti.pakollinen": "false",
        "YhteystietojenTyyppi.nimi.en": "Kriisitiedotuksen sähköpostiosoite",
        "YhteystietoElementti.kaytossa": "true",
        "YhteystietoArvo.kieli": "kieli_fi#1",
      },
      {
        "YhteystietoArvo.arvoText": "kasko.kriisitiedote@hel.fi",
        "YhteystietojenTyyppi.nimi.fi": "Kriisitiedotuksen sähköpostiosoite",
        "YhteystietojenTyyppi.nimi.sv": "Kriskommunikation epost",
        "YhteystietoElementti.tyyppi": "Email",
        "YhteystietojenTyyppi.oid": "1.2.246.562.5.31532764098",
        "YhteystietoElementti.oid": "1.2.246.562.5.30789631784",
        "YhteystietoElementti.nimi": "Sähköpostiosoite",
        "YhteystietoElementti.nimiSv": "Epostadress",
        "YhteystietoElementti.pakollinen": "false",
        "YhteystietojenTyyppi.nimi.en": "Kriisitiedotuksen sähköpostiosoite",
        "YhteystietoElementti.kaytossa": "true",
        "YhteystietoArvo.kieli": "kieli_sv#1",
      },
      {
        "YhteystietoArvo.arvoText": "kasko.kriisitiedote@hel.fi",
        "YhteystietojenTyyppi.nimi.fi": "Kriisitiedotuksen sähköpostiosoite",
        "YhteystietojenTyyppi.nimi.sv": "Kriskommunikation epost",
        "YhteystietoElementti.tyyppi": "Email",
        "YhteystietojenTyyppi.oid": "1.2.246.562.5.31532764098",
        "YhteystietoElementti.oid": "1.2.246.562.5.30789631784",
        "YhteystietoElementti.nimi": "Sähköpostiosoite",
        "YhteystietoElementti.nimiSv": "Epostadress",
        "YhteystietoElementti.pakollinen": "false",
        "YhteystietojenTyyppi.nimi.en": "Kriisitiedotuksen sähköpostiosoite",
        "YhteystietoElementti.kaytossa": "true",
        "YhteystietoArvo.kieli": "kieli_en#1",
      },
    ],
    yhteystiedot: [
      {
        osoiteTyyppi: "kaynti",
        kieli: "kieli_fi#1",
        postinumeroUri: "posti_00510",
        yhteystietoOid: "1.2.246.562.5.23670884842",
        postitoimipaikka: "HELSINKI",
        osoite: "kaynti FI Töysänkatu 2 D",
      },
      {
        osoiteTyyppi: "posti",
        kieli: "kieli_fi#1",
        postinumeroUri: "posti_00099",
        yhteystietoOid: "1.2.246.562.5.75901871253",
        postitoimipaikka: "HELSINGIN KAUPUNKI",
        osoite: "posti FI PL 1",
        ytjPaivitysPvm: "2017-04-18",
      },
      {
        kieli: "kieli_fi#1",
        www: "http://www.hel.fi/www/opev/fi/",
        yhteystietoOid: "1.2.246.562.5.98397496062",
      },
      {
        kieli: "kieli_fi#1",
        numero: "09  3108 600",
        tyyppi: "puhelin",
        yhteystietoOid: "1.2.246.562.5.97189603802",
      },
      {
        kieli: "kieli_fi#1",
        yhteystietoOid: "1.2.246.562.5.2013110709084869686594",
        email: "helsinki.kirjaamo@hel.fi",
      },
      {
        osoiteTyyppi: "kaynti",
        kieli: "kieli_sv#1",
        postinumeroUri: "posti_00530",
        yhteystietoOid: "1.2.246.562.5.38653121071",
        postitoimipaikka: "HELSINGFORS",
        osoite: "SV Hämeentie 11 A",
      },
      {
        osoiteTyyppi: "posti",
        kieli: "kieli_sv#1",
        postinumeroUri: "posti_00099",
        yhteystietoOid: "1.2.246.562.5.41036958387",
        postitoimipaikka: "HELSINGFORS STAD",
        osoite: "SV PL 3000",
      },
    ],
    nimi: {
      fi: "Helsingin kaupunki",
      sv: "Helsingfors stad",
      en: "Helsinki city",
    },
    nimet: [
      {
        nimi: {
          fi: "Helsingin kaupunki",
          sv: "Helsingfors stad",
          en: "Helsinki city",
        },
        alkuPvm: "2004-01-29",
        version: 0,
      },
    ],
    tyypit: [
      ORGANIAATIOTYYPPI_KOULUTUSTOIMIJA,
      ORGANIAATIOTYYPPI_VARHAISKASVATUKSEN_JARJESTAJA,
      // 'organisaatiotyyppi_09'
    ],
    ryhmatyypit: [],
    kayttoryhmat: [],
    postiosoite: {
      postinumeroUri: "posti_00099",
      osoiteTyyppi: "posti",
      yhteystietoOid: "1.2.246.562.5.75901871253",
      postitoimipaikka: "HELSINGIN KAUPUNKI",
      osoite: "posti FI PL 1",
      ytjPaivitysPvm: "2017-04-18",
    },
    yritysmuoto: "Kunta",
    piilotettu: false,
    parentOidPath: "|1.2.246.562.10.00000000001|",
    vuosiluokat: [],
    ytjpaivitysPvm: "2021-10-01",
    ytunnus: FinnishBusinessIds.generateBusinessId(), //'0201256-6',
    kayntiosoite: {
      postinumeroUri: "posti_00510",
      osoiteTyyppi: "kaynti",
      yhteystietoOid: "1.2.246.562.5.23670884842",
      postitoimipaikka: "HELSINKI",
      osoite: "kaynti FI Töysänkatu 2 D",
    },
    kotipaikkaUri: "kunta_091",
    maaUri: "maatjavaltiot1_fin",
    toimipistekoodi: "",
    ytjkieli: "kieli_fi#1",
    kieletUris: [
      "oppilaitoksenopetuskieli_1#2",
      "oppilaitoksenopetuskieli_2#2",
    ],
    muutKotipaikatUris: [],
    muutOppilaitosTyyppiUris: [],
    lisatiedot: [],
    version: 0,
    status: "AKTIIVINEN",
    ...override,
  };
};
