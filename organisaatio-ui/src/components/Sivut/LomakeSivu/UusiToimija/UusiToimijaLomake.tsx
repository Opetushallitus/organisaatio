import * as React from 'react';
import { useContext, useReducer, useState } from 'react';
import styles from './UusiToimijaLomake.module.css';
import PohjaSivu from '../../PohjaSivu/PohjaSivu';
import Accordion from '../../../Accordion/Accordion';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';
import queryString from 'query-string';
import homeIcon from '@iconify/icons-fa-solid/home';

import { LanguageContext, ROOT_OID } from '../../../../contexts/contexts';
import { NewOrganisaatio, Yhteystiedot } from '../../../../types/types';
import PerustietoLomake from './PerustietoLomake/PerustietoLomake';
import YhteystietoLomake from '../Koulutustoimija/YhteystietoLomake/YhteystietoLomake';
import Icon from '@iconify/react';
import { Link } from 'react-router-dom';
import useKoodisto from '../../../../api/koodisto';
import { createOrganisaatio } from '../../../../api/organisaatio';

const tyhjaOrganisaatio = (stub): NewOrganisaatio => {
    return {
        ...stub,
        ...{
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
        },
    };
};

const organisaatioReducer = function (
    state: NewOrganisaatio,
    action: { type: 'edit' | 'reset'; payload?: NewOrganisaatio }
): NewOrganisaatio {
    switch (action.type) {
        case 'reset':
            return { ...tyhjaOrganisaatio({ parentOid: ROOT_OID }) };
        default:
            return { ...state, ...action.payload };
    }
};

const UusiToimijaLomake = (props: { history: string[]; location: { search: string } }) => {
    const { i18n } = useContext(LanguageContext);
    const {
        data: organisaatioTyypit,
        loading: organisaatioTyypitLoading,
        error: organisaatioTyypitError,
    } = useKoodisto('ORGANISAATIOTYYPPI');
    const { data: maatJaValtiot, loading: maatJaValtiotLoading, error: maatJaValtiotError } = useKoodisto(
        'MAATJAVALTIOT1'
    );
    const { parentOid } = queryString.parse(props.location.search);

    const {
        data: oppilaitoksenOpetuskielet,
        loading: oppilaitoksenOpetuskieletLoading,
        error: oppilaitoksenOpetuskieletError,
    } = useKoodisto('OPPILAITOKSENOPETUSKIELI');
    const [organisaatio, setOrganisaatio] = useReducer(
        organisaatioReducer,
        { parentOid: (parentOid || ROOT_OID) as string },
        tyhjaOrganisaatio
    );
    async function postOrganisaatio() {
        const data = await createOrganisaatio(organisaatio);
        if (data) {
            setOrganisaatio({ type: 'reset' });
            props.history.push(`/lomake/${data.oid}`);
        }
    }

    function handleCancel() {
        setOrganisaatio({ type: 'reset' });
        props.history.push('/');
    }

    const [lomakeAvoinna, setLomakeAvoinna] = useState(0);

    const handleOnChange = ({ name, value }: { name: string; value: any }) => {
        setOrganisaatio({ type: 'edit', payload: { [name]: value } as NewOrganisaatio });
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
                key={'perustietolomake'}
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
                <YhteystietoLomake
                    key={'yhteystietolomake'}
                    handleOnChange={handleOnChange}
                    yhteystiedot={organisaatio.yhteystiedot}
                />
            );
            otsikot.push(i18n.translate('LOMAKE_YHTEYSTIEDOT'));
        }

        return { lomakkeet, otsikot };
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
