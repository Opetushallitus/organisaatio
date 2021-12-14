var fs = require('fs');
const input = 'localisation.json';
const output = 'localisation.csv';
fs.unlink(output, function (err) {
    if (err) console.log('error deleting');
    console.log('File deleted!');
});
fs.readFile(input, 'utf8', function (err, data) {
    const json = JSON.parse(data);
    const organisaatio2Data = json.filter((a) => a.category === 'organisaatio2');
    fs.appendFile(output, 'category,key,value_fi,value_sv,value_en\n', function (err) {
        if (err) throw err;
    });
    organisaatio2Data
        .filter((a) => a.locale === 'fi')
        .forEach((a) => {
            const sv = organisaatio2Data.find((b) => b.key === a.key && b.locale === 'sv')?.value || '';
            const en = organisaatio2Data.find((b) => b.key === a.key && b.locale === 'en')?.value || '';

            const line = `${a.category},${a.key},${a.value || ''},${sv},${en}\n`;
            fs.appendFile(output, line, function (err) {
                if (err) throw err;
            });
        });
});
