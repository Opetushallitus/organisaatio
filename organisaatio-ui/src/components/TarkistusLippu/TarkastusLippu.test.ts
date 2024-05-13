import assert from 'assert/strict';
import { describe, it } from 'node:test';

import { hasWarning } from './hasWarning';
import moment from 'moment';

describe('TarkastusLippu', () => {
    describe('hasWarning', () => {
        const tests = [
            {
                message: 'is true when tarkastus is > 1 years old',
                tarkastusDate: moment().subtract(5, 'years'),
                alkuDate: moment().subtract(22, 'years'),
                lakkautusDate: undefined,
                shouldWarn: true,
            },
            {
                message: 'is true when tarkastus is undefined',
                tarkastusDate: undefined,
                alkuDate: moment().subtract(22, 'years'),
                lakkautusDate: undefined,
                shouldWarn: true,
            },
            {
                message: 'is false when tarkastus is < 1 years old',
                tarkastusDate: moment().subtract(5, 'months'),
                alkuDate: moment().subtract(22, 'years'),

                lakkautusDate: undefined,

                shouldWarn: false,
            },
            {
                message: 'is false when lakkautettu',
                tarkastusDate: moment().subtract(5, 'years'),
                alkuDate: moment().subtract(22, 'years'),

                lakkautusDate: moment().subtract(1, 'years'),

                shouldWarn: false,
            },
            {
                message: 'is false when in future',
                tarkastusDate: undefined,
                alkuDate: moment().add(1, 'years'),
                lakkautusDate: undefined,
                shouldWarn: false,
            },
        ];
        tests.forEach(({ message, tarkastusDate, alkuDate, lakkautusDate, shouldWarn }) =>
            it(`${message} message`, () => {
                const result = hasWarning({ tarkastusDate, alkuDate, lakkautusDate });
                assert.strictEqual(result, shouldWarn);
            })
        );
    });
});
