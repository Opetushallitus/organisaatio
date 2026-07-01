import * as React from 'react';
import styled from 'styled-components';

import { disabledStyle, space, SpaceProps } from '../system';

const FormLabelBase = styled.label<{ disabled: boolean; error: boolean } & SpaceProps>`
    ${({ theme }) => theme.typography.body};
    color: ${({ theme }) => theme.colors.text.heading};
    display: block;
    margin-bottom: ${({ theme }) => theme.space[1]}px;

    ${space};
    ${disabledStyle}
`;

export type FormLabelProps = Omit<React.ComponentProps<typeof FormLabelBase>, 'error' | 'disabled'> & {
    error?: boolean;
    disabled?: boolean;
};

const FormLabel = ({ error = false, disabled = false, ...props }: FormLabelProps) => {
    return <FormLabelBase error={error} disabled={disabled} {...props} />;
};

export default FormLabel;
