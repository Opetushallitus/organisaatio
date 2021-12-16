import * as React from 'react';
import { useContext, useEffect, useState } from 'react';
import PohjaSivu from '../PohjaSivu/PohjaSivu';
import Accordion from '../../Accordion/Accordion';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';
import homeIcon from '@iconify/icons-fa-solid/home';
import { rakenne, ROOT_OID } from '../../../contexts/constants';
import {
    LiitaOrganisaatioon,
    OrganisaatioNimiJaOid,
    ParentTiedot,
    Perustiedot,
    ResolvedRakenne,
    UiOrganisaatioBase,
    Yhteystiedot,
    YhteystietoArvot,
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
    mapApiYhteystiedotToUi,
    mapUiOrganisaatioToApiToUpdate,
    mergeOrganisaatio,
    readOrganisaatio,
    updateOrganisaatio,
    useOrganisaatioHistoria,
} from '../../../api/organisaatio';
import PerustietolomakeSchema from '../../../ValidationSchemas/PerustietolomakeSchema';
import YhteystietoLomakeSchema from '../../../ValidationSchemas/YhteystietoLomakeSchema';
import { LiitaOrganisaatio } from '../../Modaalit/ToimipisteenYhdistys/LiitaOrganisaatio';
import { resolveOrganisaatio, resolveOrganisaatioTyypit, showCreateChildButton } from '../../../tools/organisaatio';
import YTJModaali from '../../Modaalit/YTJModaali/YTJModaali';
import { ApiOrganisaatio } from '../../../types/apiTypes';
import {
    AlaBanneri,
    LomakeButton,
    PaaOsio,
    ValiContainer,
    ValiNappulat,
    ValiOtsikko,
    VersioContainer,
    YlaBanneri,
} from './LomakeFields/LomakeFields';
import Muokattu from '../../Muokattu/Muokattu';
import { LanguageContext } from '../../../contexts/LanguageContext';
import { KoodistoContext } from '../../../contexts/KoodistoContext';
import { CasMeContext } from '../../../contexts/CasMeContext';
import ArvoLomake from './Koulutustoimija/ArvoLomake/ArvoLomake';

type LomakeSivuProps = {
    match: { params: { oid: string } };
    history: string[];
};

const PERUSTIEDOTID = 'perustietolomake';
const YHTEYSTIEDOTID = 'yhteystietolomake';

