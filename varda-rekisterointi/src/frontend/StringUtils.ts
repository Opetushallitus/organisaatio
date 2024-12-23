export function isNonEmpty(str: string | null | undefined): boolean {
    return str !== null && str !== undefined && str.length > 0;
}

export function hasLengthBetween(str: string | null | undefined, min: number, max: number): boolean {
    return str !== null && str !== undefined && str.length >= min && str.length <= max;
}
