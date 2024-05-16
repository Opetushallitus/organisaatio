import assert from 'assert/strict';
import { describe, it } from 'node:test';

import * as PuhelinnumeroValidator from './PuhelinnumeroValidator';
describe('PuhelinnumeroValidator', () => {
    it('validates puhelinnumero', () => {
        assert.strictEqual(PuhelinnumeroValidator.validate('09-1234567'), true);
        assert.strictEqual(PuhelinnumeroValidator.validate('0501234567'), true);
        assert.strictEqual(PuhelinnumeroValidator.validate('050 123 4567'), true);
        assert.strictEqual(PuhelinnumeroValidator.validate('+358501234567'), true);
        assert.strictEqual(PuhelinnumeroValidator.validate('+358 50 123 4567'), true);
        assert.strictEqual(PuhelinnumeroValidator.validate('0501234567, 0449876543'), false);
        assert.strictEqual(PuhelinnumeroValidator.validate('Testikatu 12'), false);
        assert.strictEqual(PuhelinnumeroValidator.validate('0501234567 / Pekka'), false);
    });
});
