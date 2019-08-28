export function hasLength(str: string | null | undefined): boolean {
    return str !== null && str !== undefined && str.length > 0;
}
