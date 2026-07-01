import { CustomHelpers } from 'joi';
import { isValid } from 'date-fns';
import { parseDateInput, UI_DATE_FORMAT } from '../tools/dateUtils';

export const uiDateValidator = (value: unknown, helpers: CustomHelpers) => {
    if (value instanceof Date) {
        return isValid(value) ? value : helpers.error('any.invalid');
    }

    if (typeof value !== 'string') {
        return helpers.error('string.base');
    }

    if (value.trim() === '') {
        return helpers.error('string.empty');
    }

    const parsedDate = parseDateInput(value, UI_DATE_FORMAT);
    return parsedDate ? value : helpers.error('any.invalid');
};
