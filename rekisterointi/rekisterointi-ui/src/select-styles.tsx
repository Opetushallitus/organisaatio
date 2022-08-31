import React from 'react';
import { StylesConfig, components } from 'react-select';

export const reactSelectStyles = (error: boolean = false): StylesConfig => ({
    control: (provided) => ({
        ...provided,
        borderRadius: 2,
        border: `${error ? '2px' : '1px'} solid ${error ? '#CC3300' : '#B2B2B2'}`,
        height: '46px',
        cursor: 'pointer',
        margin: error ? '0' : '1px',
    }),
    menu: (provided) => ({
        ...provided,
        borderRadius: 2,
        border: '1px solid #B2B2B2',
        cursor: 'pointer',
        marginTop: '3px',
        boxShadow: 'none',
    }),
    option: (provided, state) => ({
        ...provided,
        color: '#000000',
        backgroundColor: state.isFocused ? '#D8D8D8' : '#FFFFFF',
        fontSize: '16px',
        lineHeight: '16px',
    }),
    indicatorSeparator: (provided) => ({
        ...provided,
        width: 0,
    }),
});

const DropdownSvg = () => (
    <svg width="12" height="8" viewBox="0 0 12 8" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path
            fillRule="evenodd"
            clipRule="evenodd"
            d="M11.7763 1.91917L6.52627 7.7525C6.37777 7.9175 6.20202 8 5.99902 8C5.79602 8 5.62027 7.9175 5.47177 7.7525L0.221773 1.91917C0.0732734 1.75417 -0.000976562 1.55889 -0.000976562 1.33333C-0.000976562 1.10778 0.0732734 0.9125 0.221773 0.7475C0.370273 0.5825 0.546023 0.5 0.749023 0.5H11.249C11.452 0.5 11.6278 0.5825 11.7763 0.7475C11.9248 0.9125 11.999 1.10778 11.999 1.33333C11.999 1.55889 11.9248 1.75417 11.7763 1.91917Z"
            fill="#4C4C4C"
        />
    </svg>
);

export const DropdownIndicator = (props: any) => {
    return (
        <components.DropdownIndicator {...props}>
            <DropdownSvg />
        </components.DropdownIndicator>
    );
};
