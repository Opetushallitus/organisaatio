import * as PuhelinnumeroValidator from './PuhelinnumeroValidator';
it('validates puhelinnumero', () => {
    expect(PuhelinnumeroValidator.validate('09-1234567')).toEqual(true);
    expect(PuhelinnumeroValidator.validate('0501234567')).toEqual(true);
    expect(PuhelinnumeroValidator.validate('050 123 4567')).toEqual(true);
    expect(PuhelinnumeroValidator.validate('+358501234567')).toEqual(true);
    expect(PuhelinnumeroValidator.validate('+358 50 123 4567')).toEqual(true);
    expect(PuhelinnumeroValidator.validate('0501234567, 0449876543')).toEqual(false);
    expect(PuhelinnumeroValidator.validate('Testikatu 12')).toEqual(false);
    expect(PuhelinnumeroValidator.validate('0501234567 / Pekka')).toEqual(false);
});
