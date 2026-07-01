import 'react-day-picker/lib/style.css';

import { createGlobalStyle } from 'styled-components';

const DatePickerStyle = createGlobalStyle`
  .DatePicker__ {
    .DayPicker-Caption {
      color: ${({ theme }) => theme.colors.text.primary} !important;
    }

    .DayPicker-wrapper {
      font-family: ${({ theme }) => theme.fonts.main};
    }

    .DayPicker-Day {
      color: ${({ theme }) => theme.colors.text.primary};

      &:not(.DayPicker-Day--outside):not(.DayPicker-Day--selected) {
        &:hover {
          background-color: rgba(0, 0, 0, .05) !important;
        }
      }
    }
    
    .DayPicker-Day--today {
      color: ${({ theme }) => theme.colors.primary.main};
      font-weight: normal !important;
    }

    .DayPicker-Day--selected:not(.DayPicker-Day--outside) {
      background-color: ${({ theme }) => theme.colors.primary.main} !important;
      font-weight: ${({ theme }) => theme.fontWeights.bold} !important;
    }

    .DayPicker-Weekday {
      color: ${({ theme }) => theme.colors.text.secondary} !important;
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
