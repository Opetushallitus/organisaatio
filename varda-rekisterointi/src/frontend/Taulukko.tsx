import React from "react";

export type SarakeNimi<T> = keyof T;

export interface OtsikkoGeneraattori<T> {
    (sarake: SarakeNimi<T>): JSX.Element
}

const oletusOtsikkoGeneraattori = function<T>(sarake: SarakeNimi<T>) {
    return (
        <th key={sarake.toString()}>{sarake}</th>
    )
};

export interface RiviGeneraattori<T> {
    (key: string, rivi: T, sarakkeet: SarakeNimi<T>[]): JSX.Element
}

const oletusRiviGeneraattori = function<T>(key: string, rivi: T, sarakkeet: SarakeNimi<T>[]) {
    return (
        <tr key={key}>
            {
                sarakkeet.map(sarake =>
                    <td key={sarake.toString()}>{rivi[sarake]}</td>
                )
            }
        </tr>
    );
};

export type TaulukkoProps<T> = {
    data: T[],
    otsikkoGeneraattori?: OtsikkoGeneraattori<T>,
    riviGeneraattori?: RiviGeneraattori<T>,
    sarakkeet: SarakeNimi<T>[]
}

export function Taulukko<T>({ data, otsikkoGeneraattori = oletusOtsikkoGeneraattori, riviGeneraattori = oletusRiviGeneraattori, sarakkeet }: TaulukkoProps<T>) {
    return (
        <table>
            <thead>
                <tr key="thead">
                {
                    sarakkeet.map(sarake => otsikkoGeneraattori(sarake))
                }
                </tr>
            </thead>
            <tbody>
            {
                data.map((rivi, index) => riviGeneraattori(index.toString(), rivi, sarakkeet))
            }
            </tbody>
        </table>
    );
}
