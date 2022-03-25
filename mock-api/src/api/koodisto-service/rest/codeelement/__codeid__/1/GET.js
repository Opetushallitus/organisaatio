module.exports = function (request, response) {
    response.json({
        koodiUri: request.params.codeid,
    });
};
