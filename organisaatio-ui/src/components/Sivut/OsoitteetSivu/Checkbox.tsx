import React from 'react';
import styles from './Checkbox.module.css';

export type CustomCheckboxProps = React.InputHTMLAttributes<HTMLInputElement> & {
    checked: boolean;
    onChange: (checked: boolean) => void;
    disabled: boolean;
    dataTestid?: string;
    indeterminate?: boolean;
};

export function Checkbox({ children, checked, onChange, disabled, indeterminate, dataTestid }: CustomCheckboxProps) {
    return (
        <label className={styles.Label}>
            <input
                className={styles.Checkbox}
                type="checkbox"
                checked={checked}
                disabled={disabled}
                onChange={() => onChange(!checked)}
                data-testid={dataTestid}
                ref={(el) => el && (el.indeterminate = !!indeterminate)}
            />
            {children}
        </label>
    );
}

export function CheckedIcon() {
    return (
        <svg width="20" height="20" viewBox="2 2 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
            <g filter="url(#filter0_di_2076_18368)">
                <rect x="2" y="2" width="20" height="20" rx="3" fill="#0A789C" />
            </g>
            <path d="M10 17L5 12L6.41 10.59L10 14.17L17.59 6.57999L19 7.99999L10 17Z" fill="white" />
            <mask
                id="mask0_2076_18368"
                style={{ maskType: 'luminance' }}
                maskUnits="userSpaceOnUse"
                x="5"
                y="6"
                width="14"
                height="11"
            >
                <path d="M10 17L5 12L6.41 10.59L10 14.17L17.59 6.57999L19 7.99999L10 17Z" fill="white" />
            </mask>
            <g mask="url(#mask0_2076_18368)"></g>
            <defs>
                <filter
                    id="filter0_di_2076_18368"
                    x="0"
                    y="0"
                    width="24"
                    height="24"
                    filterUnits="userSpaceOnUse"
                    colorInterpolationFilters="sRGB"
                >
                    <feFlood floodOpacity="0" result="BackgroundImageFix" />
                    <feColorMatrix
                        in="SourceAlpha"
                        type="matrix"
                        values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 127 0"
                        result="hardAlpha"
                    />
                    <feOffset />
                    <feGaussianBlur stdDeviation="1" />
                    <feColorMatrix type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.01 0" />
                    <feBlend mode="normal" in2="BackgroundImageFix" result="effect1_dropShadow_2076_18368" />
                    <feBlend mode="normal" in="SourceGraphic" in2="effect1_dropShadow_2076_18368" result="shape" />
                    <feColorMatrix
                        in="SourceAlpha"
                        type="matrix"
                        values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 127 0"
                        result="hardAlpha"
                    />
                    <feOffset dy="1" />
                    <feGaussianBlur stdDeviation="1" />
                    <feComposite in2="hardAlpha" operator="arithmetic" k2="-1" k3="1" />
                    <feColorMatrix type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.195312 0" />
                    <feBlend mode="normal" in2="shape" result="effect2_innerShadow_2076_18368" />
                </filter>
            </defs>
        </svg>
    );
}

export function UncheckedIcon() {
    return (
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
            <g filter="url(#filter0_i_2076_18062)">
                <rect width="20" height="20" rx="3" fill="white" />
            </g>
            <rect x="0.5" y="0.5" width="19" height="19" rx="2.5" stroke="#CCCCCC" />
            <defs>
                <filter
                    id="filter0_i_2076_18062"
                    x="0"
                    y="0"
                    width="20"
                    height="21"
                    filterUnits="userSpaceOnUse"
                    colorInterpolationFilters="sRGB"
                >
                    <feFlood floodOpacity="0" result="BackgroundImageFix" />
                    <feBlend mode="normal" in="SourceGraphic" in2="BackgroundImageFix" result="shape" />
                    <feColorMatrix
                        in="SourceAlpha"
                        type="matrix"
                        values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 127 0"
                        result="hardAlpha"
                    />
                    <feOffset dy="1" />
                    <feGaussianBlur stdDeviation="1.5" />
                    <feComposite in2="hardAlpha" operator="arithmetic" k2="-1" k3="1" />
                    <feColorMatrix type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.165393 0" />
                    <feBlend mode="normal" in2="shape" result="effect1_innerShadow_2076_18062" />
                </filter>
            </defs>
        </svg>
    );
}
