import React from 'react';

import styles from './Footer.module.css';

export default function Footer() {
    return (
        <footer>
            <hr className={styles.divider} />
            <div className={styles.footer}>
                <div className={styles.column}>
                    <img src="/OPH_Su_Ru_vaaka_RGB.png" alt="Opetushallituksen logo" className={styles.logo} />
                </div>
            </div>
        </footer>
    );
}