const LomakeSivu = ({ match: { params }, history }: LomakeSivuProps) => {
    const { i18n, language } = useContext(LanguageContext);
    const { me: casMe } = useContext(CasMeContext);
    const [YTJModaaliAuki, setYTJModaaliAuki] = useState<boolean>(false);
    const [yhdistaOrganisaatioModaaliAuki, setYhdistaOrganisaatioModaaliAuki] = useState<boolean>(false);
    const [siirraOrganisaatioModaaliAuki, setSiirraOrganisaatioModaaliAuki] = useState<boolean>(false);
    const [isLoading, setIsLoading] = useState<boolean>(false);
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
    const [yhdistaOrganisaatio, setYhdistaOrganisaatio] = useState<LiitaOrganisaatioon>(initialYhdista);
    const [siirraOrganisaatio, setSiirraOrganisaatio] = useState<LiitaOrganisaatioon>(initialSiirra);
    const [organisaatioBase, setOrganisaatioBase] = useState<UiOrganisaatioBase | undefined>(undefined);
    const [parentTiedot, setParentTiedot] = useState<ParentTiedot>({
        organisaatioTyypit: [],
        oid: ROOT_OID,
    });
    const {
        organisaatioTyypitKoodisto,
        maatJaValtiotKoodisto,
        oppilaitoksenOpetuskieletKoodisto,
        postinumerotKoodisto,
        kuntaKoodisto,
        vuosiluokatKoodisto,
        oppilaitostyyppiKoodisto,
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
        formState: { errors: perustiedotValidationErrors, isDirty },
        handleSubmit: perustiedotHandleSubmit,
        control: perustiedotControl,
        watch: watchPerustiedot,
    } = useForm<Perustiedot>({ resolver: joiResolver(PerustietolomakeSchema) });
    const {
        getValues: getYhteystiedotValues,
        reset: yhteystiedotReset,
        watch: watchYhteystiedot,
        setValue: setYhteystiedotValue,
        register: yhteystiedotRegister,
        formState: { errors: yhteystiedotValidationErrors },
        handleSubmit: yhteystiedotHandleSubmit,
        control: yhteystiedotControl,
    } = useForm<Yhteystiedot>({
        defaultValues: mapApiYhteystiedotToUi(postinumerotKoodisto),
        resolver: joiResolver(YhteystietoLomakeSchema),
    });
    const {
        register: yhteystietoArvoRegister,
        reset: yhteystietoArvoReset,
        handleSubmit: yhteystietoArvoHandleSubmit,
    } = useForm<YhteystietoArvot>({
        defaultValues: {},
    });
    const watchOrganisaatioTyypit = watchPerustiedot('organisaatioTyypit');
    const watchOppilaitosTyyppiUri = watchPerustiedot('oppilaitosTyyppiUri');

    useEffect(() => {
        (async function () {
            const organisaatio = await readOrganisaatio(params.oid);
            if (organisaatio) {
                await resetOrganisaatio({ ...organisaatio });
            }
        })();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [params.oid]);
    useEffect(() => {
        const organisaatioRakenne = resolveOrganisaatio(rakenne, {
            organisaatioTyypit: watchOrganisaatioTyypit || [],
            oppilaitosTyyppiUri: watchOppilaitosTyyppiUri?.value || '',
            oid: params.oid,
        });
        setResolvedOrganisaatioRakenne(organisaatioRakenne);
    }, [params.oid, watchOppilaitosTyyppiUri?.value, watchOrganisaatioTyypit]);
    const { historia, historiaLoading, historiaError, executeHistoria } = useOrganisaatioHistoria(params.oid);
    const [muokattu, setMuokattu] = useState(0);
    const handleLisaaUusiToimija = () => {
        return history.push(`/lomake/uusi?parentOid=${organisaatioBase ? organisaatioBase.oid : ROOT_OID}`);
    };

    const mapOrganisaatioToUi = ({
        nimi: mappingNimi,
        maaUri,
        kieletUris,
        kotipaikkaUri,
        muutKotipaikatUris,
        alkuPvm,
        tyypit,
        oppilaitosTyyppiUri,
        oppilaitosKoodi,
        muutOppilaitosTyyppiUris,
        vuosiluokat,
        yhteystiedot: apiYhteystiedot,
        lakkautusPvm,
        ytunnus: mappingYtunnus,
        yhteystietoArvos,
        ...rest
    }: ApiOrganisaatio): {
        Uiperustiedot: Perustiedot;
        UibaseTiedot: UiOrganisaatioBase;
        Uiyhteystiedot: Yhteystiedot;
        UIhteysTietoArvot: YhteystietoArvot;
    } => {
        const maa = maatJaValtiotKoodisto.uri2SelectOption(maaUri);
        const kotipaikka = kuntaKoodisto.uri2SelectOption(kotipaikkaUri);
        const kielet = kieletUris.map((kieliUri) => oppilaitoksenOpetuskieletKoodisto.uri2SelectOption(kieliUri));
        const muutKotipaikat =
            muutKotipaikatUris?.map((muuKotipaikkaUri) => kuntaKoodisto.uri2SelectOption(muuKotipaikkaUri)) || [];
        return {
            Uiperustiedot: {
                nimi: mappingNimi,
                maa,
                kielet,
                kotipaikka,
                muutKotipaikat,
                alkuPvm,
                lakkautusPvm,
                ytunnus: mappingYtunnus,
                organisaatioTyypit: tyypit,
                oppilaitosTyyppiUri: oppilaitostyyppiKoodisto.uri2SelectOption(oppilaitosTyyppiUri),
                oppilaitosKoodi,
                muutOppilaitosTyyppiUris: muutOppilaitosTyyppiUris.map((kieliUri) =>
                    oppilaitostyyppiKoodisto.uri2SelectOption(kieliUri)
                ),
                vuosiluokat: vuosiluokat.map((kieliUri) => vuosiluokatKoodisto.uri2SelectOption(kieliUri)),
            },
            UibaseTiedot: { ...rest, apiYhteystiedot, currentNimi: mappingNimi },
            Uiyhteystiedot: mapApiYhteystiedotToUi(postinumerotKoodisto, apiYhteystiedot),
            UIhteysTietoArvot: {
                koskiposti: (yhteystietoArvos || [])
                    .filter((a) => {
                        return a['YhteystietojenTyyppi.oid'] === '1.2.246.562.5.79385887983';
                    })
                    .reduce((p, c) => {
                        switch (c['YhteystietoArvo.kieli'].substr(0, 8)) {
                            case 'kieli_fi':
                                return { ...p, fi: c['YhteystietoArvo.arvoText'] };
                            case 'kieli_sv':
                                return { ...p, sv: c['YhteystietoArvo.arvoText'] };
                            case 'kieli_en':
                                return { ...p, en: c['YhteystietoArvo.arvoText'] };
                            default:
                                return { ...p };
                        }
                    }, {}),
            },
        };
    };

    async function resetOrganisaatio({ organisaatio, polku }) {
        const { Uiyhteystiedot, UibaseTiedot, Uiperustiedot, UIhteysTietoArvot } = mapOrganisaatioToUi(organisaatio);
        setOrganisaatioNimiPolku(polku);
        setOrganisaatioBase(UibaseTiedot);
        const data = await readOrganisaatio(organisaatio.parentOid || ROOT_OID, true);
        if (data) {
            const {
                organisaatio: { tyypit, oid },
            } = data;
            setParentTiedot({ organisaatioTyypit: tyypit, oid });
            perustiedotReset(Uiperustiedot);
            yhteystiedotReset(Uiyhteystiedot);
            yhteystietoArvoReset(UIhteysTietoArvot);
        }
    }

    async function handleOrganisationMerge(props: LiitaOrganisaatioon) {
        if (organisaatioBase?.oid) {
            const mergeOrganisaatioResult = await mergeOrganisaatio({
                oid: organisaatioBase.oid,
                ...props,
            });
            if (mergeOrganisaatioResult) {
                const organisaatioAfterMerge = await readOrganisaatio(params.oid);
                if (organisaatioAfterMerge) {
                    await resetOrganisaatio({ ...organisaatioAfterMerge });
                    executeHistoria();
                }
            }
        }
    }

    async function handleSiirraOrganisaatio(props: LiitaOrganisaatioon) {
        setSiirraOrganisaatioModaaliAuki(false);
        setSiirraOrganisaatio(initialSiirra);
        await handleOrganisationMerge(props);
    }

    async function cancelSiirraOrganisaatio() {
        setSiirraOrganisaatioModaaliAuki(false);
        setSiirraOrganisaatio(initialSiirra);
    }

    async function handleYhdistaOrganisaatio(props: LiitaOrganisaatioon) {
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
                    yhteystietoArvoHandleSubmit(async (yhteystietoArvoFormValuet) => {
                        try {
                            setIsLoading(true);
                            const apiOrganisaatio = mapUiOrganisaatioToApiToUpdate(
                                postinumerotKoodisto,
                                organisaatioBase,
                                yhteystiedotFormValues,
                                perustiedotFormValues,
                                yhteystietoArvoFormValuet
                            );
                            const organisaatio = await updateOrganisaatio(apiOrganisaatio);
                            if (organisaatio) {
                                await resetOrganisaatio({ organisaatio, polku: organisaatioNimiPolku });
                                setMuokattu(muokattu + 1);
                            }
                        } finally {
                            setIsLoading(false);
                        }
                    })();
                })();
            })();
        }
    }

    if (!organisaatioBase || historiaLoading || historiaError || isLoading) {
        return (
            <PaaOsio>
                <Spin />
            </PaaOsio>
        );
    }

    registerPerustiedot('nimi');
    const { nimi, ytunnus, organisaatioTyypit } = getPerustiedotValues();
    const resolvedTyypit = resolveOrganisaatioTyypit(rakenne, organisaatioTyypitKoodisto, parentTiedot);
    const opetusKielet = getPerustiedotValues('kielet')?.map((kieliOption) => kieliOption.label) || [];
    const accordionProps = () => {
        const lomakkeet = [] as React.ReactElement[];
        const otsikot = [] as string[];
        lomakkeet.push(
            <PerustietoLomake
                resolvedTyypit={resolvedTyypit}
                getPerustiedotValues={getPerustiedotValues}
                formRegister={registerPerustiedot}
                setPerustiedotValue={setPerustiedotValue}
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
                getYhteystiedotValues={getYhteystiedotValues}
                opetusKielet={opetusKielet}
                watch={watchYhteystiedot}
                setYhteystiedotValue={setYhteystiedotValue}
                formControl={yhteystiedotControl}
                hasValidationErrors={!!Object.keys(yhteystiedotValidationErrors).length}
                formRegister={yhteystiedotRegister}
                key={YHTEYSTIEDOTID}
            />
        );
        otsikot.push(i18n.translate('LOMAKE_YHTEYSTIEDOT'));
        if (organisaatioTyypit?.includes('organisaatiotyyppi_02')) {
            lomakkeet.push(
                <ArvoLomake tyyppiOid={'1.2.246.562.5.79385887983'} yhteystietoArvoRegister={yhteystietoArvoRegister} />
            );
            otsikot.push(i18n.translate('LOMAKE_KOSKI_POSTI'));
        }
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
            <YlaBanneri>
                <div>
                    <Link to={'/organisaatiot'}>
                        <Icon icon={homeIcon} />
                    </Link>
                </div>
                {organisaatioNimiPolku.map((o, index) => [
                    <div key={o.oid}>
                        <Link to={`${o.oid}`}>{o.nimi[language] || o.nimi['fi'] || o.nimi['sv'] || o.nimi['en']}</Link>
                    </div>,
                    organisaatioNimiPolku.length - 1 !== index && <div> &gt; </div>,
                ])}
            </YlaBanneri>
            <ValiContainer>
                <ValiOtsikko>
                    <h3>
                        {(organisaatioTyypit?.length > 0 &&
                            organisaatioTyypitKoodisto.uri2Nimi(organisaatioTyypit[0])) ||
                            i18n.translate('LABEL_NOT_AVAILABLE')}
                    </h3>
                    <h1>
                        {i18n.translateNimi(nimi)}
                        {organisaatioBase?.status !== 'AKTIIVINEN' && ` (${i18n.translate('LABEL_PASSIIVINEN')})`}
                    </h1>
                </ValiOtsikko>
                <ValiNappulat>
                    {resolvedOrganisaatioRakenne?.moveTargetType.length > 0 &&
                        casMe.canHaveButton('LOMAKE_SIIRRA_ORGANISAATIO') && (
                            <Button
                                onClick={() => {
                                    setSiirraOrganisaatio({ ...siirraOrganisaatio });
                                    setSiirraOrganisaatioModaaliAuki(true);
                                }}
                            >
                                {i18n.translate('LOMAKE_SIIRRA_ORGANISAATIO')}
                            </Button>
                        )}
                    {resolvedOrganisaatioRakenne?.mergeTargetType.length > 0 &&
                        casMe.canHaveButton('LOMAKE_YHDISTA_ORGANISAATIO') && (
                            <Button
                                onClick={() => {
                                    setYhdistaOrganisaatio({ ...yhdistaOrganisaatio });
                                    setYhdistaOrganisaatioModaaliAuki(true);
                                }}
                            >
                                {i18n.translate('LOMAKE_YHDISTA_ORGANISAATIO')}
                            </Button>
                        )}
                    {showCreateChildButton(resolvedOrganisaatioRakenne) &&
                        casMe.canHaveButton('LOMAKE_LISAA_UUSI_TOIMIJA') && (
                            <LomakeButton
                                disabled={isDirty}
                                onClick={handleLisaaUusiToimija}
                                label={'LOMAKE_LISAA_UUSI_TOIMIJA'}
                            />
                        )}
                </ValiNappulat>
            </ValiContainer>
            <PaaOsio>
                <Accordion {...accordionProps()} />
            </PaaOsio>
            <AlaBanneri>
                <VersioContainer>
                    <Muokattu oid={organisaatioBase.oid} muokattu={muokattu} />
                </VersioContainer>
                <div>
                    <LomakeButton label={'BUTTON_SULJE'} onClick={() => history.push('/organisaatiot')} />
                    {casMe.canHaveButton('BUTTON_TALLENNA') && (
                        <LomakeButton label={'BUTTON_TALLENNA'} onClick={saveOrganisaatio} />
                    )}
                </div>
            </AlaBanneri>
            {yhdistaOrganisaatioModaaliAuki && (
                <LiitaOrganisaatio
                    liitaOrganisaatioon={yhdistaOrganisaatio}
                    organisaatioBase={organisaatioBase}
                    handleChange={setYhdistaOrganisaatio}
                    tallennaCallback={() => {
                        handleYhdistaOrganisaatio({ ...yhdistaOrganisaatio });
                    }}
                    peruutaCallback={() => {
                        cancelYhdistaOrganisaatio();
                    }}
                    suljeCallback={() => cancelYhdistaOrganisaatio()}
                    targetType={resolvedOrganisaatioRakenne.mergeTargetType[0]}
                    labels={{
                        title: 'TOIMIPISTEEN_YHDISTYS_TITLE',
                        confirmTitle: 'TOIMIPISTEEN_YHDISTYS_VAHVISTUS_TITLE',
                        confirmMessage: 'TOIMIPISTEEN_YHDISTYS_VAHVISTUS_{from}_TO_{to}',
                        otherOrg: 'ORGANISAATIO_YHDISTYS_TOINEN_ORGANISAATIO',
                        liitosPvm: 'ORGANISAATIO_YHDISTYS_PVM',
                    }}
                />
            )}
            {siirraOrganisaatioModaaliAuki && (
                <LiitaOrganisaatio
                    liitaOrganisaatioon={siirraOrganisaatio}
                    organisaatioBase={organisaatioBase}
                    handleChange={setSiirraOrganisaatio}
                    tallennaCallback={() => {
                        handleSiirraOrganisaatio({ ...siirraOrganisaatio });
                    }}
                    peruutaCallback={() => {
                        cancelSiirraOrganisaatio();
                    }}
                    suljeCallback={() => cancelSiirraOrganisaatio()}
                    targetType={resolvedOrganisaatioRakenne.moveTargetType[0]}
                    labels={{
                        title: 'TOIMIPISTEEN_SIIRTO_TITLE',
                        confirmTitle: 'TOIMIPISTEEN_SIIRTO_VAHVISTUS_TITLE',
                        confirmMessage: 'TOIMIPISTEEN_SIIRTO_VAHVISTUS_{from}_TO_{to}',
                        otherOrg: 'ORGANISAATIO_SIIRTO_TOINEN_ORGANISAATIO',
                        liitosPvm: 'ORGANISAATIO_SIIRTO_PVM',
                    }}
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
