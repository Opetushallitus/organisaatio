import { useState } from 'react';

export const useTranslatedInput = (initialValue: string | undefined = '', name: string, disabled: boolean) => {
    const [value, setValue] = useState<string>(initialValue);
    return {
        name,
        value,
        setValue,
        reset: () => setValue(''),
        bind: {
            disabled,
            name,
            value,
            onChange: (event) => {
                setValue(event.target.value);
            },
        },
    };
};
