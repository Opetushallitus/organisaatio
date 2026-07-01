import * as React from 'react';
import styled from 'styled-components';

const ModalOverlay = styled.div`
    background-color: ${({ theme }) => theme.colors.modalOverlay};
    position: absolute;
    top: 0px;
    left: 0px;
    width: 100%;
    height: 100%;
    z-index: 1;
`;

export type ModalOverlayProps = React.ComponentProps<typeof ModalOverlay>;

export default ModalOverlay;
