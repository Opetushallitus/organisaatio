type RgbColor = {
    r: number;
    g: number;
    b: number;
};

type HslColor = {
    h: number;
    s: number;
    l: number;
};

const clamp = (value: number, min: number, max: number) => Math.min(Math.max(value, min), max);

const parseHexColor = (hexColor: string): RgbColor => {
    const hex = hexColor.replace('#', '');
    const normalizedHex =
        hex.length === 3
            ? hex
                  .split('')
                  .map((char) => char + char)
                  .join('')
            : hex;
    const color = parseInt(normalizedHex, 16);

    return {
        r: (color >> 16) & 255,
        g: (color >> 8) & 255,
        b: color & 255,
    };
};

const toHexChannel = (value: number) => {
    return Math.round(value).toString(16).padStart(2, '0');
};

const rgbToHex = ({ r, g, b }: RgbColor) => `#${toHexChannel(r)}${toHexChannel(g)}${toHexChannel(b)}`;

const rgbToHsl = ({ r, g, b }: RgbColor): HslColor => {
    const red = r / 255;
    const green = g / 255;
    const blue = b / 255;
    const max = Math.max(red, green, blue);
    const min = Math.min(red, green, blue);
    const lightness = (max + min) / 2;

    if (max === min) {
        return { h: 0, s: 0, l: lightness };
    }

    const delta = max - min;
    const saturation = lightness > 0.5 ? delta / (2 - max - min) : delta / (max + min);
    let hue = 0;

    if (max === red) {
        hue = (green - blue) / delta + (green < blue ? 6 : 0);
    } else if (max === green) {
        hue = (blue - red) / delta + 2;
    } else {
        hue = (red - green) / delta + 4;
    }

    return { h: hue / 6, s: saturation, l: lightness };
};

const hueToRgb = (p: number, q: number, t: number) => {
    let hue = t;
    if (hue < 0) {
        hue += 1;
    }
    if (hue > 1) {
        hue -= 1;
    }
    if (hue < 1 / 6) {
        return p + (q - p) * 6 * hue;
    }
    if (hue < 1 / 2) {
        return q;
    }
    if (hue < 2 / 3) {
        return p + (q - p) * (2 / 3 - hue) * 6;
    }
    return p;
};

const hslToRgb = ({ h, s, l }: HslColor): RgbColor => {
    if (s === 0) {
        const value = l * 255;
        return { r: value, g: value, b: value };
    }

    const q = l < 0.5 ? l * (1 + s) : l + s - l * s;
    const p = 2 * l - q;

    return {
        r: hueToRgb(p, q, h + 1 / 3) * 255,
        g: hueToRgb(p, q, h) * 255,
        b: hueToRgb(p, q, h - 1 / 3) * 255,
    };
};

const lightenColor = (amount: number, hexColor: string) => {
    const hsl = rgbToHsl(parseHexColor(hexColor));
    return rgbToHex(hslToRgb({ ...hsl, l: clamp(hsl.l + amount, 0, 1) }));
};

const toRgba = (hexColor: string, alpha: number) => {
    const { r, g, b } = parseHexColor(hexColor);
    return `rgba(${r}, ${g}, ${b}, ${alpha})`;
};

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
        focusOutline: toRgba(bg_red, 0.2),
    };

    const colors = {
        bg_white,
        divider: 'rgba(0, 0, 0, 0.15)',
        inputBorder: '#ced4da',
        modalOverlay: 'rgba(0, 0, 0, 0.5)',
        primary: {
            main: bg_blue,
            contrastText: bg_white,
            focusOutline: toRgba(bg_blue, 0.2),
        },
        success: {
            main: bg_green,
            contrastText: bg_white,
            focusOutline: toRgba(bg_green, 0.2),
        },
        secondary: {
            main: bg_gray,
            contrastText: bg_white,
            focusOutline: toRgba(bg_gray, 0.2),
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
        hoverBackgroundColor: lightenColor(0.1, backgroundColor),
        focusOutlineColor,
    } as const;
};

const createOutlinedButtonVariant = (outlineColor: string, focusOutlineColor: string) => {
    return {
        outlineColor,
        hoverOutlineColor: lightenColor(0.1, outlineColor),
        focusOutlineColor,
    } as const;
};

const createTextButtonVariant = (color: string, focusOutlineColor: string) => {
    return {
        color,
        hoverColor: lightenColor(0.1, color),
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
