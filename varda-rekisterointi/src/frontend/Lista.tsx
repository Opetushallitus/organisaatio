import React from "react";

export interface RiviTunnisteGeneraattori<T> {
    (rivi: T): string;
}

export interface SarakeGeneraattori<T> {
    (rivi: T): ListaSarakeProps[];
}

type ListaProps<T> = {
    otsikot: string[];
    rivit: T[];
    tunnisteGeneraattori: RiviTunnisteGeneraattori<T>;
    sarakeGeneraattori: SarakeGeneraattori<T>;
}
export function Lista<T>({ otsikot, rivit, tunnisteGeneraattori, sarakeGeneraattori }: ListaProps<T>) {
    return (
      <table className="varda-lista">
          <OtsikkoRivi key="otsikkorivi" otsikot={otsikot}/>
          <tbody>
          {
              rivit.map(rivi =>
                  <ListaRivi rivi={rivi} key={tunnisteGeneraattori(rivi)} sarakeGeneraattori={sarakeGeneraattori}/>)
          }
          </tbody>
      </table>
    );
}

type OtsikkoRiviProps = {
    otsikot: string[];
}
export function OtsikkoRivi({ otsikot }: OtsikkoRiviProps) {
    return (
      <thead>
        <tr>
        {
            otsikot.map(otsikko =>
                <th key={otsikko}>{otsikko}</th>)
        }
        </tr>
      </thead>
    )
}

type ListaRiviProps<T> = {
    rivi: T;
    sarakeGeneraattori: SarakeGeneraattori<T>;
}
export function ListaRivi<T>({ rivi, sarakeGeneraattori }: ListaRiviProps<T>) {
    const sarakePropsit = sarakeGeneraattori(rivi);
    return (
        <tr className="varda-lista-rivi">
        {
            sarakePropsit.map((propsit, indeksi) =>  <ListaSarake key={indeksi} {...propsit}/>)
        }
        </tr>
    )
}

type ListaSarakeProps = {
    data: string;
}
export function ListaSarake({ data }: ListaSarakeProps) {
    return (
        <td>{data}</td>
    );
}
