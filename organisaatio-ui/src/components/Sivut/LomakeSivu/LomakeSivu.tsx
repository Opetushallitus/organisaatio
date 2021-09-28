import * as React from 'react';
import { FormEvent, useContext, useEffect, useState } from 'react';
import styles from './LomakeSivu.module.css';
import PohjaSivu from '../PohjaSivu/PohjaSivu';
import Accordion from '../../Accordion/Accordion';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';

import homeIcon from '@iconify/icons-fa-solid/home';

import {KoodistoContext, LanguageContext, rakenne, ROOT_OID } from '../../../contexts/contexts';
import {
    KoodiUri,
    Nimi,
    Organisaatio,
    OrganisaatioNimiJaOid,
    SiirraOrganisaatioon,
    YhdistaOrganisaatioon,
    Yhteystiedot,
    YhteystiedotOsoite,
    YhteystiedotPhone,
    YtjOrganisaatio,
} from '../../../types/types';
import PerustietoLomake from './Koulutustoimija/PerustietoLomake/PerustietoLomake';
import YhteystietoLomake from './Koulutustoimija/YhteystietoLomake/YhteystietoLomake';
import NimiHistoriaLomake from './Koulutustoimija/NimiHistoriaLomake/NimiHistoriaLomake';
import OrganisaatioHistoriaLomake from './Koulutustoimija/OrganisaatioHistoriaLomake/OrganisaatioHistoriaLomake';
import Icon from '@iconify/react';
import { Link } from 'react-router-dom';
import useKoodisto from '../../../api/koodisto';
import {
    mergeOrganisaatio,
    readOrganisaatio,
    updateOrganisaatio,
    useOrganisaatioHistoria,
} from '../../../api/organisaatio';
import { YhdistaOrganisaatio } from '../../Modaalit/ToimipisteenYhdistys/YhdistaOrganisaatio';
import { SiirraOrganisaatio } from '../../Modaalit/ToimipisteenYhdistys/SiirraOrganisaatio';
import { resolveOrganisaatio, resolveOrganisaatioTyypit } from '../../../tools/organisaatio';
import { useForm } from 'react-hook-form';
import { joiResolver } from '@hookform/resolvers/joi';
import Joi from 'joi';

type LomakeSivuProps = {
    match: { params: { oid: string } };
    history: string[];
};

const PERUSTIEDOTUUID = 'perustietolomake';
const YHTEYSTIEDOTUUID = 'yhteystietolomake';

export const PerustietoLomakeSchema = Joi.object({
    nimiEn: Joi.string(),
    nimiFi: Joi.string(),
    nimiSv: Joi.string(),
    ytunnus: Joi.string(),
    alkuPvm: Joi.string().required(),
    tyypit: Joi.array().min(1).required(),
    kotipaikkaUri: Joi.object({ label: Joi.string().required(), value: Joi.string().required() }).required(),
    muutKotipaikatUris: Joi.array(),
    maaUri: Joi.object({ label: Joi.string().required(), value: Joi.string().required() }).required(),
    kieletUris: Joi.array().min(1).required(),
});

export const yhteystietoLomakeSchema = Joi.object({
    nimiEn: Joi.string(),
    nimiFi: Joi.string(),
    nimiSv: Joi.string(),
    ytunnus: Joi.string(),
    alkuPvm: Joi.string().required(),
    tyypit: Joi.array().min(1).required(),
    kotipaikkaUri: Joi.object({ label: Joi.string().required(), value: Joi.string().required() }).required(),
    muutKotipaikatUris: Joi.array(),
    maaUri: Joi.object({ label: Joi.string().required(), value: Joi.string().required() }).required(),
    kieletUris: Joi.array().min(1).required(),
});


