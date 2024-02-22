const kayttooikeusryhmaVirkailijas = {
    'Mansikkalan tietäjä': [
        {
            oid: '1.2.246.562.99.00000054321',
            etunimet: 'Mauritz',
            kutsumanimi: 'Mauritz',
            sukunimi: 'Mansikka',
            asiointikieli: 'FI',
            sahkoposti: 'mauritz.mansikka@mauri.fi',
        },
    ],
    'Puolakan kiertäjä': [
        {
            oid: '1.2.246.562.99.00000000005',
            etunimet: 'Ville',
            kutsumanimi: 'Ville',
            sukunimi: 'Valtionavustus',
            asiointikieli: 'FI',
            sahkoposti: 'ville.valtionavustus@oph.fi',
        },
        {
            oid: '1.2.246.562.99.00000033333',
            etunimet: 'Paula',
            kutsumanimi: 'Paula',
            sukunimi: 'Puolukka',
            asiointikieli: 'FI',
            sahkoposti: 'puolakan.paula@puolakka.fi',
        },
    ],
};

const organisaatioVirkailijas = {
    '1.2.246.562.99.00000000002': [
        {
            oid: '1.2.246.562.99.00000000002',
            etunimet: 'Faija',
            kutsumanimi: 'Faija',
            sukunimi: 'Mehiläinen',
            asiointikieli: 'FI',
            sahkoposti: 'faija@bees.fi',
        },
    ],
    '1.2.246.562.99.00000000005': [
        {
            oid: '1.2.246.562.99.00000000005',
            etunimet: 'Ville',
            kutsumanimi: 'Ville',
            sukunimi: 'Valtionavustus',
            asiointikieli: 'FI',
            sahkoposti: 'ville.valtionavustus@oph.fi',
        },
        {
            oid: '1.2.246.562.99.00000123456',
            etunimet: 'Matti',
            kutsumanimi: 'Matti',
            sukunimi: 'Meikäläinen',
            asiointikieli: 'FI',
            sahkoposti: 'matti.meikäläinen@koulu.fi',
        },
    ],
};

module.exports = function (request, response) {
    console.log(request.body);
    const virkailijasByKayttooikeusryhma =
        request.body.kayttoOikeusRyhmaNimet &&
        request.body.kayttoOikeusRyhmaNimet.flatMap((k) => kayttooikeusryhmaVirkailijas[k]).filter(Boolean);
    const virkailijasByOrganisaatio =
        request.body.organisaatioOids &&
        request.body.organisaatioOids.flatMap((oid) => organisaatioVirkailijas[oid]).filter(Boolean);
    if (virkailijasByKayttooikeusryhma && virkailijasByOrganisaatio) {
        const intersection = virkailijasByKayttooikeusryhma.filter(
            (v) => !!virkailijasByOrganisaatio.find((k) => k.oid === v.oid)
        );
        console.log(intersection);
        response.status(200).send(intersection);
    } else if (virkailijasByKayttooikeusryhma) {
        response.status(200).send(virkailijasByKayttooikeusryhma);
    } else {
        response.status(200).send(virkailijasByOrganisaatio);
    }
};
