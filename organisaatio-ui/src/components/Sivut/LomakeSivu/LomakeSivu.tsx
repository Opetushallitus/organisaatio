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
    OrganisaatioNimiJaOid,
    OrganisaationNimetNimi,
    ParentTiedot,
    Perustiedot,
    ResolvedRakenne,
    SiirraOrganisaatioon,
    UiOrganisaatioBase,
    YhdistaOrganisaatioon,
    Yhteystiedot,
} from '../../../types/types';

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
import {mapApiYhteystiedotToUi, mapUiOrganisaatioToApiToUpdate, mapUiYhteystiedotToApi} from '../../../tools/mappers';
import PerustietolomakeSchema from '../../../ValidationSchemas/PerustietolomakeSchema';
import YhteystietoLomakeSchema from '../../../ValidationSchemas/YhteystietoLomakeSchema';
import { YhdistaOrganisaatio } from '../../Modaalit/ToimipisteenYhdistys/YhdistaOrganisaatio';
import { SiirraOrganisaatio } from '../../Modaalit/ToimipisteenYhdistys/SiirraOrganisaatio';
import { resolveOrganisaatio, resolveOrganisaatioTyypit } from '../../../tools/organisaatio';
import YTJModaali from '../../Modaalit/YTJModaali/YTJModaali';
import { ApiOrganisaatio } from '../../../types/apiTypes';

type LomakeSivuProps = {
    match: { params: { oid: string } };
    history: string[];
};

const PERUSTIEDOTID = 'perustietolomake';
const YHTEYSTIEDOTID = 'yhteystietolomake';

