export const useTranslatedInput = (
    initialValue: string | undefined = '',
    name: string,
    disabled: boolean,
    localizationKey: string,
    validationRegister: any
) => {
    return {
        localizationKey,
        name,
        bind: {
            localizationKey,
            disabled,
            name,
            ...validationRegister,
        },
    };
};
