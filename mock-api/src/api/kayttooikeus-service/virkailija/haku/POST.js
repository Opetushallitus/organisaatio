module.exports = function (request, response) {
    console.log(request.body);
    const virkailijasByOid = {
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

    const virkailijas = request.body.organisaatioOids.flatMap((oid) => virkailijasByOid[oid]).filter(Boolean);
    console.log(virkailijas);
    response.status(200).send(virkailijas);
};
