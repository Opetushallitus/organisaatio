import * as React from 'react';

const HtmlButton = React.forwardRef<HTMLButtonElement, React.ComponentProps<'button'>>(
    ({ type = 'button', ...props }, ref) => {
        return <button type={type} ref={ref} {...props} />;
    }
);

HtmlButton.displayName = 'HtmlButton';

export type HtmlButtonProps = React.ComponentProps<'button'>;

export default HtmlButton;
