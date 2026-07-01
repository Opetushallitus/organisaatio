import { lighten, rgba } from 'polished';

const createColors = () => {
    const bg_white = '#ffffff';
    const bg_black = '#2a2a2a';
    const bg_gray = '#666';
    const bg_gray_lighten_2 = '#aeaeae';
    const bg_blue = '#0a789c';
    const bg_red = '#e44e4e';
    const bg_green = '#4c7f00';

    const error = {
        main: bg_red,
        contrastText: bg_white,
        focusOutline: rgba(bg_red, 0.2),
    };

    const colors = {
        bg_white,
        divider: 'rgba(0, 0, 0, 0.15)',
        inputBorder: '#ced4da',
        modalOverlay: 'rgba(0, 0, 0, 0.5)',
        primary: {
            main: bg_blue,
            contrastText: bg_white,
            focusOutline: rgba(bg_blue, 0.2),
        },
        success: {
            main: bg_green,
            contrastText: bg_white,
            focusOutline: rgba(bg_green, 0.2),
        },
        secondary: {
            main: bg_gray,
            contrastText: bg_white,
            focusOutline: rgba(bg_gray, 0.2),
        },
        error,
        danger: error,
        text: {
            primary: bg_gray,
            secondary: bg_gray_lighten_2,
            heading: bg_black,
        },
    } as const;

    return colors;
};

const createContainedButtonVariant = (backgroundColor: string, color: string, focusOutlineColor: string) => {
    return {
        backgroundColor,
        color,
        hoverBackgroundColor: lighten(0.1, backgroundColor),
        focusOutlineColor,
    } as const;
};

const createOutlinedButtonVariant = (outlineColor: string, focusOutlineColor: string) => {
    return {
        outlineColor,
        hoverOutlineColor: lighten(0.1, outlineColor),
        focusOutlineColor,
    } as const;
};

const createTextButtonVariant = (color: string, focusOutlineColor: string) => {
    return {
        color,
        hoverColor: lighten(0.1, color),
        focusOutlineColor,
    } as const;
};

export const createTheme = () => {
    const fontFamily = "'Roboto', sans-serif";
    const space = [0, 8, 16, 24, 32, 40, 48, 56, 64] as const;
    const radii = [0, 4] as const;
    const breakpoints = ['576px', '768px', '992px'] as const;

    const fontWeights = {
        bold: 500,
        regular: 400,
    } as const;

    const fonts = {
        main: fontFamily,
    } as const;

    const lineHeights = {
        body: '1.5',
        heading: '1.2',
    } as const;

    const shadows = {
        0: 'none',
        1: '0 2px 8px rgba(0,0,0,0.15)',
        dropdownMenu: '0 2px 8px rgba(0,0,0,0.15)',
    } as const;

    const colors = createColors();

    const fontSizes = {
        body: '1rem',
        secondary: '0.85rem',
        h1: '3rem',
        h2: '2.5rem',
        h3: '2rem',
        h4: '1.5rem',
        h5: '1.25rem',
        h6: '1rem',
    } as const;

    const baseTypography = {
        lineHeight: lineHeights.body,
        fontFamily: fonts.main,
    } as const;

    const baseHeadingTypography = {
        lineHeight: lineHeights.heading,
        fontFamily: fonts.main,
        color: colors.text.heading,
        fontWeight: fontWeights.bold,
    } as const;

    const typography = {
        body: {
            ...baseTypography,
            fontSize: fontSizes.body,
            color: colors.text.primary,
        },
        secondary: {
            ...baseTypography,
            fontSize: fontSizes.secondary,
            color: colors.text.secondary,
        },
        h1: {
            ...baseHeadingTypography,
            fontSize: fontSizes.h1,
        },
        h2: {
            ...baseHeadingTypography,
            fontSize: fontSizes.h2,
        },
        h3: {
            ...baseHeadingTypography,
            fontSize: fontSizes.h3,
        },
        h4: {
            ...baseHeadingTypography,
            fontSize: fontSizes.h4,
        },
        h5: {
            ...baseHeadingTypography,
            fontSize: fontSizes.h5,
        },
        h6: {
            ...baseHeadingTypography,
            fontSize: fontSizes.h6,
        },
    } as const;

    const zIndices = {
        datePicker: 500,
        modal: 501,
        drawer: 502,
    } as const;

    const disabled = {
        cursor: 'not-allowed',
        opacity: 0.5,
    } as const;

    const buttonVariants = {
        contained: {
            primary: createContainedButtonVariant(
                colors.primary.main,
                colors.primary.contrastText,
                colors.primary.focusOutline
            ),
            success: createContainedButtonVariant(
                colors.success.main,
                colors.success.contrastText,
                colors.success.focusOutline
            ),
            danger: createContainedButtonVariant(
                colors.danger.main,
                colors.danger.contrastText,
                colors.danger.focusOutline
            ),
            secondary: createContainedButtonVariant(
                colors.secondary.main,
                colors.secondary.contrastText,
                colors.secondary.focusOutline
            ),
        },
        outlined: {
            primary: createOutlinedButtonVariant(colors.primary.main, colors.primary.focusOutline),
            success: createOutlinedButtonVariant(colors.success.main, colors.success.focusOutline),
            danger: createOutlinedButtonVariant(colors.danger.main, colors.danger.focusOutline),
            secondary: createOutlinedButtonVariant(colors.secondary.main, colors.secondary.focusOutline),
        },
        text: {
            primary: createTextButtonVariant(colors.primary.main, colors.primary.focusOutline),
            success: createTextButtonVariant(colors.success.main, colors.success.focusOutline),
            danger: createTextButtonVariant(colors.danger.main, colors.danger.focusOutline),
            secondary: createTextButtonVariant(colors.secondary.main, colors.secondary.focusOutline),
        },
    };

    return {
        space,
        radii,
        colors,
        fontWeights,
        fonts,
        lineHeights,
        shadows,
        breakpoints,
        fontSizes,
        typography,
        zIndices,
        disabled,
        buttonVariants,
    } as const;
};

export type Theme = ReturnType<typeof createTheme>;

export default createTheme;
