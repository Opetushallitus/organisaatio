import { useState } from 'react';

export const useTranslatedInput = (
    initialValue: string | undefined = '',
    name: string,
    disabled: boolean,
    localizationKey: string
) => {
    const [value, setValue] = useState<string>(initialValue);
    return {
        localizationKey,
        name,
        value,
        setValue,
        reset: () => setValue(''),
        bind: {
            localizationKey,
            disabled,
            name,
            value,
            onChange: (event) => {
                setValue(event.target.value);
            },
        },
    };
};
