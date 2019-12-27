const PATTERN = /^(\+|-| |\(|\)|[0-9]){3,100}$/;

export function validate(puhelinnumero: string): boolean {
    return PATTERN.test(puhelinnumero);
}
