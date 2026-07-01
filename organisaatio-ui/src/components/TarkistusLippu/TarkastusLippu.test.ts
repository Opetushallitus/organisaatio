import assert from 'assert/strict';
import { describe, it } from 'node:test';
import { addYears, subMonths, subYears } from 'date-fns';

import { hasWarning } from './hasWarning';

describe('TarkastusLippu', () => {
    describe('hasWarning', () => {
        const tests = [
            {
                message: 'is true when tarkastus is > 1 years old',
                tarkastusDate: subYears(new Date(), 5),
                alkuDate: subYears(new Date(), 22),
                lakkautusDate: undefined,
                shouldWarn: true,
            },
            {
                message: 'is true when tarkastus is undefined',
                tarkastusDate: undefined,
                alkuDate: subYears(new Date(), 22),
                lakkautusDate: undefined,
                shouldWarn: true,
            },
            {
                message: 'is false when tarkastus is < 1 years old',
                tarkastusDate: subMonths(new Date(), 5),
                alkuDate: subYears(new Date(), 22),

                lakkautusDate: undefined,

                shouldWarn: false,
            },
            {
                message: 'is false when lakkautettu',
                tarkastusDate: subYears(new Date(), 5),
                alkuDate: subYears(new Date(), 22),

                lakkautusDate: subYears(new Date(), 1),

                shouldWarn: false,
            },
            {
                message: 'is false when in future',
                tarkastusDate: undefined,
                alkuDate: addYears(new Date(), 1),
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
