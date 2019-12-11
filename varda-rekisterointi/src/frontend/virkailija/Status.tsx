import React, {useEffect, useState} from "react";
import styles from './Status.module.css';

export enum StatusTila {
    PASSIIVINEN,
    PIILOTETTU,
    NAKYVA
}

type Props = {
    tila?: StatusTila,
    teksti: string,
    asetaTila: (uusiTila: StatusTila) => void
}

const nakyvaKesto = 1500;

export default function Status({tila = StatusTila.PASSIIVINEN, teksti, asetaTila}: Props) {

    const [cssLuokka, asetaCssLuokka] = useState(styles.passiivinen);

    useEffect(() => {
        function paatteleCssLuokka(uusiTila: StatusTila) {
            switch (uusiTila) {
                case StatusTila.PASSIIVINEN: return styles.passiivinen;
                case StatusTila.NAKYVA: return styles.nayta;
                case StatusTila.PIILOTETTU: return styles.piilota;
            }
        }
        asetaCssLuokka(paatteleCssLuokka(tila));
        let timerId: number;
        if (tila === StatusTila.NAKYVA) {
            timerId = setTimeout(() => {
                asetaTila(StatusTila.PIILOTETTU);
            }, nakyvaKesto);
        }
        return () => {
            if (timerId) clearTimeout(timerId);
        }
    }, [tila, teksti, asetaTila]);

    return (
        <div className={`${styles.status} ${cssLuokka}`}>
            <i className="material-icons md-18">&#xe5ca;</i><span className={styles.teksti}>{teksti}</span>
        </div>
    );
}
