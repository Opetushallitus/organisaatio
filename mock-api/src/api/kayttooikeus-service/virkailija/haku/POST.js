module.exports = function (request, response) {
    console.log(request);
    const allVirkailijas = [
        {
            oid: '1.2.246.562.99.00000000002',
            etunimet: 'Faija',
            kutsumanimi: 'Faija',
            sukunimi: 'Mehiläinen',
            asiointikieli: 'FI',
            sahkoposti: 'faija@bees.fi',
        },
        {
            oid: '1.2.246.562.99.00000000005',
            etunimet: 'Ville',
            kutsumanimi: 'Ville',
            sukunimi: 'Valtionavustus',
            asiointikieli: 'FI',
            sahkoposti: 'ville.valtionavustus@oph.fi',
        },
    ];
    if (!request.body.organisaatioOids) {
        response.status(200).send(allVirkailijas);
    } else if (request.body.organisaatioOids.includes('1.2.246.562.99.00000000002')) {
        response.status(200).send([allVirkailijas[0]]);
    } else {
        response.status(200).send(allVirkailijas);
    }
};
