import * as React from 'react';
import {useContext} from "react";
import styles from './LomakeSivu.module.css';
import PohjaSivu from "../PohjaSivu/PohjaSivu";
import Accordion from "../../Accordion/Accordion"
import Button from "@opetushallitus/virkailija-ui-components/Button";

import {LanguageContext} from "../../../contexts/contexts";
import {Icon} from "@iconify/react";
import chevronRight from "@iconify/icons-fa-solid/chevron-right";
import Box from "@opetushallitus/virkailija-ui-components/Box";
import Input from "@opetushallitus/virkailija-ui-components/Input";
import PerustietoLomake from "./PerustietoLomake/PerustietoLomake";


const LomakeSivu = () => {
    const { i18n } = useContext(LanguageContext);
    return(
        <PohjaSivu>
            <div className={styles.YlaBanneri}>
                <span>yläosa</span>
            </div>
            <div className={styles.ValiContainer}>
                <div className={styles.ValiOtsikko}>
                    <h3>Koulutustoimija</h3>
                    <h1>Helsingin kaupunki</h1>
                </div>
                <div className={styles.ValiNappulat}>
                    <Button>{i18n.translate('YHDISTA_ORGANISAATIO')}</Button>
                    <Button>+ {i18n.translate('LISAA_UUSI_OPPILAITOS')}</Button>
                </div>
            </div>
            <div className={styles.PaaOsio} >
                <Accordion lomakkeet={<PerustietoLomake />}/>
            </div>
            <div className={styles.AlaBanneri}>
                <span>alaosa</span>
            </div>
        </PohjaSivu>
    );
}

export default LomakeSivu;