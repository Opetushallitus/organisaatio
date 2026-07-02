import * as React from 'react';

import styled from 'styled-components';

const ModalFooter = styled.div`
    padding: ${({ theme }) => theme.space[2]}px;
    border-top: 1px solid ${({ theme }) => theme.colors.divider};
`;

export type ModalFooterProps = React.ComponentProps<typeof ModalFooter>;

export default ModalFooter;
