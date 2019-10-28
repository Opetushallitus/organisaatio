import * as YtunnusValidator from './YtunnusValidator';

it('validates y-tunnus', () => {
    expect(YtunnusValidator.validate('1572860-0')).toEqual(true);
    expect(YtunnusValidator.validate('0737546-2')).toEqual(true);
    expect(YtunnusValidator.validate('1572860+0')).toEqual(false);
    expect(YtunnusValidator.validate('1234567-9')).toEqual(false);
    expect(YtunnusValidator.validate('157286-0')).toEqual(true);
});
