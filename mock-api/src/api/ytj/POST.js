module.exports = function (request, response) {
    try {
        const body = request.body['soap:envelope']['soap:body'][0];
        if (body.wmyritystiedotv2) {
            const yTunnus = body.wmyritystiedotv2[0].ytunnus[0];
            response.sendFile(`${yTunnus}.xml`, { root: __dirname });
        } else if (body.wmyrityshaku) {
            const hakusana = body.wmyrityshaku[0].hakusana[0];
            const shortHakusana = hakusana.toLowerCase().substring(0, 6);
            response.sendFile(`${shortHakusana}.xml`, { root: __dirname });
        }
    } catch (e) {
        console.error(e);
        response.status(500).send(e);
    }
};
