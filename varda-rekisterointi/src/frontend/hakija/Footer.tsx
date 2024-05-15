import React from 'react';
import logo from './OPH_Su_Ru_vaaka_RGB.png';
import styles from './Footer.module.css';

export default function Footer() {
    return (
        <div>
            <hr className={styles.divider} />
            <div className={styles.footer}>
                <div className={styles.column}>
                    <img src={logo} alt="Opetushallituksen logo" className={styles.logo} />
                </div>
            </div>
        </div>
    );
}
