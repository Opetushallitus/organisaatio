import * as React from 'react';
import { createPortal } from 'react-dom';
import styled, { css } from 'styled-components';

import ModalOverlay from '../ModalOverlay';
import { notIn } from '../utils/notIn';

const Wrapper = styled.div.attrs({ role: 'dialog' })`
    z-index: ${({ theme }) => theme.zIndices.modal};
    position: fixed;
    top: 0px;
    left: 0px;
    bottom: 0px;
    right: 0px;
`;

const ContentWrapper = styled.div`
    position: absolute;
    top: 0px;
    left: 0px;
    width: 100%;
    height: 100%;
    align-items: center;
    justify-content: center;
    display: flex;
    box-sizing: border-box;
    padding: ${({ theme }) => theme.space[2]}px;
`;

const Content = styled.div.withConfig({
    shouldForwardProp: notIn(['fullWidth', 'maxWidth']),
})<{ fullWidth: boolean; maxWidth: string }>`
    width: 100%;
    z-index: 2;
    position: relative;
    background-color: white;
    border-radius: ${({ theme }) => theme.radii[1]}px;
    box-shadow:
        0px 11px 15px -7px rgba(0, 0, 0, 0.2),
        0px 24px 38px 3px rgba(0, 0, 0, 0.14),
        0px 9px 46px 8px rgba(0, 0, 0, 0.12);

    ${({ fullWidth, maxWidth }) =>
        !fullWidth &&
        css`
            max-width: ${maxWidth};
        `}
`;

type ModalBaseProps = {
    children: React.ReactNode;
    maxWidth?: string;
    fullWidth?: boolean;
    onClose?: () => void;
    open: boolean;
};

type ModalProps = Omit<React.ComponentProps<typeof Content>, keyof ModalBaseProps> & ModalBaseProps;

const createTarget = (): HTMLDivElement => {
    return document.createElement('div');
};

const Modal = ({
    children,
    maxWidth = '720px',
    fullWidth = false,
    open = false,
    onClose = () => {},
    ...props
}: ModalProps) => {
    const targetRef = React.useRef<HTMLDivElement | null>(null);

    if (!targetRef.current) {
        targetRef.current = createTarget();
    }
    const target = targetRef.current;

    React.useEffect(() => {
        const target = targetRef.current;
        if (!target) {
            return undefined;
        }

        document.body.appendChild(target);

        return () => {
            if (target.parentNode) {
                target.parentNode.removeChild(target);
            }
        };
    }, []);

    const content = open ? (
        <Wrapper>
            <ContentWrapper>
                <ModalOverlay onClick={onClose} />
                <Content maxWidth={maxWidth} fullWidth={fullWidth} {...props}>
                    {children}
                </Content>
            </ContentWrapper>
        </Wrapper>
    ) : null;

    return createPortal(content, target);
};

export default Modal;