const LomakeSivu = ({ match: { params }, history }: LomakeSivuProps) => {
    const { i18n, language } = useContext(LanguageContext);
    const [YTJModaaliAuki, setYTJModaaliAuki] = useState<boolean>(false);
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
    const [organisaatioBase, setOrganisaatioBase] = useState<UiOrganisaatioBase | undefined>(undefined);
    const [parentTiedot, setParentTiedot] = useState<ParentTiedot>({ organisaatioTyypit: [], oid: ROOT_OID });
    const {
        organisaatioTyypitKoodisto,
        maatJaValtiotKoodisto,
        oppilaitoksenOpetuskieletKoodisto,
        kuntaKoodisto,
    } = useContext(KoodistoContext);
    const [organisaatioNimiPolku, setOrganisaatioNimiPolku] = useState<OrganisaatioNimiJaOid[]>([]);
    const [resolvedOrganisaatioRakenne, setResolvedOrganisaatioRakenne] = useState<ResolvedRakenne>(
        resolveOrganisaatio(rakenne, { organisaatioTyypit: [], oid: '' })
    );
    const {
        reset: perustiedotReset,
        setValue: setPerustiedotValue,
        getValues: getPerustiedotValues,
        register: registerPerustiedot,
        formState: { errors: perustiedotValidationErrors },
        handleSubmit: perustiedotHandleSubmit,
        control: perustiedotControl,
    } = useForm<Perustiedot>({ resolver: joiResolver(PerustietolomakeSchema) });
    const {
        reset: yhteystiedotReset,
        watch: watchYhteystiedot,
        setValue: setYhteystiedotValue,
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
                await resetOrganisaatio(organisaatio, polku);
            }
        })();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [params.oid]);
    const { historia, historiaLoading, historiaError, executeHistoria } = useOrganisaatioHistoria(params.oid);
    const handleLisaaUusiToimija = () => {
        return history.push(`/lomake/uusi?parentOid=${organisaatioBase ? organisaatioBase.oid : ROOT_OID}`);
    };

    const mapOrganisaatioToUi = ({
        nimi,
        maaUri,
        kieletUris,
        kotipaikkaUri,
        muutKotipaikatUris,
        alkuPvm,
        tyypit,
        yhteystiedot: apiYhteystiedot,
        ...rest
    }: ApiOrganisaatio): {
        Uiperustiedot: Perustiedot;
        UibaseTiedot: UiOrganisaatioBase;
        Uiyhteystiedot: Yhteystiedot;
    } => {
        const maa = maatJaValtiotKoodisto.uri2SelectOption(maaUri);
        const kotipaikka = kuntaKoodisto.uri2SelectOption(kotipaikkaUri);
        const kielet = kieletUris.map((kieliUri) => oppilaitoksenOpetuskieletKoodisto.uri2SelectOption(kieliUri));
        const muutKotipaikat =
            muutKotipaikatUris?.map((muuKotipaikkaUri) => kuntaKoodisto.uri2SelectOption(muuKotipaikkaUri)) || [];
        return {
            Uiperustiedot: { nimi, maa, kielet, kotipaikka, muutKotipaikat, alkuPvm, organisaatioTyypit: tyypit },
            UibaseTiedot: { ...rest, apiYhteystiedot, currentNimi: nimi },
            Uiyhteystiedot: mapApiYhteystiedotToUi(apiYhteystiedot),
        };
    };

    async function resetOrganisaatio(organisaatio, polku) {
        resolveOrganisaatioRakenne(organisaatio.tyypit, organisaatio.oid);
        const { Uiyhteystiedot, UibaseTiedot, Uiperustiedot } = mapOrganisaatioToUi(organisaatio);
        setOrganisaatioNimiPolku(polku);
        setOrganisaatioBase(UibaseTiedot);
        const {
            organisaatio: { tyypit: organisaatioTyypit, oid },
        } = await readOrganisaatio(organisaatio.parentOid || ROOT_OID);
        const parentTiedot = { organisaatioTyypit, oid };
        setParentTiedot(parentTiedot);
        perustiedotReset(Uiperustiedot);
        yhteystiedotReset(Uiyhteystiedot);
    }

    function resolveOrganisaatioRakenne(organisaatioTyypit, oid) {
        const organisaatioRakenne = resolveOrganisaatio(rakenne, {
            organisaatioTyypit,
            oid,
        });
        setResolvedOrganisaatioRakenne(organisaatioRakenne);
    }

    async function handleOrganisationMerge(props: SiirraOrganisaatioon | YhdistaOrganisaatioon) {
        if (organisaatioBase?.oid) {
            const mergeOrganisaatioResult = await mergeOrganisaatio({
                oid: organisaatioBase.oid,
                ...props,
            });
            if (mergeOrganisaatioResult) {
                const organisaatioAfterMerge = await readOrganisaatio(params.oid);
                if (organisaatioAfterMerge) {
                    await resetOrganisaatio(organisaatioAfterMerge.organisaatio, organisaatioAfterMerge.polku);
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

    function saveOrganisaatio() {
        if (organisaatioBase) {
            perustiedotHandleSubmit((perustiedotFormValues) => {
                yhteystiedotHandleSubmit(async (yhteystiedotFormValues) => {
                    const organisaatioToBeUpdated = mapUiOrganisaatioToApiToUpdate(organisaatioBase, yhteystiedotFormValues, perustiedotFormValues)
                    const updatedOrganisaatio = await updateOrganisaatio(organisaatioToBeUpdated);
                    if (updatedOrganisaatio) {
                        await resetOrganisaatio(updatedOrganisaatio, organisaatioNimiPolku);
                    }
                })();
            })();
        }
    }

    if (!organisaatioBase || historiaLoading || historiaError) {
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
    const { nimi, ytunnus, organisaatioTyypit } = getPerustiedotValues();
    const resolvedTyypit = resolveOrganisaatioTyypit(rakenne, organisaatioTyypitKoodisto, parentTiedot);
    const accordionProps = () => {
        const lomakkeet = [] as React.ReactElement[];
        const otsikot = [] as string[];
        lomakkeet.push(
            <PerustietoLomake
                resolvedTyypit={resolvedTyypit}
                getPerustiedotValues={getPerustiedotValues}
                formRegister={registerPerustiedot}
                handleNimiUpdate={handleNimiUpdate}
                formControl={perustiedotControl}
                validationErrors={perustiedotValidationErrors}
                key={PERUSTIEDOTID}
                organisaatioBase={organisaatioBase}
                rakenne={resolvedOrganisaatioRakenne}
                language={language}
                openYtjModal={() => setYTJModaaliAuki(true)}
            />
        );
        otsikot.push(i18n.translate('LOMAKE_PERUSTIEDOT'));
        lomakkeet.push(
            <YhteystietoLomake
                watch={watchYhteystiedot}
                setYhteystiedotValue={setYhteystiedotValue}
                formControl={yhteystiedotControl}
                validationErrors={yhteystiedotValidationErrors}
                formRegister={yhteystiedotRegister}
                key={YHTEYSTIEDOTID}
            />
        );
        otsikot.push(i18n.translate('LOMAKE_YHTEYSTIEDOT'));
        lomakkeet.push(<NimiHistoriaLomake key={'nimihistorialomake'} nimet={organisaatioBase?.nimet} />);
        otsikot.push(i18n.translate('LOMAKE_NIMIHISTORIA'));
        if (organisaatioBase?.oid !== ROOT_OID && organisaatioBase?.oid) {
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
                    <h3>{(organisaatioTyypit && organisaatioTyypit[0]) || i18n.translate('LABEL_NOT_AVAILABLE')}</h3>
                    <h1 className={organisaatioBase?.status === 'AKTIIVINEN' ? '' : styles.Passivoitu}>
                        {nimi && (nimi[language] || nimi['fi'] || nimi['sv'] || nimi['en'])}
                    </h1>
                </div>
                <div className={styles.ValiNappulat}>
                    {resolvedOrganisaatioRakenne?.moveTargetType.length > 0 && (
                        <Button
                            onClick={() => {
                                setSiirraOrganisaatio({ ...siirraOrganisaatio });
                                setSiirraOrganisaatioModaaliAuki(true);
                            }}
                        >
                            {i18n.translate('LOMAKE_SIIRRA_ORGANISAATIO')}
                        </Button>
                    )}
                    {resolvedOrganisaatioRakenne?.mergeTargetType.length > 0 && (
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
                    organisaatioBase={organisaatioBase}
                    handleChange={setYhdistaOrganisaatio}
                    organisaatioRakenne={resolvedOrganisaatioRakenne}
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
                    organisaatioBase={organisaatioBase}
                    handleChange={setSiirraOrganisaatio}
                    organisaatioRakenne={resolvedOrganisaatioRakenne}
                    tallennaCallback={() => {
                        handleSiirraOrganisaatio({ ...siirraOrganisaatio });
                    }}
                    peruutaCallback={() => {
                        cancelSiirraOrganisaatio();
                    }}
                    suljeCallback={() => cancelSiirraOrganisaatio()}
                />
            )}
            {YTJModaaliAuki && (
                <YTJModaali
                    setters={{ setPerustiedotValue, setYhteystiedotValue }}
                    ytunnus={ytunnus || ''}
                    suljeModaali={() => setYTJModaaliAuki(false)}
                />
            )}
        </PohjaSivu>
    );
};
export default LomakeSivu;
