import * as React from 'react';
import { useContext, useEffect, useState } from 'react';
import styles from './UusiToimijaLomake.module.css';
import PohjaSivu from '../../PohjaSivu/PohjaSivu';
import Accordion from '../../../Accordion/Accordion';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import queryString from 'query-string';
import homeIcon from '@iconify/icons-fa-solid/home';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';
import { KoodistoContext, LanguageContext, rakenne, ROOT_OID } from '../../../../contexts/contexts';
import { Organisaatio, Perustiedot } from '../../../../types/types';
import PerustietoLomake from './PerustietoLomake/PerustietoLomake';
import YhteystietoLomake from '../Koulutustoimija/YhteystietoLomake/YhteystietoLomake';
import Icon from '@iconify/react';
import { Link, useHistory } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { joiResolver } from '@hookform/resolvers/joi';
import { createOrganisaatio, readOrganisaatio } from '../../../../api/organisaatio';
import { resolveOrganisaatio, resolveOrganisaatioTyypit } from '../../../../tools/organisaatio';
import { mapApiYhteystiedotToUi, mapUiYhteystiedotToApi } from '../../../../tools/mappers';
import YhteystietoLomakeSchema from '../../../../ValidationSchemas/YhteystietoLomakeSchema';
import PerustietolomakeSchema from '../../../../ValidationSchemas/PerustietolomakeSchema';
import YTJModaali from '../../../Modaalit/YTJModaali/YTJModaali';

const PERUSTIEDOTUUID = 'perustietolomake';
const YHTEYSTIEDOTUUID = 'yhteystietolomake';

