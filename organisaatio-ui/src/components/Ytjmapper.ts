

export default function(yritystiedot: any, alkuperainenOrg: any) {
    const nimi: any = {
        "nimi" : {
        },
        "alkuPvm" : ""
    };

    // Tarkistetaan "kenttien" olemassaolo, sillä yritystiedot voidaan täyttää myöhemminkin
    if (yritystiedot.nimi) {
        nimi.nimi.fi = yritystiedot.nimi;
    }
    if (yritystiedot.svNimi) {
        nimi.nimi.sv = yritystiedot.svNimi;
    }
    if (yritystiedot.ytunnus) {
        alkuperainenOrg.ytunnus = yritystiedot.ytunnus;
    }
    if (yritystiedot.yritysmuoto) {
        alkuperainenOrg.yritysmuoto = yritystiedot.yritysmuoto;
    }
    // yrityksenKieli, sitten kun koodiston kielet on saatu
    // postiOsoite, sitten kun koodiston postinumerot on saatu
    // kayntiOsoite, sitten kun koodiston postinumerot on saatu
    if (yritystiedot.sahkoposti) {
        alkuperainenOrg.yhteystiedot['kieli_fi#1'].email.email = yritystiedot.sahkoposti;
    }
    if (yritystiedot.www) {
        alkuperainenOrg.yhteystiedot['kieli_fi#1'].www.www = yritystiedot.www;
    }
    if (yritystiedot.puhelin) {
        alkuperainenOrg.yhteystiedot['kieli_fi#1'].puhelin.numero = yritystiedot.puhelin;
    }
    // kotipaikka / kotipaikkaKoodi, sitten kun koodiston kotipaikat on saatu
    if (yritystiedot.aloitusPvm) {
        let pvm = new Date(yritystiedot.aloitusPvm);
        const offset = pvm.getTimezoneOffset();
        pvm = new Date(pvm.getTime() - (offset*60*1000));
        const yYYMMDDdate = pvm.toISOString().split('T')[0];
        alkuperainenOrg.organisaatio.alkuPvm = yYYMMDDdate;
    }

    // TODO ???
    //  YTunnuksella luotu organisaatio on oletusarvoisesti koulutustoimija
    // Ei kuitenkaan poisteta "Koulutustoimija" tyyppiä, jos se on jo asetettu
     //const organisaatiotyyppi = "organisaatiotyyppi_01"; // Koulutustoimija
    //if (alkuperainenOrg.organisaatio.tyypit.indexOf(organisaatiotyyppi) === -1) {
    //    this.toggleCheckOrganisaatio(organisaatiotyyppi);
    //}
    /* TODO mikä on?
    if (typeof model.ytjTiedot.yrityksenKieli === "string") {
        var kieli = KIELET[model.ytjTiedot.yrityksenKieli.trim().toLowerCase()];
        if (kieli) {
            model.organisaatio.ytjkieli = kieli.kieliUriAndVersio;
        }
    }

    */

    // asetetaan päivitys timestamp
    let pvm = new Date();
    const offset = pvm.getTimezoneOffset();
    pvm = new Date(pvm.getTime() - (offset*60*1000));
    const yYYMMDDdate = pvm.toISOString().split('T')[0];

    alkuperainenOrg.organisaatio.ytjpaivitysPvm = yYYMMDDdate;
    nimi.alkuPvm = yYYMMDDdate;

    /* Lisätään nimi nimihistoriaan, jos se eroaa nykyisestä nimestä
    var nimiHistoriaModel = NimiHistoriaModel;

    // Uuden organisaation tapauksess ei ole nimihistoriaa
    if (nimiHistoriaModel.getNimi() === null) {
        nimiHistoriaModel.getNimihistoria().push(nimi);
        // this.setNimet();
    }
    else if (deepEquals(nimiHistoriaModel.getNimi().nimi, nimi.nimi) === false) {
        if (nimiHistoriaModel.getNimi().alkuPvm === nimi.alkuPvm) {
            nimiHistoriaModel.getNimi().nimi = nimi.nimi;
        }
        else {
            nimiHistoriaModel.getNimihistoria().push(nimi);
        }
        //this.setNimet();
    }

     */

};

export function deepEquals(x: any, y : any) {
    if (x === y) {
        return true; // if both x and y are null or undefined and exactly the same
    } else if (!(x instanceof Object) || !(y instanceof Object)) {
        return false; // if they are not strictly equal, they both need to be Objects
    } else if (x.constructor !== y.constructor) {
        // they must have the exact same prototype chain, the closest we can do is
        // test their constructor.
        return false;
    } else {
        for (const p in x) {
            if (!x.hasOwnProperty(p)) {
                continue; // other properties were tested using x.constructor === y.constructor
            }
            if (!y.hasOwnProperty(p)) {
                return false; // allows to compare x[ p ] and y[ p ] when set to undefined
            }
            if (x[p] === y[p]) {
                continue; // if they have the same strict value or identity then they are equal
            }
            if (typeof (x[p]) !== 'object') {
                return false; // Numbers, Strings, Functions, Booleans must be strictly equal
            }
            if (!deepEquals(x[p], y[p])) {
                return false;
            }
        }
        for (const p in y) {
            if (y.hasOwnProperty(p) && !x.hasOwnProperty(p)) {
                return false;
            }
        }
        return true;
    }
}