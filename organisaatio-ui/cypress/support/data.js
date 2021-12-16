const { FinnishBusinessIds } = require('finnish-business-ids');

export const organisaatio = (prefix = '', override) => {
    return {
        ...{
            parentOid: `1.2.246.562.10.00000000001`,
            ytunnus: FinnishBusinessIds.generateBusinessId(),
            nimi: { fi: `${prefix} Suominimi`, sv: `${prefix} Ruotsi`, en: `${prefix} Enkku` },
            status: ``,
            nimet: [
                {
                    nimi: { fi: `${prefix} Suominimi`, sv: `${prefix} Ruotsi`, en: `${prefix} Enkku` },
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
                { kieli: `kieli_fi#1`, email: `${prefix}-FI.noreply@test.com` },
                { kieli: `kieli_fi#1`, www: `http://${prefix}-test.com` },
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
                { kieli: `kieli_sv#1`, email: `${prefix}-SV.noreply@test.com` },
                { kieli: `kieli_sv#1`, www: `http://${prefix}-test.com` },
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
                { kieli: `kieli_en#1`, email: `${prefix}-EN.noreply@test.com` },
                { kieli: `kieli_en#1`, www: `http://${prefix}-test.com` },
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
            yhteystietoArvos: [
                {
                    //KOSKI sahkoposti
                    'YhteystietoArvo.arvoText': 'testi@testi.com',
                    'YhteystietoArvo.kieli': 'kieli_fi#1',
                    'YhteystietojenTyyppi.oid': '1.2.246.562.5.79385887983',
                    'YhteystietoElementti.oid': '1.2.246.562.5.57850489428',
                    'YhteystietoElementti.pakollinen': false,
                    'YhteystietoElementti.kaytossa': true,
                },
            ],
        },
        ...override,
    };
};
