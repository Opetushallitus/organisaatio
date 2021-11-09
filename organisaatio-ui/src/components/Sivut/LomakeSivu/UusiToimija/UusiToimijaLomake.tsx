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
import { Perustiedot, ParentTiedot } from '../../../../types/types';
import PerustietoLomake from './PerustietoLomake/PerustietoLomake';
import YhteystietoLomake from '../Koulutustoimija/YhteystietoLomake/YhteystietoLomake';
import Icon from '@iconify/react';
import { Link, useHistory } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { joiResolver } from '@hookform/resolvers/joi';
import {
    createOrganisaatio,
    mapApiYhteystiedotToUi,
    mapUiOrganisaatioToApiToSave,
    readOrganisaatio,
} from '../../../../api/organisaatio';
import {resolveOrganisaatio, resolveOrganisaatioTyypit, resolveParentOidByQuery} from '../../../../tools/organisaatio';
import YhteystietoLomakeSchema from '../../../../ValidationSchemas/YhteystietoLomakeSchema';
import PerustietolomakeSchema from '../../../../ValidationSchemas/PerustietolomakeSchema';
import YTJModaali from '../../../Modaalit/YTJModaali/YTJModaali';

const PERUSTIEDOTUUID = 'perustietolomake';
const YHTEYSTIEDOTUUID = 'yhteystietolomake';

const UusiToimijaLomake = (props: { history: string[]; location: { search: string } }) => {
    const history = useHistory();
    const { i18n } = useContext(LanguageContext);
    const [YTJModaaliAuki, setYTJModaaliAuki] = useState<boolean>(false);
    const parentOid = resolveParentOidByQuery(props.location.search);
    const { organisaatioTyypitKoodisto, postinumerotKoodisto } = useContext(KoodistoContext);
    const [parentTiedot, setParentTiedot] = useState<ParentTiedot>({ organisaatioTyypit: [], oid: '' });
    const [lomakeAvoinna, setLomakeAvoinna] = useState<string>(PERUSTIEDOTUUID);

    useEffect(() => {
        (async function () {
            const {
                organisaatio: { tyypit, oid },
            } = await readOrganisaatio(parentOid);
            setParentTiedot({ organisaatioTyypit: tyypit, oid });
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
        defaultValues: mapApiYhteystiedotToUi(postinumerotKoodisto, []),
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
    const organisaatioRakenne = resolveOrganisaatio(rakenne, {
        organisaatioTyypit: watchPerustiedot('organisaatioTyypit', []),
    });
    const resolvedTyypit = resolveOrganisaatioTyypit(rakenne, organisaatioTyypitKoodisto, parentTiedot);

    async function saveOrganisaatio() {
        await perustiedotHandleSubmit((perustiedotFormValues) => {
            yhteystiedotHandleSubmit(async (yhteystiedotFormValues) => {
                const apiOrganisaatio = mapUiOrganisaatioToApiToSave(
                    postinumerotKoodisto,
                    yhteystiedotFormValues,
                    perustiedotFormValues,
                    parentOid
                );
                const savedOrganisaatio = await createOrganisaatio(apiOrganisaatio);
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
                resolvedTyypit={resolvedTyypit}
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
