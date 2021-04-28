import * as React from 'react';
import { useContext, useReducer, useState } from 'react';
import styles from './UusiToimijaLomake.module.css';
import PohjaSivu from '../../PohjaSivu/PohjaSivu';
import Accordion from '../../../Accordion/Accordion';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';

import homeIcon from '@iconify/icons-fa-solid/home';

import { LanguageContext } from '../../../../contexts/contexts';
import { Koodi, Organisaatio } from '../../../../types/types';
import PerustietoLomake from './PerustietoLomake/PerustietoLomake';
import YhteystietoLomake from './YhteystietoLomake/YhteystietoLomake';
import Icon from '@iconify/react';
import useAxios from 'axios-hooks';
import Axios from 'axios';

const urlPrefix = process.env.NODE_ENV === 'development' ? '/api' : '/organisaatio';

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
    kieletUris: [],
    yhteystiedot: ['kieli_fi#1', 'kieli_sv#1', 'kieli_en#1']
        .map((kieli) => [
            {
                kieli,
                tyyppi: 'puhelin',
                numero: '',
            },
            {
                kieli,
                email: '',
            },
            {
                kieli,
                www: '',
            },
            {
                kieli,
                osoiteTyyppi: 'posti',
                osoite: '',
                postinumeroUri: '',
                postitoimipaikka: '',
            },
            {
                kieli,
                osoiteTyyppi: 'kaynti',
                osoite: '',
                postinumeroUri: '',
                postitoimipaikka: '',
            },
        ])
        .flat(),
};

const UusiToimijaLomake = (props: any) => {
    const { i18n } = useContext(LanguageContext);
    const [{ data: organisaatioTyypit, loading: organisaatioTyypitLoading, error: organisaatioTyypitError }] = useAxios<
        Koodi[]
    >(`${urlPrefix}/koodisto/ORGANISAATIOTYYPPI/koodi`);
    const [{ data: maatJaValtiot, loading: maatJaValtiotLoading, error: maatJaValtiotError }] = useAxios<Koodi[]>(
        `${urlPrefix}/koodisto/MAATJAVALTIOT1/koodi`
    );
    const [
        {
            data: oppilaitoksenOpetuskielet,
            loading: oppilaitoksenOpetuskieletLoading,
            error: oppilaitoksenOpetuskieletError,
        },
    ] = useAxios<Koodi[]>(`${urlPrefix}/koodisto/OPPILAITOKSENOPETUSKIELI/koodi`);
    const [organisaatio, setOrganisaatio] = useReducer(
        (state: Organisaatio, newState: Organisaatio) => ({ ...state, ...newState }),
        tyhjaOrganisaatio
    );

    async function postOrganisaatio() {
        try {
            const response = await Axios.post(`${urlPrefix}/organisaatio/v4/`, organisaatio);
            console.error('saved org response', response);
            props.history.push('/');
        } catch (error) {
            console.error('error while posting org', error);
        } finally {
        }
    }

    function handleCancel() {
        setOrganisaatio(Object.assign({}, tyhjaOrganisaatio));
        props.history.push('/');
    }

    const [lomakeAvoinna, setLomakeAvoinna] = useState(0);

    const handleOnChange = ({ name, value }: { name: string; value: any }) => {
        setOrganisaatio({ [name]: value } as Organisaatio);
    };
    if (
        organisaatioTyypitLoading ||
        organisaatioTyypitError ||
        maatJaValtiotLoading ||
        maatJaValtiotError ||
        oppilaitoksenOpetuskieletLoading ||
        oppilaitoksenOpetuskieletError
    ) {
        return (
            <div className={styles.PaaOsio}>
                <Spin>ladataan sivua </Spin>
            </div>
        );
    }
    return (
        <PohjaSivu>
            <div className={styles.YlaBanneri}>
                <div>
                    <a href="/">
                        <Icon icon={homeIcon} />
                        {i18n.translate('UUSI_TOIMIJA')}
                    </a>
                </div>
            </div>
            <div className={styles.ValiContainer}>
                <div className={styles.ValiOtsikko}>
                    <h3>{i18n.translate('TOIMIJA')}</h3>
                    <h1>{i18n.translate('UUDEN_TOIMIJAN_LISAAMINEN')}</h1>
                </div>
            </div>
            <div className={styles.PaaOsio}>
                <Accordion
                    preExpanded={lomakeAvoinna}
                    handlePreExpanded={setLomakeAvoinna}
                    handleChange={(event) => {
                        console.log('accordionevent', event);
                        //setLomakeAvoinna(avoinnaIndex[0] + 1)
                    }}
                    lomakkeet={[
                        <PerustietoLomake
                            handleJatka={() => setLomakeAvoinna(1)}
                            handleOnChange={handleOnChange}
                            organisaatioTyypit={organisaatioTyypit}
                            organisaatio={organisaatio}
                            maatJaValtiot={maatJaValtiot}
                            opetuskielet={oppilaitoksenOpetuskielet}
                        />,
                        <YhteystietoLomake handleOnChange={handleOnChange} yhteystiedot={organisaatio.yhteystiedot} />,
                    ]}
                    otsikot={['Perustiedot', 'Yhteystiedot']} // TODO kriisisähköposti?
                />
            </div>
            <div className={styles.AlaBanneri}>
                <div>
                    <Button variant="outlined" className={styles.Versionappula} onClick={handleCancel}>
                        {i18n.translate('SULJE_TIEDOT')}
                    </Button>
                    <Button className={styles.Versionappula} onClick={postOrganisaatio}>
                        {i18n.translate('TALLENNA')}
                    </Button>
                </div>
            </div>
        </PohjaSivu>
    );
};

export default UusiToimijaLomake;
