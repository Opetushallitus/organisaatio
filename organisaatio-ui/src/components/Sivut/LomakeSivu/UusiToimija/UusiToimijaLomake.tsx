import * as React from 'react';
import { useContext, useReducer, useState } from 'react';
import styles from './UusiToimijaLomake.module.css';
import PohjaSivu from '../../PohjaSivu/PohjaSivu';
import Accordion from '../../../Accordion/Accordion';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';

import homeIcon from '@iconify/icons-fa-solid/home';

import { LanguageContext } from '../../../../contexts/contexts';
import { Koodi, Organisaatio, Yhteystiedot } from '../../../../types/types';
import PerustietoLomake from './PerustietoLomake/PerustietoLomake';
import YhteystietoLomake from '../Koulutustoimija/YhteystietoLomake/YhteystietoLomake';
import Icon from '@iconify/react';
import useAxios from 'axios-hooks';
import Axios from 'axios';
import { Link } from 'react-router-dom';

const tyhjaOrganisaatio: Organisaatio = {
    ytunnus: '',
    nimi: {},
    status: '',
    nimet: [],
    alkuPvm: null,
    yritysmuoto: '',
    tyypit: [],
    kotipaikkaUri: '',
    muutKotipaikatUris: [],
    maaUri: '',
    kieletUris: [],
    yhteystiedot: ['kieli_fi#1', 'kieli_sv#1', 'kieli_en#1']
        .map(
            (kieli) =>
                [
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
                ] as Yhteystiedot[]
        )
        .flat(),
};

const UusiToimijaLomake = (props: { history: string[] }) => {
    const { i18n } = useContext(LanguageContext);
    const [{ data: organisaatioTyypit, loading: organisaatioTyypitLoading, error: organisaatioTyypitError }] = useAxios<
        Koodi[]
    >(`/organisaatio/koodisto/ORGANISAATIOTYYPPI/koodi`);
    const [{ data: maatJaValtiot, loading: maatJaValtiotLoading, error: maatJaValtiotError }] = useAxios<Koodi[]>(
        `/organisaatio/koodisto/MAATJAVALTIOT1/koodi`
    );
    const [
        {
            data: oppilaitoksenOpetuskielet,
            loading: oppilaitoksenOpetuskieletLoading,
            error: oppilaitoksenOpetuskieletError,
        },
    ] = useAxios<Koodi[]>(`/organisaatio/koodisto/OPPILAITOKSENOPETUSKIELI/koodi`);
    const [organisaatio, setOrganisaatio] = useReducer(
        (state: Organisaatio, newState: Organisaatio): Organisaatio => ({ ...state, ...newState }),
        tyhjaOrganisaatio
    );

    async function postOrganisaatio() {
        try {
            const response = await Axios.post(`/organisaatio/organisaatio/v4/`, organisaatio);
            console.error('saved org response', response);
            props.history.push('/');
        } catch (error) {
            console.error('error while posting org', error);
        } finally {
        }
    }

    function handleCancel() {
        setOrganisaatio({ ...tyhjaOrganisaatio });
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
    const accordionProps = () => {
        const lomakkeet = [] as React.ReactElement[];
        const otsikot = [] as string[];
        lomakkeet.push(
            <PerustietoLomake
                handleJatka={() => setLomakeAvoinna(1)}
                handleOnChange={handleOnChange}
                organisaatioTyypit={organisaatioTyypit}
                organisaatio={organisaatio}
                maatJaValtiot={maatJaValtiot}
                opetuskielet={oppilaitoksenOpetuskielet}
            />
        );
        otsikot.push(i18n.translate('LOMAKE_PERUSTIEDOT'));
        if (organisaatio.yhteystiedot) {
            lomakkeet.push(
                <YhteystietoLomake handleOnChange={handleOnChange} yhteystiedot={organisaatio.yhteystiedot} />
            );
            otsikot.push(i18n.translate('LOMAKE_YHTEYSTIEDOT'));
        }

        return { lomakkeet: lomakkeet, otsikot: otsikot };
    };

    return (
        <PohjaSivu>
            <div className={styles.YlaBanneri}>
                <div>
                    <Link to="/">
                        <Icon icon={homeIcon} />
                        {i18n.translate('UUSI_TOIMIJA_TITLE')}
                    </Link>
                </div>
            </div>
            <div className={styles.ValiContainer}>
                <div className={styles.ValiOtsikko}>
                    <h3>{i18n.translate('UUSI_TOIMIJA_TOIMIJA_TITLE')}</h3>
                    <h1>{i18n.translate('UUSI_TOIMIJA_UUDEN_TOIMIJAN_LISAAMINEN')}</h1>
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
                    {...accordionProps()}
                />
            </div>
            <div className={styles.AlaBanneri}>
                <div>
                    <Button variant="outlined" className={styles.Versionappula} onClick={handleCancel}>
                        {i18n.translate('BUTTON_SULJE')}
                    </Button>
                    <Button className={styles.Versionappula} onClick={postOrganisaatio}>
                        {i18n.translate('BUTTON_TALLENNA')}
                    </Button>
                </div>
            </div>
        </PohjaSivu>
    );
};

export default UusiToimijaLomake;
