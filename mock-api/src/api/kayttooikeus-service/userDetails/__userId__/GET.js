module.exports = function (request, response) {
    response.sendFile(`any.json`, { root: __dirname });
};