const UusiToimijaLomake = (props: { history: string[]; location: { search: string } }) => {
    const history = useHistory();
    const { i18n } = useContext(LanguageContext);
    const [YTJModaaliAuki, setYTJModaaliAuki] = useState<boolean>(false);
    const { parentOid } = queryString.parse(props.location.search);
    const { organisaatioTyypitKoodisto } = useContext(KoodistoContext);
    const [parentOrganisaatio, setParentOrganisaatio] = useState<Organisaatio | undefined>(undefined);
    const [lomakeAvoinna, setLomakeAvoinna] = useState<string>(PERUSTIEDOTUUID);

    useEffect(() => {
        (async function () {
            const parent = await readOrganisaatio((parentOid || ROOT_OID) as string);
            setParentOrganisaatio(Object.assign({}, parent.organisaatio));
        })();
    }, [parentOid]);

    const {
        reset: resetPerustiedot,
        watch: watchPerustiedot,
        setValue: setPerustiedotValue,
        register: registerPerustiedot,
        formState: { errors: perustiedotValidationErrors },
        handleSubmit: perustiedotHandleSubmit,
        control: perustiedotControl,
    } = useForm<Perustiedot>({ resolver: joiResolver(PerustietolomakeSchema) });

    const {
        reset: resetYhteystiedot,
        watch,
        setValue: setYhteystiedotValue,
        register: registerYhteystiedot,
        formState: { errors: yhteystiedotValidationErrors },
        handleSubmit: yhteystiedotHandleSubmit,
        control: yhteystiedotControl,
    } = useForm({
        defaultValues: mapApiYhteystiedotToUi([]),
        resolver: joiResolver(YhteystietoLomakeSchema),
    });

    const validateChanges = (accordionUuids: string[]): void => {
        const accordionuuid = accordionUuids[0];
        const setAvoinnaCb = () => {
            setLomakeAvoinna(accordionuuid);
        };
        switch (lomakeAvoinna) {
            case PERUSTIEDOTUUID:
                perustiedotHandleSubmit(setAvoinnaCb)();
                break;
            case YHTEYSTIEDOTUUID:
                yhteystiedotHandleSubmit(setAvoinnaCb)();
                break;
            default:
                return setAvoinnaCb();
        }
    };
    // const handleYtjData = (ytjData: YtjData) => {
    //     if (ytjData.kunta) setPerustiedotValue('kotipaikkaUri', ytjData.kunta);
    //     else warning({ message: 'YTJ_DATA_KOTIPAIKKA_NOT_FOUND_IN_KOODISTO' });
    //     if (ytjData.kieli) setPerustiedotValue('kieletUris', [ytjData.kieli]);
    //     else warning({ message: 'YTJ_DATA_UNKNOWN_KIELI' });
    //
    //     setPerustiedotValue('ytunnus', ytjData.ytunnus);
    //     setPerustiedotValue('nimi', { fi: ytjData.nimi, sv: ytjData.nimi, en: ytjData.nimi });
    //     setPerustiedotValue('alkuPvm', ytjData.aloitusPvm);
    //     setYhteystiedotValue('kieli_fi#1', ytjData.yhteysTiedot);
    //     setYhteystiedotValue('osoitteetOnEri', !!ytjData.kayntiOsoite);
    //     setYTJModaaliAuki(false);
    // };
    const organisaatioRakenne = resolveOrganisaatio(rakenne, { tyypit: watchPerustiedot('tyypit') || [] });
    const resolvedTyypit = resolveOrganisaatioTyypit(rakenne, organisaatioTyypitKoodisto, parentOrganisaatio);

    async function saveOrganisaatio() {
        await perustiedotHandleSubmit((perustiedotFormValues) => {
            yhteystiedotHandleSubmit(async (yhteystiedotFormValues) => {
                const yhteystiedot = mapUiYhteystiedotToApi([], yhteystiedotFormValues);
                const { kotipaikkaUri, maaUri, kieletUris, muutKotipaikatUris } = perustiedotFormValues;
                const nimet = [
                    {
                        nimi: Object.assign({}, perustiedotFormValues.nimi),
                        alkuPvm: new Date().toISOString().split('T')[0],
                    },
                ];
                const orgToBeUpdated = {
                    ...{
                        ...perustiedotFormValues,
                        kotipaikkaUri: kotipaikkaUri?.value,
                        maaUri: maaUri?.value,
                        kieletUris: kieletUris?.map((a) => a.value),
                        muutKotipaikatUris: muutKotipaikatUris?.map((a) => a.value),
                    },
                    yhteystiedot,
                    parentOid: (parentOid || ROOT_OID) as string,
                    nimet,
                };
                const savedOrganisaatio = await createOrganisaatio(orgToBeUpdated);
                if (savedOrganisaatio) {
                    props.history.push(`/lomake/${savedOrganisaatio.oid}`);
                }
            })();
        })();
    }
    if (!organisaatioRakenne || !resolvedTyypit) {
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
                organisaatioTyypit={resolvedTyypit}
                rakenne={organisaatioRakenne}
                watchPerustiedot={watchPerustiedot}
                handleJatka={() => validateChanges([YHTEYSTIEDOTUUID])}
                openYtjModal={() => setYTJModaaliAuki(true)}
                validationErrors={perustiedotValidationErrors}
                formControl={perustiedotControl}
                setPerustiedotValue={setPerustiedotValue}
                setYhteystiedotValue={setYhteystiedotValue}
                formRegister={registerPerustiedot}
                key={PERUSTIEDOTUUID}
            />
        );
        otsikot.push(i18n.translate('LOMAKE_PERUSTIEDOT'));
        lomakkeet.push(
            <YhteystietoLomake
                setYhteystiedotValue={setYhteystiedotValue}
                watch={watch}
                formControl={yhteystiedotControl}
                validationErrors={yhteystiedotValidationErrors}
                formRegister={registerYhteystiedot}
                key={YHTEYSTIEDOTUUID}
            />
        );
        otsikot.push(i18n.translate('LOMAKE_YHTEYSTIEDOT'));
        return { lomakkeet, otsikot, handleUuidChange: validateChanges, preExpanded: lomakeAvoinna };
    };

    function handleCancel() {
        resetPerustiedot();
        resetYhteystiedot();
        history.goBack();
    }

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
                <Accordion {...accordionProps()} />
            </div>
            <div className={styles.AlaBanneri}>
                <div>
                    <Button variant="outlined" className={styles.Versionappula} onClick={handleCancel}>
                        {i18n.translate('BUTTON_SULJE')}
                    </Button>
                    <Button className={styles.Versionappula} onClick={saveOrganisaatio}>
                        {i18n.translate('BUTTON_TALLENNA')}
                    </Button>
                </div>
            </div>
            {YTJModaaliAuki && (
                <YTJModaali
                    ytunnus={watchPerustiedot('ytunnus') || ''}
                    setters={{ setPerustiedotValue, setYhteystiedotValue }}
                    suljeModaali={() => setYTJModaaliAuki(false)}
                />
            )}
        </PohjaSivu>
    );
};

export default UusiToimijaLomake;
