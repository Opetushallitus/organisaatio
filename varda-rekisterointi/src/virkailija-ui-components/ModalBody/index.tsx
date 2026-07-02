import * as React from 'react';

import styled from 'styled-components';

const Body = styled.div`
    padding: ${({ theme }) => theme.space[2]}px;
`;

const Wrapper = styled.div<{
    maxHeight: string;
}>`
    max-height: ${({ maxHeight }) => maxHeight};
    overflow-y: auto;
`;

export type ModalBodyProps = React.ComponentProps<typeof Body> & {
    maxHeight?: string;
};

const ModalBody = ({ maxHeight = '400px', ...props }: ModalBodyProps) => (
    <Wrapper maxHeight={maxHeight}>
        <Body {...props} />
    </Wrapper>
);

export default ModalBody;
