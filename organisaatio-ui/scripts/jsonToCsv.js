const fs = require('fs');
const { format } = require('@fast-csv/format');
const input = 'localisation.json';
const output = 'localisation.csv';
const encodingOut = 'utf16le';
const separator = '\t';
fs.unlink(output, function (err) {
    if (err) console.log('error deleting');
    console.log('File deleted!');
});
fs.readFile(input, 'utf8', function (err, data) {
    const json = JSON.parse(data);
    const organisaatio2Data = json.filter((a) => a.category === 'organisaatio2');
    fs.appendFile(
        output,
        '\ufeff' +
            //'sep=, \n' +
            `"category"${separator}"key"${separator}"value_fi"${separator}"value_sv"${separator}"value_en"`,
        { encoding: encodingOut },
        function (err) {
            if (err) throw err;
        }
    );
    organisaatio2Data.sort((a, b) => {
        return a.key.localeCompare(b.key);
    });
    organisaatio2Data
        .filter((a) => a.locale === 'fi')
        .forEach((a) => {
            const sv = organisaatio2Data.find((b) => b.key === a.key && b.locale === 'sv')?.value || '';
            const en = organisaatio2Data.find((b) => b.key === a.key && b.locale === 'en')?.value || '';

            const line = `"${a.category}"${separator}"${a.key}${separator}"${
                a.value || ''
            }"${separator}"${sv}"${separator}"${en}"\n`;
            fs.appendFile(output, line, { encoding: encodingOut }, function (err) {
                if (err) throw err;
            });
        });
});
