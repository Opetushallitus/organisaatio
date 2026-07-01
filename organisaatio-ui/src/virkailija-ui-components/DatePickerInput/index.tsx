import * as React from 'react';
import { DayPicker, DayPickerProps } from '@daypicker/react';
import { format as formatDate } from 'date-fns/format';
import { isValid as isValidDate } from 'date-fns/isValid';
import { parse as parseDate } from 'date-fns/parse';
import styled from 'styled-components';

import DatePickerStyle from '../DatePickerStyle';
import Input, { InputProps } from '../Input';
import InputIcon from '../InputIcon';
import isString from '../utils/isString';

const firstYear = new Date(0).getFullYear();

const removeLeadingZeros = (value: string) => {
    if (isString(value)) {
        return value.replace(/\b0/g, '');
    }

    return value;
};

const formatDateFn = (value: Date | number, format: string): string => {
    return formatDate(value, format);
};

const parseDateFn = (value: string | undefined, format: string): Date | undefined => {
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

const parseValue = (value: Date | number | string | undefined, format: string): Date | undefined => {
    if (value instanceof Date || typeof value === 'number') {
        const date = new Date(value);
        return isValidDate(date) ? date : undefined;
    }

    return parseDateFn(value, format);
};

const formatValue = (value: Date | number | string | undefined, format: string): string => {
    const date = parseValue(value, format);

    if (date) {
        return formatDateFn(date, format);
    }

    return isString(value) ? value : '';
};

const defaultClassNames = {
    overlay: 'DatePicker__ DatePickerOverlay__',
    overlayWrapper: 'DatePickerOverlayWrapper__',
    container: 'DatePickerInput__',
};

const Container = styled.div`
    position: relative;
    display: inline-block;
    width: 100%;
`;

const OverlayWrapper = styled.div`
    position: absolute;
    top: 100%;
    left: 0;
`;

type OverlayClassNames = Partial<typeof defaultClassNames>;

type DatePickerDayPickerProps = Omit<DayPickerProps, 'mode' | 'required' | 'selected' | 'onSelect'>;

export type DatePickerInputProps = {
    value?: Date | number | string;
    placeholder?: string;
    error?: boolean;
    showIcon?: boolean;
    format?: string;
    classNames?: OverlayClassNames;
    inputProps?: InputProps;
    dayPickerProps?: DatePickerDayPickerProps;
    onChange?: (date: Date) => void;
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
    dayPickerProps = {},
}: DatePickerInputProps) => {
    const classNames = {
        ...classNamesProp,
        overlay: `${defaultClassNames.overlay} ${classNamesProp.overlay || ''}`,
        overlayWrapper: `${defaultClassNames.overlayWrapper} ${classNamesProp.overlayWrapper || ''}`,
        container: `${defaultClassNames.container} ${classNamesProp.container || ''}`,
    };

    const { month, defaultMonth, onMonthChange, ...calendarProps } = dayPickerProps;
    const selectedDate = parseValue(value, format);
    const [inputValue, setInputValue] = React.useState(() => formatValue(value, format));
    const [displayMonth, setDisplayMonth] = React.useState(() => month || defaultMonth || selectedDate || new Date());
    const [isOpen, setIsOpen] = React.useState(false);
    const containerRef = React.useRef<HTMLDivElement>(null);

    React.useEffect(() => {
        setInputValue(formatValue(value, format));
    }, [format, value]);

    React.useEffect(() => {
        if (selectedDate) {
            setDisplayMonth(selectedDate);
        }
    }, [selectedDate?.getTime()]);

    React.useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (!containerRef.current?.contains(event.target as Node)) {
                setIsOpen(false);
            }
        };

        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const openCalendar = () => {
        if (!inputProps.disabled) {
            setIsOpen(true);
        }
    };

    const handleMonthChange = (newMonth: Date) => {
        setDisplayMonth(newMonth);
        onMonthChange?.(newMonth);
    };

    const handleInputChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const nextValue = event.target.value;

        setInputValue(nextValue);
        inputProps.onChange?.(event);

        const parsedDate = parseDateFn(nextValue, format);
        if (parsedDate) {
            setDisplayMonth(parsedDate);
            onChange?.(parsedDate);
        }
    };

    const handleInputFocus = (event: React.FocusEvent<HTMLInputElement>) => {
        openCalendar();
        inputProps.onFocus?.(event);
    };

    const handleInputClick = (event: React.MouseEvent<HTMLInputElement>) => {
        openCalendar();
        inputProps.onClick?.(event);
    };

    const handleInputKeyDown = (event: React.KeyboardEvent<HTMLInputElement>) => {
        if (event.key === 'Escape') {
            setIsOpen(false);
        }

        inputProps.onKeyDown?.(event);
    };

    const handleDaySelect = (date: Date | undefined) => {
        if (!date) {
            return;
        }

        setInputValue(formatDateFn(date, format));
        setDisplayMonth(date);
        setIsOpen(false);
        onChange?.(date);
    };

    return (
        <Container className={classNames.container} ref={containerRef}>
            <DatePickerStyle />
            <Input
                {...inputProps}
                value={inputValue}
                suffix={showIcon && <InputIcon type="event" />}
                error={error}
                placeholder={placeholder}
                onChange={handleInputChange}
                onFocus={handleInputFocus}
                onClick={handleInputClick}
                onKeyDown={handleInputKeyDown}
            />
            {isOpen && (
                <OverlayWrapper className={classNames.overlayWrapper}>
                    <div className={classNames.overlay}>
                        <DayPicker
                            {...calendarProps}
                            mode="single"
                            selected={selectedDate}
                            month={month || displayMonth}
                            onMonthChange={handleMonthChange}
                            onSelect={handleDaySelect}
                        />
                    </div>
                </OverlayWrapper>
            )}
        </Container>
    );
};

export default DatePickerInput;