const LomakeSivu = ({ match: { params }, history }: LomakeSivuProps) => {
    const { i18n, language } = useContext(LanguageContext);
    const [yhdistaOrganisaatioModaaliAuki, setYhdistaOrganisaatioModaaliAuki] = useState<boolean>(false);
    const [siirraOrganisaatioModaaliAuki, setSiirraOrganisaatioModaaliAuki] = useState<boolean>(false);
    const initialYhdista = {
        merge: true,
        date: new Date(),
        newParent: undefined,
    };
    const initialSiirra = {
        merge: false,
        date: new Date(),
        newParent: undefined,
    };
    const [yhdistaOrganisaatio, setYhdistaOrganisaatio] = useState<YhdistaOrganisaatioon>(initialYhdista);
    const [siirraOrganisaatio, setSiirraOrganisaatio] = useState<SiirraOrganisaatioon>(initialSiirra);
    const {
        data: organisaatioTyypit,
        loading: organisaatioTyypitLoading,
        error: organisaatioTyypitError,
    } = useKoodisto('ORGANISAATIOTYYPPI');

    const { postinumerotKoodisto } = useContext(KoodistoContext);
    const postinumerot = postinumerotKoodisto.koodit();
    const [organisaatio, setOrganisaatio] = useState<Organisaatio | undefined>(undefined);
    const [parentOrganisaatio, setParentOrganisaatio] = useState<Organisaatio | undefined>(undefined);
    const [organisaatioNimiPolku, setOrganisaatioNimiPolku] = useState<OrganisaatioNimiJaOid[]>([]);
    useEffect(() => {
        (async function () {
            const o = await readOrganisaatio(params.oid);
            if (o) {
                setOrganisaatioNimiPolku(o.polku);
                setOrganisaatio(Object.assign({}, o.organisaatio));
                if (o.organisaatio.oid === ROOT_OID) {
                    setParentOrganisaatio(Object.assign({}, o.organisaatio));
                } else {
                    const parent = await readOrganisaatio(o.organisaatio.parentOid);
                    setParentOrganisaatio(Object.assign({}, parent.organisaatio));
                }
            }
        })();
    }, [params.oid]);
    const { historia, historiaLoading, historiaError, executeHistoria } = useOrganisaatioHistoria(params.oid);
    const handleLisaaUusiToimija = () => {
        return history.push(`/lomake/uusi?parentOid=${organisaatio ? organisaatio.oid : ROOT_OID}`);
    };

    async function handleOrganisationMerge(props: SiirraOrganisaatioon | YhdistaOrganisaatioon) {
        if (organisaatio?.oid) {
            const mergeOrganisaatioResult = await mergeOrganisaatio({
                oid: organisaatio.oid,
                ...props,
            });
            if (mergeOrganisaatioResult) {
                const organisaatioAfterMerge = await readOrganisaatio(params.oid);
                if (organisaatioAfterMerge) {
                    setOrganisaatioNimiPolku(organisaatioAfterMerge.polku);
                    setOrganisaatio(Object.assign({}, organisaatioAfterMerge.organisaatio));
                    executeHistoria();
                }
            }
        }
    }

    async function handleSiirraOrganisaatio(props: SiirraOrganisaatioon) {
        setSiirraOrganisaatioModaaliAuki(false);
        setSiirraOrganisaatio(initialSiirra);
        await handleOrganisationMerge(props);
    }
    async function cancelSiirraOrganisaatio() {
        setSiirraOrganisaatioModaaliAuki(false);
        setSiirraOrganisaatio(initialSiirra);
    }
    async function handleYhdistaOrganisaatio(props: YhdistaOrganisaatioon) {
        setYhdistaOrganisaatioModaaliAuki(false);
        setYhdistaOrganisaatio(initialYhdista);
        await handleOrganisationMerge(props);
    }
    async function cancelYhdistaOrganisaatio() {
        setYhdistaOrganisaatioModaaliAuki(false);
        setYhdistaOrganisaatio(initialYhdista);
    }

    async function putOrganisaatio() {
        if (organisaatio) {
            const data = await updateOrganisaatio(organisaatio);
            if (data) {
                setOrganisaatio(data);
                history.push(`/lomake/${organisaatio.oid}`);
            }
        }
    }

    // TODO täytyy tarkastaa mitä kaikkea tietoa tuolta Ytj:ltä tuleekaan? esim yrityksen lopetuksesta.
    const setYtjDataFetched = (ytjOrganisaatio: YtjOrganisaatio) => {
        const {
            nimi,
            postiOsoite,
            kayntiOsoite,
            yritysmuoto,
            yritysTunnus: { alkupvm, ytunnus },
        } = ytjOrganisaatio;
        const alkuPvm = alkupvm.split('.');
        [alkuPvm[0], alkuPvm[2]] = [alkuPvm[2], alkuPvm[0]]; // reverse date to YYYY-MM-DD format
        organisaatio &&
            organisaatio.yhteystiedot &&
            organisaatio.yhteystiedot
                .filter((yT: Yhteystiedot) => yT.kieli === 'kieli_fi#1')
                .forEach((yT: Yhteystiedot) => {
                    if (
                        (yT as YhteystiedotOsoite).osoiteTyyppi &&
                        (yT as YhteystiedotOsoite).osoiteTyyppi === 'posti'
                    ) {
                        const { katu: osoite, postinumero, toimipaikka: postitoimipaikka } = postiOsoite;
                        const postinumeroKoodi = postinumerotKoodisto.koodit().find((p) => p.arvo === postinumero);
                        yT = Object.assign(yT, {
                            osoite,
                            postinumeroUri: (postinumeroKoodi && postinumeroKoodi.uri) || '',
                            postitoimipaikka,
                        });
                    } else if (
                        (yT as YhteystiedotOsoite).osoiteTyyppi &&
                        (yT as YhteystiedotOsoite).osoiteTyyppi === 'kaynti'
                    ) {
                        const { katu: osoite, postinumero, toimipaikka: postitoimipaikka } = kayntiOsoite;
                        const postinumeroKoodi = postinumerot.find((p) => p.arvo === postinumero);
                        yT = Object.assign(yT, {
                            osoite,
                            postinumeroUri: (postinumeroKoodi && postinumeroKoodi.uri) || '',
                            postitoimipaikka,
                        });
                    } else if ((yT as YhteystiedotPhone).tyyppi && (yT as YhteystiedotPhone).tyyppi === 'puhelin') {
                        (yT as YhteystiedotPhone).numero = ytjOrganisaatio.puhelin;
                    }
                });
        setOrganisaatio(
            Object.assign({}, organisaatio, { nimi: { fi: nimi }, alkuPvm: alkuPvm.join('-'), ytunnus, yritysmuoto })
        ); // TODO nimet?
    };

    const handleOnChange = ({
        name,
        value,
    }: {
        name: keyof Organisaatio;
        value: { nimi: Nimi; alkuPvm: string }[] | Nimi | KoodiUri[] | Date | KoodiUri | Yhteystiedot[];
    }) => {
        setOrganisaatio((organisaatio) => {
            return Object.assign({}, organisaatio, { [name]: value });
        });
    };
    const organisaatioRakenne = resolveOrganisaatio(rakenne, organisaatio);
    const resolvedTyypit = resolveOrganisaatioTyypit(rakenne, organisaatioTyypit, parentOrganisaatio);
    const {
        register: registerPerustiedot,
        formState: { errors: perustiedotValidationErrors },
        handleSubmit: perustiedotHandleSubmit,
        control: perustiedotControl,
    } = useForm({ resolver: joiResolver(PerustietoLomakeSchema) });

    const {
        register: yhteystiedotRegister,
        formState: { errors: yhteystiedotValidationErrors },
        handleSubmit: yhteystiedotHandleSubmit,
        control: yhteystiedotControl,
    } = useForm({ resolver: joiResolver(PerustietoLomakeSchema) });

    const [lomakeAvoinna, setLomakeAvoinna] = useState<string>(PERUSTIEDOTUUID);

    const validateChanges = (accordionUuids: string[]): void => {
        const accordionuuid = accordionUuids[0];
        const setAvoinnaCb = () => setLomakeAvoinna(accordionuuid);
        if (lomakeAvoinna === PERUSTIEDOTUUID) {
            perustiedotHandleSubmit(setAvoinnaCb)();
        } else if (lomakeAvoinna === YHTEYSTIEDOTUUID) {
            yhteystiedotHandleSubmit(setAvoinnaCb)();
        }
    };
    const organisaatioRakenne = resolveOrganisaatio(rakenne, organisaatio);
    const resolvedTyypit = resolveOrganisaatioTyypit(rakenne, organisaatioTyypit, parentOrganisaatio);
    function isLoading() {
        return (
            historiaLoading ||
            historiaError ||
            organisaatioTyypitLoading ||
            organisaatioTyypitError
        );
    }

    if (!organisaatioRakenne || !resolvedTyypit || !organisaatio || isLoading()) {
        return (
            <div className={styles.PaaOsio}>
                <Spin>{i18n.translate('LABEL_PAGE_LOADING')}</Spin>
            </div>
        );
    }

    const accordionProps = () => {
        const lomakkeet = [] as React.ReactElement[];
        const otsikot = [] as string[];
        lomakkeet.push(
            <PerustietoLomake
                formControl={perustiedotControl}
                validationErrors={perustiedotValidationErrors}
                formRegister={registerPerustiedot}
                key={PERUSTIEDOTUUID}
                setYtjDataFetched={setYtjDataFetched}
                handleOnChange={handleOnChange}
                organisaatioTyypit={resolvedTyypit}
                organisaatio={organisaatio}
                language={language}
            />
        );
        otsikot.push(i18n.translate('LOMAKE_PERUSTIEDOT'));
        if (organisaatio.yhteystiedot) {
            lomakkeet.push(
                <YhteystietoLomake
                    formControl={yhteystiedotControl}
                    validationErrors={yhteystiedotValidationErrors}
                    formRegister={yhteystiedotRegister}
                    key={YHTEYSTIEDOTUUID}
                    handleOnChange={handleOnChange}
                    yhteystiedot={organisaatio.yhteystiedot}
                />
            );
            otsikot.push(i18n.translate('LOMAKE_YHTEYSTIEDOT'));
        }
        lomakkeet.push(<NimiHistoriaLomake key={'nimihistorialomake'} nimet={organisaatio.nimet} />);
        otsikot.push(i18n.translate('LOMAKE_NIMIHISTORIA'));

        if (organisaatio.oid !== ROOT_OID && organisaatio.oid) {
            lomakkeet.push(<OrganisaatioHistoriaLomake key={'organisaatiohistorialomake'} historia={historia} />);
            otsikot.push(i18n.translate('LOMAKE_RAKENNE'));
        }

        return {
            lomakkeet: lomakkeet,
            otsikot: otsikot,
            //handleItemChange: (event: FormEvent<HTMLDivElement>) => console.log('piip', event),
            handleUuidChange: validateChanges,
            //handlePreExpanded: setLomakeAvoinna,
            preExpanded: lomakeAvoinna,
        };
    };
    const thisTyyppi = organisaatioTyypit.find((a) => a.uri === organisaatio.tyypit[0]);
    return (
        <PohjaSivu>
            <div className={styles.YlaBanneri}>
                <div>
                    <Link to="/">
                        <Icon icon={homeIcon} />
                    </Link>
                </div>
                {organisaatioNimiPolku.map((o, index) => [
                    <div key={o.oid}>
                        <Link to={`${o.oid}`}>{o.nimi[language] || o.nimi['fi'] || o.nimi['sv'] || o.nimi['en']}</Link>
                    </div>,
                    organisaatioNimiPolku.length - 1 !== index && <div> &gt; </div>,
                ])}
            </div>
            <div className={styles.ValiContainer}>
                <div className={styles.ValiOtsikko}>
                    <h3>
                        {organisaatio.tyypit
                            ? `${thisTyyppi ? thisTyyppi.nimi[language] : ''} (${organisaatio.tyypit[0]})`
                            : i18n.translate('LABEL_NOT_AVAILABLE')}
                    </h3>
                    <h1 className={organisaatio.status === 'AKTIIVINEN' ? '' : styles.Passivoitu}>
                        {organisaatio.nimi[language] ||
                            organisaatio.nimi['fi'] ||
                            organisaatio.nimi['sv'] ||
                            organisaatio.nimi['en']}
                    </h1>
                </div>
                <div className={styles.ValiNappulat}>
                    {organisaatioRakenne.moveTargetType.length > 0 && (
                        <Button
                            onClick={() => {
                                setSiirraOrganisaatio({ ...siirraOrganisaatio });
                                setSiirraOrganisaatioModaaliAuki(true);
                            }}
                        >
                            {i18n.translate('LOMAKE_SIIRRA_ORGANISAATIO')}
                        </Button>
                    )}
                    {organisaatioRakenne.mergeTargetType.length > 0 && (
                        <Button
                            onClick={() => {
                                setYhdistaOrganisaatio({ ...yhdistaOrganisaatio });
                                setYhdistaOrganisaatioModaaliAuki(true);
                            }}
                        >
                            {i18n.translate('LOMAKE_YHDISTA_ORGANISAATIO')}
                        </Button>
                    )}
                    <Button onClick={handleLisaaUusiToimija}>{i18n.translate('LOMAKE_LISAA_UUSI_TOIMIJA')}</Button>
                </div>
            </div>
            <div className={styles.PaaOsio}>
                {/*<YhdistysJaSiirto />*/}
                <Accordion {...accordionProps()} />
            </div>
            <div className={styles.AlaBanneri}>
                <div className={styles.VersioContainer}>
                    <div className={styles.MuokattuKolumni}>
                        <span>{i18n.translate('VERSIOHISTORIA_MUOKATTU_VIIMEKSI')}</span>
                        <span>01.01.2020 16:39 ngo Schimpff</span>
                    </div>
                </div>
                <div>
                    <Button variant="outlined" className={styles.Versionappula} onClick={() => history.push('/')}>
                        {i18n.translate('BUTTON_SULJE')}
                    </Button>
                    <Button className={styles.Versionappula} onClick={putOrganisaatio}>
                        {i18n.translate('BUTTON_TALLENNA')}
                    </Button>
                </div>
            </div>
            {yhdistaOrganisaatioModaaliAuki && (
                <YhdistaOrganisaatio
                    yhdistaOrganisaatio={yhdistaOrganisaatio}
                    organisaatio={organisaatio}
                    handleChange={setYhdistaOrganisaatio}
                    organisaatioRakenne={organisaatioRakenne}
                    tallennaCallback={() => {
                        handleYhdistaOrganisaatio({ ...yhdistaOrganisaatio });
                    }}
                    peruutaCallback={() => {
                        cancelYhdistaOrganisaatio();
                    }}
                    suljeCallback={() => cancelYhdistaOrganisaatio()}
                />
            )}
            {siirraOrganisaatioModaaliAuki && (
                <SiirraOrganisaatio
                    siirraOrganisaatio={siirraOrganisaatio}
                    organisaatio={organisaatio}
                    handleChange={setSiirraOrganisaatio}
                    organisaatioRakenne={organisaatioRakenne}
                    tallennaCallback={() => {
                        handleSiirraOrganisaatio({ ...siirraOrganisaatio });
                    }}
                    peruutaCallback={() => {
                        cancelSiirraOrganisaatio();
                    }}
                    suljeCallback={() => cancelSiirraOrganisaatio()}
                />
            )}
        </PohjaSivu>
    );
};

export default LomakeSivu;
