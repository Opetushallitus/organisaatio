const format = new RegExp(/^\d{7}-\d$/);
const multipliers = [7, 9, 10, 5, 8, 4, 2];

export function ytunnusJoiValidator(ytunnus: string | undefined): string {
    const validationError = new Error('failed to validate');

    if (ytunnus === '') return ytunnus;
    if (!ytunnus) throw validationError;
    if (ytunnus.length === 8) {
        ytunnus = '0' + ytunnus;
    }
    if (!format.test(ytunnus)) throw validationError;
    const sum = ytunnus
        .substring(0, 7)
        .split('')
        .map((number, index) => parseInt(number, 10) * multipliers[index])
        .reduce((accumulator, currentValue) => accumulator + currentValue, 0);
    const remainder = sum % 11;
    if (remainder === 1) throw validationError;
    const checksum = remainder === 0 ? 0 : 11 - remainder;
    if (checksum != parseInt(ytunnus[8], 10)) throw validationError;

    return ytunnus;
}
