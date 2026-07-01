import 'react-day-picker/lib/style.css';

import * as React from 'react';
import DayPickerInput from 'react-day-picker/DayPickerInput';
import formatDate from 'date-fns/format';
import isValidDate from 'date-fns/isValid';
import parseDate from 'date-fns/parse';

import DatePickerStyle from '../DatePickerStyle';
import Input from '../Input';
import InputIcon from '../InputIcon';
import isString from '../utils/isString';

const firstYear = new Date(0).getFullYear();

const removeLeadingZeros = (value: string) => {
    if (isString(value)) {
        return value.replace(/\b0/g, '');
    }

    return value;
};

const formatDateFn = (value: Date | number, format: string) => {
    return formatDate(value, format);
};

const parseDateFn = (value: string | undefined, format: string) => {
    if (!isString(value) || value === '') {
        return undefined;
    }

    const parsedDate = parseDate(value, format, new Date());

    if (!isValidDate(parsedDate)) {
        return undefined;
    }

    const parseIsDeterministic = removeLeadingZeros(value) === removeLeadingZeros(formatDate(parsedDate, format));

    if (!parseIsDeterministic) {
        return undefined;
    }

    if (parsedDate.getFullYear() < firstYear) {
        return undefined;
    }
    return parsedDate || undefined;
};

const defaultClassNames = {
    overlay: 'DatePicker__ DatePickerOverlay__',
    overlayWrapper: 'DatePickerOverlayWrapper__',
    container: 'DatePickerInput__',
};

type DayPickerInputProps = React.ComponentProps<typeof DayPickerInput>;

export type DatePickerInputProps = Omit<DayPickerInputProps, 'classNames' | 'onChange' | 'format'> & {
    error?: boolean;
    showIcon?: boolean;
    format?: string;
    classNames?: { [x: string]: string };
    onChange?: DayPickerInputProps['onDayChange'];
};

export const DatePickerInput = ({
    value,
    placeholder = '',
    format = 'd.M.yyyy',
    onChange = () => {},
    inputProps = {},
    error = false,
    showIcon = true,
    classNames: classNamesProp = {},
    ...props
}: DatePickerInputProps) => {
    const classNames = {
        ...classNamesProp,
        overlay: `${defaultClassNames.overlay} ${classNamesProp.overlay || ''}`,
        overlayWrapper: `${defaultClassNames.overlayWrapper} ${classNamesProp.overlayWrapper || ''}`,
        container: `${defaultClassNames.container} ${classNamesProp.container || ''}`,
    };

    return (
        <>
            <DatePickerStyle />
            <DayPickerInput
                classNames={classNames}
                format={format}
                value={value}
                component={Input}
                inputProps={{
                    ...inputProps,
                    suffix: showIcon && <InputIcon type="event" />,
                    error,
                }}
                parseDate={parseDateFn}
                formatDate={formatDateFn}
                placeholder={placeholder}
                onDayChange={onChange}
                {...props}
            />
        </>
    );
};

export default DatePickerInput;
