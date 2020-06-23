import * as React from 'react';
import styles from './PerustietoLomake.module.css';

export default function PerustietoLomake() {
    return(
        <div className={styles.UloinKehys}>
                <div className={styles.VasenKolumni}>
                    <span className={styles.AvainKevyestiBoldattu}> avain</span>
                    <span>arvo</span>
                </div>
                <div className={styles.OikeaKolumni}>
                    <span className={styles.AvainKevyestiBoldattu}> avain</span>
                    <span>arvo</span>
                </div>
                {
                   // props.children
                }
        </div>
    );
}