import React, { useEffect, useRef, useState } from 'react';
import styles from './RajausAccordion.module.css';

type RajausAccordionProps = React.PropsWithChildren<{
    header: string;
    selectionDescription: string;
    open: boolean;
    onToggleOpen: () => void;
}>;

export function RajausAccordion({ header, selectionDescription, open, onToggleOpen, children }: RajausAccordionProps) {
    const descriptionRef = useRef<HTMLSpanElement>(null);
    const [descriptionOverflowing, setDescriptionOverflowing] = useState<boolean>(false);
    useEffect(() => {
        const overflowing = descriptionRef.current !== null && isContentOverflowing(descriptionRef.current);
        if (overflowing !== descriptionOverflowing) setDescriptionOverflowing(overflowing);
    });

    function toggleOpenOnSpaceOrEnter(event) {
        if (event.key === 'Enter' || event.key === ' ') {
            onToggleOpen();
        }
    }

    return (
        <section className={styles.RajausAccordion}>
            <div
                tabIndex={0}
                role="button"
                aria-pressed="false"
                className={styles.AccordionTitle}
                onKeyDown={toggleOpenOnSpaceOrEnter}
                onClick={onToggleOpen}
            >
                <h3>{header}</h3>
                <span
                    ref={descriptionRef}
                    aria-live="off"
                    className={styles.AccordionSelectionDescription + ' ' + (descriptionOverflowing ? styles.Fade : '')}
                >
                    {selectionDescription}
                </span>
                <AccordionButton open={open} disabled={false} />
            </div>
            {open && (
                <div className={styles.AccordionContentContainer}>
                    <div className={styles.AccordionContent}>{children}</div>
                </div>
            )}
        </section>
    );
}
function isContentOverflowing(element: HTMLElement) {
    return element.clientWidth < element.scrollWidth;
}

type AccordionButtonProps = {
    open: boolean;
    disabled: boolean;
};

function AccordionButton({ open, disabled }: AccordionButtonProps) {
    const icon = disabled ? (
        <IconAccordionDisabledButton />
    ) : open ? (
        <IconAccordionCloseButton />
    ) : (
        <IconAccordionOpenButton />
    );
    return <div className={styles.AccordionButton}>{icon}</div>;
}

function IconAccordionOpenButton() {
    return (
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M8 12.5L0 4.49998L1.43333 3.06665L8 9.66665L14.5667 3.09998L16 4.53332L8 12.5Z" fill="#AEAEAE" />
        </svg>
    );
}

function IconAccordionCloseButton() {
    return (
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path
                d="M1.43333 12.5L0 11.0667L8 3.06665L16 11.0333L14.5667 12.4667L8 5.89998L1.43333 12.5Z"
                fill="#AEAEAE"
            />
        </svg>
    );
}

function IconAccordionDisabledButton() {
    return (
        <svg width="16" height="17" viewBox="0 0 16 17" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path
                d="M2.35889 15.1668L1.33325 14.1412L6.97428 8.50016L1.33325 2.85914L2.35889 1.8335L7.99992 7.47452L13.6409 1.8335L14.6666 2.85914L9.02556 8.50016L14.6666 14.1412L13.6409 15.1668L7.99992 9.5258L2.35889 15.1668Z"
                fill="#999999"
            />
        </svg>
    );
}
