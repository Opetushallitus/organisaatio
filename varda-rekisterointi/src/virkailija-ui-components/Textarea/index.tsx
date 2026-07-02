import * as React from 'react';

import { InputBase } from '../Input';

const TextareaBase = InputBase.withComponent('textarea');

type TextareaBaseProps = {
    error?: boolean;
    disabled?: boolean;
    rows?: number;
};

export type TextareaProps = TextareaBaseProps &
    Omit<React.ComponentProps<typeof TextareaBase>, keyof TextareaBaseProps>;

const Textarea = React.forwardRef<HTMLTextAreaElement, TextareaProps>(
    ({ rows = 3, error = false, disabled = false, ...props }, ref) => (
        <TextareaBase ref={ref} error={error} disabled={disabled} rows={rows} {...props} />
    )
);

Textarea.displayName = 'Textarea';

export default Textarea;
