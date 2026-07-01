import { isAfter, isBefore, subYears } from 'date-fns';

export const hasWarning = ({
    tarkastusDate,
    alkuDate,
    lakkautusDate,
}: {
    tarkastusDate?: Date;
    alkuDate?: Date;
    lakkautusDate?: Date;
}): boolean => {
    const now = new Date();
    const lastYear = subYears(now, 1);
    const tarkastusOk = tarkastusDate && isAfter(tarkastusDate, lastYear);
    const activeNow = alkuDate && isBefore(alkuDate, now) && (!lakkautusDate || isAfter(lakkautusDate, now));
    return !tarkastusOk && !!activeNow;
};
