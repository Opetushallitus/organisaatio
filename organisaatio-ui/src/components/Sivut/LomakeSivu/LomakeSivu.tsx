import * as React from 'react';
import { useContext, useEffect, useState } from 'react';
import styles from './LomakeSivu.module.css';
import PohjaSivu from '../PohjaSivu/PohjaSivu';
import Accordion from '../../Accordion/Accordion';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';
import homeIcon from '@iconify/icons-fa-solid/home';
import { KoodistoContext, LanguageContext, rakenne, ROOT_OID } from '../../../contexts/contexts';
import {
    Organisaatio,
    OrganisaatioNimiJaOid,
    OrganisaationNimetNimi,
    Perustiedot,
    SiirraOrganisaatioon,
    YhdistaOrganisaatioon,
    Yhteystiedot,
} from '../../../types/types';
import { YtjOrganisaatio } from '../../../types/apiTypes';
import PerustietoLomake from './Koulutustoimija/PerustietoLomake/PerustietoLomake';
import YhteystietoLomake from './Koulutustoimija/YhteystietoLomake/YhteystietoLomake';
import NimiHistoriaLomake from './Koulutustoimija/NimiHistoriaLomake/NimiHistoriaLomake';
import OrganisaatioHistoriaLomake from './Koulutustoimija/OrganisaatioHistoriaLomake/OrganisaatioHistoriaLomake';
import Icon from '@iconify/react';
import { Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { joiResolver } from '@hookform/resolvers/joi';
import {
    mergeOrganisaatio,
    readOrganisaatio,
    updateOrganisaatio,
    useOrganisaatioHistoria,
} from '../../../api/organisaatio';
import { mapApiYhteystiedotToUi, mapUiYhteystiedotToApi } from '../../../tools/mappers';
import PerustietolomakeSchema from '../../../ValidationSchemas/PerustietolomakeSchema';
import YhteystietoLomakeSchema from '../../../ValidationSchemas/YhteystietoLomakeSchema';
import { YhdistaOrganisaatio } from '../../Modaalit/ToimipisteenYhdistys/YhdistaOrganisaatio';
import { SiirraOrganisaatio } from '../../Modaalit/ToimipisteenYhdistys/SiirraOrganisaatio';
import { mapYtjToAPIOrganisaatio, resolveOrganisaatio, resolveOrganisaatioTyypit } from '../../../tools/organisaatio';

type LomakeSivuProps = {
    match: { params: { oid: string } };
    history: string[];
};

const PERUSTIEDOTID = 'perustietolomake';
const YHTEYSTIEDOTID = 'yhteystietolomake';

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
    const { postinumerotKoodisto } = useContext(KoodistoContext);
    const [organisaatio, setOrganisaatio] = useState<Organisaatio | undefined>(undefined);
    const [parentOrganisaatio, setParentOrganisaatio] = useState<Organisaatio | undefined>(undefined);
    const { organisaatioTyypitKoodisto } = useContext(KoodistoContext);
    const [organisaatioNimiPolku, setOrganisaatioNimiPolku] = useState<OrganisaatioNimiJaOid[]>([]);
    const {
        setValue: setPerustiedotValue,
        register: registerPerustiedot,
        formState: { errors: perustiedotValidationErrors },
        handleSubmit: perustiedotHandleSubmit,
        control: perustiedotControl,
    } = useForm<Perustiedot>({ resolver: joiResolver(PerustietolomakeSchema) });
    const {
        reset: yhteystiedotReset,
        watch: watchYhteystiedot,
        register: yhteystiedotRegister,
        formState: { errors: yhteystiedotValidationErrors },
        handleSubmit: yhteystiedotHandleSubmit,
        control: yhteystiedotControl,
    } = useForm<Yhteystiedot>({
        defaultValues: mapApiYhteystiedotToUi([]),
        resolver: joiResolver(YhteystietoLomakeSchema),
    });

    useEffect(() => {
        (async function () {
            const { organisaatio, polku } = await readOrganisaatio(params.oid);
            if (organisaatio && polku) {
                setOrganisaatioNimiPolku(polku);
                setOrganisaatio(Object.assign({}, organisaatio));
                yhteystiedotReset(mapApiYhteystiedotToUi(organisaatio.yhteystiedot || []));
                if (organisaatio.oid === ROOT_OID) {
                    setParentOrganisaatio(Object.assign({}, organisaatio));
                } else {
                    const parent = await readOrganisaatio(organisaatio.parentOid);
                    setParentOrganisaatio(Object.assign({}, parent.organisaatio));
                }
            }
        })();
    }, [params.oid, yhteystiedotReset]);
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

    // TODO täytyy tarkastaa mitä kaikkea tietoa tuolta Ytj:ltä tuleekaan? esim yrityksen lopetuksesta.
    const setYtjDataFetched = (ytjOrganisaatio: YtjOrganisaatio) => {
        const newOganisaatio = mapYtjToAPIOrganisaatio({ ytjOrganisaatio, organisaatio, postinumerotKoodisto });
        setOrganisaatio(newOganisaatio);
    };

    const [lomakeAvoinna, setLomakeAvoinna] = useState<string>(PERUSTIEDOTID);

    const validateChanges = (accordionUuids: string[]): void => {
        const accordionuuid = accordionUuids[0];
        const setAvoinnaCb = () => {
            setLomakeAvoinna(accordionuuid);
        };
        switch (lomakeAvoinna) {
            case PERUSTIEDOTID:
                perustiedotHandleSubmit(setAvoinnaCb)();
                break;
            case YHTEYSTIEDOTID:
                yhteystiedotHandleSubmit(setAvoinnaCb)();
                break;
            default:
                return setAvoinnaCb();
        }
    };

    const organisaatioRakenne = resolveOrganisaatio(rakenne, organisaatio);
    const resolvedTyypit = resolveOrganisaatioTyypit(rakenne, organisaatioTyypitKoodisto, parentOrganisaatio);

    function saveOrganisaatio() {
        if (organisaatio) {
            perustiedotHandleSubmit((perustiedotFormValues) => {
                yhteystiedotHandleSubmit(async (yhteystiedotFormValues) => {
                    const yhteystiedot = mapUiYhteystiedotToApi(organisaatio.yhteystiedot, yhteystiedotFormValues);
                    const { kotipaikkaUri, maaUri, kieletUris } = perustiedotFormValues;
                    const today = new Date().toISOString().split('T')[0];
                    const nimet = organisaatio.nimet;
                    const uusiNimi = { ...perustiedotFormValues.nimi };
                    const sameDayNimiIdx = organisaatio.nimet.findIndex(
                        (nimi: OrganisaationNimetNimi) => nimi.alkuPvm && today === nimi.alkuPvm
                    );
                    if (sameDayNimiIdx > -1) {
                        nimet[sameDayNimiIdx].nimi = uusiNimi;
                    } else {
                        nimet.push({ nimi: uusiNimi });
                    }
                    const orgToBeUpdated = {
                        ...organisaatio,
                        ...{
                            ...perustiedotFormValues,
                            muutKotipaikatUris: perustiedotFormValues.muutKotipaikatUris.map((a) => a.value),
                            kotipaikkaUri: kotipaikkaUri?.value,
                            maaUri: maaUri?.value,
                            kieletUris: kieletUris.map((a) => a.value),
                        },
                        yhteystiedot,
                        nimet,
                    };
                    const updatedOrganisaatio = await updateOrganisaatio(orgToBeUpdated);
                    if (updatedOrganisaatio) {
                        setOrganisaatio(updatedOrganisaatio);
                        history.push(`/lomake/${organisaatio.oid}`);
                    }
                })();
            })();
        }
    }

    if (!organisaatioRakenne || !resolvedTyypit || !organisaatio || historiaLoading || historiaError) {
        return (
            <div className={styles.PaaOsio}>
                <Spin>{i18n.translate('LABEL_PAGE_LOADING')}</Spin>
            </div>
        );
    }

    const handleNimiUpdate = (nimi) => {
        setPerustiedotValue('nimi', nimi);
    };

    registerPerustiedot('nimi');
    handleNimiUpdate(organisaatio.nimi);

    const accordionProps = () => {
        const lomakkeet = [] as React.ReactElement[];
        const otsikot = [] as string[];
        lomakkeet.push(
            <PerustietoLomake
                formRegister={registerPerustiedot}
                handleNimiUpdate={handleNimiUpdate}
                formControl={perustiedotControl}
                validationErrors={perustiedotValidationErrors}
                key={PERUSTIEDOTID}
                setYtjDataFetched={setYtjDataFetched}
                organisaatioTyypit={resolvedTyypit}
                rakenne={organisaatioRakenne}
                organisaatio={organisaatio}
                language={language}
            />
        );
        otsikot.push(i18n.translate('LOMAKE_PERUSTIEDOT'));
        if (organisaatio.yhteystiedot) {
            lomakkeet.push(
                <YhteystietoLomake
                    watch={watchYhteystiedot}
                    formControl={yhteystiedotControl}
                    validationErrors={yhteystiedotValidationErrors}
                    formRegister={yhteystiedotRegister}
                    key={YHTEYSTIEDOTID}
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
            lomakkeet,
            otsikot,
            handleUuidChange: validateChanges,
            preExpanded: lomakeAvoinna,
        };
    };
    const thisTyyppi = organisaatioTyypitKoodisto.uri2Nimi(organisaatio.tyypit[0]);
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
                            ? `${thisTyyppi ? thisTyyppi : ''} (${organisaatio.tyypit[0]})`
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
                    <Button className={styles.Versionappula} onClick={saveOrganisaatio}>
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
