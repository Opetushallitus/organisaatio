import * as React from 'react';
import styles from './NimiHistoriaLomake.module.css';
import YksinkertainenTaulukko from "../../../../Taulukot/YksinkertainenTaulukko";


type nimiHistoriaProps = {
    nimet: any
    handleOnChange: ({ name, value }: { name: string; value: any; }) => void

}

export default function NimiHistoriaLomake(props: nimiHistoriaProps) {
    const { nimet } = props;


    const columns = [
            {
                Header: 'Nimen voimassaolo',
                accessor: 'alkuPvm'
            },
            {
                Header: 'Nimi',
                Cell: ({ row }: any) => Object.keys(row.original.nimi).map((k, i) => <span>{`${row.original.nimi[k]} [${k}]${(Object.keys(row.original.nimi).length) - 1 > i ? ', ' : ''}`}</span>)
            }
        ];
    return(
        <div className={styles.UloinKehys}>
          <YksinkertainenTaulukko data={nimet} tableColumns={columns} />
        </div>
    );
}