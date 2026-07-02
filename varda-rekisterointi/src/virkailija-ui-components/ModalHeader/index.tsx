import * as React from 'react';

import styled from 'styled-components';

import Box from '../Box';
import Typography from '../Typography';
import isFunction from '../utils/isFunction';
import Icon from '../Icon';

type ModalHeaderProps = {
    disableTypography?: boolean;
    onClose?: () => void;
    className?: string;
    style?: React.CSSProperties;
    children: React.ReactNode;
};

const Wrapper = styled(Box)`
    border-bottom: 1px solid ${({ theme }) => theme.colors.divider};
`;

const CloseIcon = styled(Icon).attrs({ role: 'button', type: 'close' })`
    cursor: pointer;
    color: ${({ theme }) => theme.colors.text.heading};
    opacity: 0.5;
    transition: opacity 0.25s;

    &:hover {
        opacity: 1;
    }
`;

const ModalHeader = ({ disableTypography = false, onClose, className, style, children }: ModalHeaderProps) => {
    return (
        <Wrapper display="flex" justifyContent="space-between" p={2} className={className} style={style}>
            <Box>{disableTypography ? children : <Typography variant="h5">{children}</Typography>}</Box>

            {isFunction(onClose) && <CloseIcon onClick={onClose} />}
        </Wrapper>
    );
};

export default ModalHeader;
