import { format } from 'date-fns/format';
import { isValid } from 'date-fns/isValid';
import { parse } from 'date-fns/parse';

export const UI_DATE_FORMAT = 'd.M.yyyy';
export const API_DATE_FORMAT = 'yyyy-MM-dd';

export const parseDateInput = (
    date: Date | string | undefined,
    dateFormats: string[] | string = [UI_DATE_FORMAT],
    defaultToToday = false
): Date | undefined => {
    if (date === undefined || date === '') {
        return defaultToToday ? new Date() : undefined;
    }

    if (date instanceof Date) {
        return isValid(date) ? date : undefined;
    }

    const formats = Array.isArray(dateFormats) ? dateFormats : [dateFormats];
    for (const dateFormat of formats) {
        const parsedDate = parse(date, dateFormat, new Date());
        if (isValid(parsedDate)) {
            return parsedDate;
        }
    }

    return undefined;
};

export const formatDateInput = (
    date: Date | string | undefined,
    outputFormat: string,
    dateFormats?: string[] | string,
    defaultToToday = false
): string => {
    const parsedDate = parseDateInput(date, dateFormats, defaultToToday);
    return parsedDate ? format(parsedDate, outputFormat) : '';
};
