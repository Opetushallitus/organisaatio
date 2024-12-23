import assert from 'assert/strict';
import { describe, it } from 'node:test';

import * as YtunnusValidator from './YtunnusValidator';

describe('YtunnusValidator', () => {
    it('validates y-tunnus', () => {
        assert.strictEqual(YtunnusValidator.validate('1572860-0'), true);
        assert.strictEqual(YtunnusValidator.validate('0737546-2'), true);
        assert.strictEqual(YtunnusValidator.validate('1572860+0'), false);
        assert.strictEqual(YtunnusValidator.validate('1234567-9'), false);
        assert.strictEqual(YtunnusValidator.validate('157286-0'), true);
    });
});
