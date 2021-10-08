import * as React from 'react';
import { useContext, useEffect, useState } from 'react';
import styles from './UusiToimijaLomake.module.css';
import PohjaSivu from '../../PohjaSivu/PohjaSivu';
import Accordion from '../../../Accordion/Accordion';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import queryString from 'query-string';
import homeIcon from '@iconify/icons-fa-solid/home';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';
import { LanguageContext, rakenne, ROOT_OID } from '../../../../contexts/contexts';
import { NewOrganisaatio, Organisaatio, Nimi} from '../../../../types/types';
import PerustietoLomake from './PerustietoLomake/PerustietoLomake';
import YhteystietoLomake from '../Koulutustoimija/YhteystietoLomake/YhteystietoLomake';
import Icon from '@iconify/react';
import useKoodisto from '../../../../api/koodisto';
import { Link, useHistory } from 'react-router-dom';
import { createOrganisaatio, readOrganisaatio } from '../../../../api/organisaatio';
import { resolveOrganisaatioTyypit } from '../../../../tools/organisaatio';
import { useForm } from 'react-hook-form';
import { joiResolver } from '@hookform/resolvers/joi';
import {mapApiYhteystiedotToUi, mapUiYhteystiedotToApi} from '../../../../tools/mappers';
import YhteystietoLomakeSchema from '../../../../ValidationSchemas/YhteystietoLomakeSchema';
import PerustietolomakeSchema from '../../../../ValidationSchemas/PerustietolomakeSchema';
/*
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
                        ] as ApiYhteystiedot[]
                )
                .flat(),
        },
    };
};

 */

const PERUSTIEDOTUUID = 'perustietolomake';
const YHTEYSTIEDOTUUID = 'yhteystietolomake';

const UusiToimijaLomake = (props: { history: string[]; location: { search: string } }) => {
    const history = useHistory();
    const { i18n } = useContext(LanguageContext);
    const [parentOrganisaatio, setParentOrganisaatio] = useState<Organisaatio | undefined>(undefined);
    const {
        data: organisaatioTyypit,
        loading: organisaatioTyypitLoading,
        error: organisaatioTyypitError,
    } = useKoodisto('ORGANISAATIOTYYPPI');
    const { parentOid } = queryString.parse(props.location.search);

    async function saveOrganisaatio() {
        perustiedotHandleSubmit((perustiedotFormValues) => {
            yhteystiedotHandleSubmit(async (yhteystiedotFormValues) => {
                const yhteystiedot = mapUiYhteystiedotToApi([], yhteystiedotFormValues);
                const orgToBeUpdated = {...perustiedotFormValues, yhteystiedot, parentOid: (parentOid || ROOT_OID) as string, nimet: [perustiedotFormValues.nimi]} as NewOrganisaatio;
                const savedOrganisaatio = await createOrganisaatio(orgToBeUpdated);
                if(savedOrganisaatio) {
                    props.history.push(`/lomake/${savedOrganisaatio.oid}`);
                }
            })()
        })();}

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
    } = useForm({ resolver: joiResolver(PerustietolomakeSchema)});

    const {
        reset: resetYhteystiedot,
        watch,
        register: yhteystiedotRegister,
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
    const resolvedTyypit = resolveOrganisaatioTyypit(rakenne, organisaatioTyypit, parentOrganisaatio);
    const handleNimiUpdate = (nimi: Nimi) => {
        setPerustiedotValue('nimi', nimi);
    };
    if (organisaatioTyypitLoading ||
        organisaatioTyypitError || !resolvedTyypit) {
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
                watchPerustiedot={watchPerustiedot}
                handleNimiUpdate={handleNimiUpdate}
                handleJatka={() => validateChanges([YHTEYSTIEDOTUUID])}
                validationErrors={perustiedotValidationErrors}
                formControl={perustiedotControl}
                formRegister={registerPerustiedot}
                key={PERUSTIEDOTUUID}
                organisaatioTyypit={resolvedTyypit}
            />
        );
        otsikot.push(i18n.translate('LOMAKE_PERUSTIEDOT'));
        lomakkeet.push(
            <YhteystietoLomake
                watch={watch}
                formControl={yhteystiedotControl}
                validationErrors={yhteystiedotValidationErrors}
                formRegister={yhteystiedotRegister}
                key={YHTEYSTIEDOTUUID}
            />
            );
            otsikot.push(i18n.translate('LOMAKE_YHTEYSTIEDOT'));
        return {lomakkeet, otsikot, handleUuidChange: validateChanges, preExpanded: lomakeAvoinna};
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
        </PohjaSivu>
    );
};

export default UusiToimijaLomake;
