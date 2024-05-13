import assert from 'assert/strict';
import { describe, it } from 'node:test';

import { ytunnusJoiValidator } from './YtunnusValidator';

describe('ytunnusJoiValidator', () => {
    it('Passes on valid ytunnus', () => {
        assert.strictEqual(ytunnusJoiValidator('1572860-0'), '1572860-0');
    });
    it('Passes on valid ytunnus starting with 0', () => {
        assert.strictEqual(ytunnusJoiValidator('0737546-2'), '0737546-2');
    });
    it('Passes and sets preceding 0 when its missing from ytunnus', () => {
        assert.strictEqual(ytunnusJoiValidator('157286-0'), '0157286-0');
    });
    it('Passes on empty string', () => {
        assert.strictEqual(ytunnusJoiValidator(''), '');
    });
    it('Throws on faulty syntax ytunnus', () => {
        assert.throws(() => ytunnusJoiValidator('1572860+0'));
    });
    it('Throws on faulty syntax ytunnus', () => {
        assert.throws(() => ytunnusJoiValidator('1234567-9'));
    });
    it('Throws on faulty syntax ytunnus', () => {
        assert.throws(() => ytunnusJoiValidator(undefined));
    });
});
