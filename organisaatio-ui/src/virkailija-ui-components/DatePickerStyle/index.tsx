import '@daypicker/react/style.css';

import { createGlobalStyle } from 'styled-components';

const DatePickerStyle = createGlobalStyle`
  .DatePicker__ {
    .rdp-root {
      --rdp-accent-color: ${({ theme }) => theme.colors.primary.main};
      --rdp-selected-border: 2px solid ${({ theme }) => theme.colors.primary.main};
      --rdp-today-color: ${({ theme }) => theme.colors.primary.main};
      margin: 0;
      font-family: ${({ theme }) => theme.fonts.main};
    }

    .rdp-caption_label {
      color: ${({ theme }) => theme.colors.text.primary} !important;
    }

    .rdp-day_button {
      color: ${({ theme }) => theme.colors.text.primary};
      font: inherit;
    }

    .rdp-day:not(.rdp-outside):not(.rdp-selected) .rdp-day_button:hover {
      background-color: rgba(0, 0, 0, .05) !important;
    }

    .rdp-today:not(.rdp-outside) .rdp-day_button {
      color: ${({ theme }) => theme.colors.primary.main};
      font-weight: normal !important;
    }

    .rdp-selected .rdp-day_button {
      background-color: ${({ theme }) => theme.colors.primary.main} !important;
      color: ${({ theme }) => theme.colors.primary.contrastText} !important;
      font-weight: ${({ theme }) => theme.fontWeights.bold} !important;
    }

    .rdp-weekday {
      color: ${({ theme }) => theme.colors.text.secondary} !important;
      opacity: 1;
    }

    .rdp-chevron {
      fill: ${({ theme }) => theme.colors.primary.main};
    }
  }

  .DatePickerOverlay__ {
    border: 1px solid ${({ theme }) => theme.colors.divider};
    border-radius: ${({ theme }) => theme.radii[1]}px;
    box-shadow: ${({ theme }) => theme.shadows.dropdownMenu};
    background-color: white;
  }

  .DatePickerOverlayWrapper__ {
    display: inline-block !important;
    transform: translateY(${({ theme }) => theme.space[1]}px);
    position: absolute;
    z-index: ${({ theme }) => theme.zIndices.datePicker};
  }
`;

export default DatePickerStyle;
