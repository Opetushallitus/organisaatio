import * as React from 'react';
import {useContext, useState} from "react";
import styles from './UusiToimijaLomake.module.css';
import PohjaSivu from "../../PohjaSivu/PohjaSivu";
import Accordion from "../../../Accordion/Accordion"
import Button from "@opetushallitus/virkailija-ui-components/Button";
import Spin from "@opetushallitus/virkailija-ui-components/Spin";

import homeIcon from '@iconify/icons-fa-solid/home';

import {LanguageContext} from "../../../../contexts/contexts";
import {
    Koodi,
    Organisaatio,
} from "../../../../types/types"
import PerustietoLomake from "./PerustietoLomake/PerustietoLomake";
import YhteystietoLomake from "./YhteystietoLomake/YhteystietoLomake";
import Icon from "@iconify/react";
import useAxios from "axios-hooks";

const urlPrefix = process.env.NODE_ENV === 'development' ? '/api' : '/organisaatio-ui';

const tyhjaOrganisaatio = {
    ytunnus: '',
    nimi: '',
    nimet: '',
    alkuPvm: null,
    yritysmuoto: '',
    tyypit: [],
    kotipaikkaUri: '',
    muutKotipaikatUris: [],
    maaUri: '',
    kieletUris: [] ,
    yhteystiedot: [{
        puhelinnumero: '',
        sahkoposti: '',
        postiosoite: {
            katuosoite: '',
            postinumeroUri: '',
            postitoimipaikka: ''
        },
        kayntiosoite: {
            katuosoite: '',
            postinumeroUri: '',
            postitoimipaikka: ''
        }
    }]
}

const UusiToimijaLomake = (props: any) => {
    const { i18n } = useContext(LanguageContext);
    const [{ data: organisaatioTyypit, loading: organisaatioTyypitLoading, error: organisaatioTyypitError}] = useAxios<Koodi[]>(
        `${urlPrefix}/koodisto/ORGANISAATIOTYYPPI/koodi`);
    const [{ data: maatJaValtiot, loading: maatJaValtiotLoading, error: maatJaValtiotError}] = useAxios<Koodi[]>(
        `${urlPrefix}/koodisto/MAATJAVALTIOT1/koodi`);
    const [{ data: oppilaitoksenOpetuskielet, loading: oppilaitoksenOpetuskieletLoading, error: oppilaitoksenOpetuskieletError}] = useAxios<Koodi[]>(
        `${urlPrefix}/koodisto/OPPILAITOKSENOPETUSKIELI/koodi`);
    const [organisaatio, setOrganisaatio] = useState<Organisaatio>(tyhjaOrganisaatio);
    if (organisaatioTyypitLoading || organisaatioTyypitError || maatJaValtiotLoading || maatJaValtiotError || oppilaitoksenOpetuskieletLoading || oppilaitoksenOpetuskieletError) {
        return (<div className={styles.PaaOsio}>
            <Spin>ladataan sivua </Spin>
        </div>);
    }
    return(
        <PohjaSivu>
            <div className={styles.YlaBanneri}>
                <div>
                    <a href="/"><Icon icon={homeIcon} />{i18n.translate('UUSI_TOIMIJA')}</a>
                </div>
            </div>
            <div className={styles.ValiContainer}>
                <div className={styles.ValiOtsikko}>
                    <h3>{i18n.translate('TOIMIJA')}</h3>
                    <h1>{i18n.translate('UUDEN_TOIMIJAN_LISAAMINEN')}</h1>
                </div>
            </div>
            <div className={styles.PaaOsio} >
                <Accordion
                    lomakkeet={[
                        <PerustietoLomake
                            setOrganisaatio={setOrganisaatio}
                            organisaatioTyypit={organisaatioTyypit}
                            organisaatio={organisaatio}
                            maatJaValtiot={maatJaValtiot}
                            opetuskielet={oppilaitoksenOpetuskielet}
                        />,
                        <YhteystietoLomake
                            yhteystiedot={organisaatio.yhteystiedot}
                        />,
                    ]}
                    otsikot={['Perustiedot', 'Yhteystiedot',]} // TODO kriisisähköposti?
                />
            </div>
            <div className={styles.AlaBanneri}>
                <div>
                    <Button variant="outlined" className={styles.Versionappula}>{i18n.translate('SULJE_TIEDOT')}
                    </Button>
                    <Button className={styles.Versionappula}>{i18n.translate('TALLENNA')}
                    </Button>
                </div>
            </div>
        </PohjaSivu>
    );
}

export default UusiToimijaLomake;