import { hasWarning } from './TarkastusLippu';
import moment from 'moment';

describe('TarkastusLippu', () => {
    describe('hasWarning', () => {
        const tests = [
            [
                'is true when tarkastus is > 1 years old',
                moment().subtract(5, 'years'),
                moment().subtract(22, 'years'),
                undefined,
                true,
            ],
            ['is true when tarkastus is undefined', undefined, moment().subtract(22, 'years'), undefined, true],
            [
                'is false when tarkastus is < 1 years old',
                moment().subtract(5, 'months'),
                moment().subtract(22, 'years'),
                undefined,
                false,
            ],
            [
                'is false when lakkautettu',
                moment().subtract(5, 'years'),
                moment().subtract(22, 'years'),
                moment().subtract(1, 'years'),
                false,
            ],
            ['is false when in future', undefined, moment().add(1, 'years'), undefined, false],
        ];
        test.each(tests)('%s message', (message, tarkastusDate, alkuDate, lakkautusDate, shouldWarn) => {
            // @ts-ignore
            const result = hasWarning({ tarkastusDate, alkuDate, lakkautusDate });
            if (shouldWarn) expect(result).toBeTruthy();
            else expect(result).toBeFalsy();
        });
    });
});
