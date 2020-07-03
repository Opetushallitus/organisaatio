import * as React from 'react';
import styles from './NimiHistoriaLomake.module.css';
import YksinkertainenTaulukko from "../../../../Taulukot/YksinkertainenTaulukko";




export default function NimiHistoriaLomake() {
    const columns = [
            {
                Header: 'Nimen voimassaolo',
                accessor: 'alkuPvm',
            },
            {
                Header: 'Nimi',
                accessor: 'name',
            }
        ];
  const data = [
      {
        alkuPvm: '1.1.2011',
        name: 'Miikan organisaatio',
      },
    ];
    return(
        <div className={styles.UloinKehys}>
          <YksinkertainenTaulukko data={data} tableColumns={columns} />
        </div>
    );
}