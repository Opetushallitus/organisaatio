const format = new RegExp(/^\d{7}-\d$/);
const multipliers = [7, 9, 10, 5, 8, 4, 2];

export function validate(ytunnus: string): boolean {
    if (ytunnus.length === 8) {
        ytunnus = '0' + ytunnus;
    }
    if (!format.test(ytunnus)) {
        return false;
    }
    const sum = ytunnus
        .substring(0, 7)
        .split('')
        .map((number, index) => parseInt(number, 10) * (multipliers[index] ?? 0))
        .reduce((accumulator, currentValue) => accumulator + currentValue, 0);
    const remainder = sum % 11;
    if (remainder === 1) {
        return false;
    }
    const checksum = remainder === 0 ? 0 : 11 - remainder;
    return checksum === parseInt(ytunnus[8] ?? 'NaN', 10);
}
