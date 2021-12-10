module.exports = function (request, response) {
    switch (request.params.oid) {
        case 'devaaja':
            response.sendFile(`devaaja.json`, { root: __dirname });
            break;
        case '1.2.246.562.24.83319065944':
            response.send(401, 'string');
        default:
            response.sendFile(`anonymous.json`, { root: __dirname });
    }
};
