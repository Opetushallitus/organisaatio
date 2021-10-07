export const isYTunnus = (input: string) => {
    // Y-Tunnus on muotoa NNNNNNN-T
    return (
        input.length === 9 &&
        // Tarkistetaan että numeropositioissa on vain numeroita.
        /\d{7}.\d/.test(input) &&
        // Tarkistetaan että välimerkki on väliviiva.
        input.charAt(7) === '-'
    );
};
