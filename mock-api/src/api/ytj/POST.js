module.exports = function (request, response) {
    const body = request.body['soap:envelope']['soap:body'][0];
    console.log(body);
    if (body.wmyritystiedotv2) {
        const yTunnus = body.wmyritystiedotv2[0].ytunnus[0];
        console.log(`sending ${yTunnus} response`);
        response.sendFile(`${yTunnus}.xml`, { root: __dirname });
    } else if (body.wmyrityshaku) {
        const hakusana = body.wmyrityshaku[0].hakusana[0];
        console.log(`sending ${hakusana} response`);
        const shortHakusana = hakusana.toLowerCase().substring(0, 6);
        response.sendFile(`${shortHakusana}.xml`, { root: __dirname });
    }
};