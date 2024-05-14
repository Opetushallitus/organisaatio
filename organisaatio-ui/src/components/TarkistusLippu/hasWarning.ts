import moment, { Moment } from 'moment';

export const hasWarning = ({
    tarkastusDate,
    alkuDate,
    lakkautusDate,
}: {
    tarkastusDate?: Moment;
    alkuDate?: Moment;
    lakkautusDate?: Moment;
}): boolean => {
    const now = moment();
    const lastYear = moment();
    lastYear.subtract(1, 'years');
    const tarkastusOk = tarkastusDate?.isAfter(lastYear);
    const activeNow = alkuDate && alkuDate.isBefore(now) && (!lakkautusDate || lakkautusDate.isAfter(now));
    return !tarkastusOk && !!activeNow;
};
