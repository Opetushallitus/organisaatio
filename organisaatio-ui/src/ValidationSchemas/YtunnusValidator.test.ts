import { ytunnusJoiValidator } from './YtunnusValidator';

describe('ytunnusJoiValidator', () => {
    test.each([
        ['Passes on valid ytunnus', '1572860-0', '1572860-0'],
        ['Passes on valid ytunnus starting with 0', '0737546-2', '0737546-2'],
        ['Passes and sets preceding 0 when its missing from ytunnus', '157286-0', '0157286-0'],
        ['Passes on empty string', '', ''],
    ])('%s', (_, input, expected) => expect(ytunnusJoiValidator(input)).toStrictEqual(expected));
    test.each([
        ['Throws on faulty syntax ytunnus', '1572860+0'],
        ['Throws on non valid ytunnus', '1234567-9'],
        ['Throws on undefined ytunnus', undefined],
    ])('%s', (_, input) => expect(() => ytunnusJoiValidator(input)).toThrow());
});
