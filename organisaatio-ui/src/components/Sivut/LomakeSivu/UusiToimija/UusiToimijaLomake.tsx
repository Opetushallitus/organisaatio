import * as React from 'react';
import { useContext, useEffect, useState } from 'react';
import PohjaSivu from '../../PohjaSivu/PohjaSivu';
import Accordion from '../../../Accordion/Accordion';
import homeIcon from '@iconify/icons-fa-solid/home';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';
import { KoodistoContext, LanguageContext, rakenne } from '../../../../contexts/contexts';
import { ParentTiedot, Perustiedot } from '../../../../types/types';
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
import {
    resolveOrganisaatio,
    resolveOrganisaatioTyypit,
    resolveParentOidByQuery,
} from '../../../../tools/organisaatio';
import YhteystietoLomakeSchema from '../../../../ValidationSchemas/YhteystietoLomakeSchema';
import PerustietolomakeSchema from '../../../../ValidationSchemas/PerustietolomakeSchema';
import YTJModaali from '../../../Modaalit/YTJModaali/YTJModaali';
import {
    AlaBanneri,
    LomakeButton,
    MuokattuKolumni,
    PaaOsio,
    ValiContainer,
    ValiOtsikko,
    VersioContainer,
    YlaBanneri,
} from '../LomakeFields/LomakeFields';

const PERUSTIEDOTUUID = 'perustietolomake';
const YHTEYSTIEDOTUUID = 'yhteystietolomake';

const UusiToimijaLomake = (props: { history: string[]; location: { search: string } }) => {
    const history = useHistory();
    const { i18n } = useContext(LanguageContext);
    const [YTJModaaliAuki, setYTJModaaliAuki] = useState<boolean>(false);
    const parentOid = resolveParentOidByQuery(props.location.search);
    const { organisaatioTyypitKoodisto, postinumerotKoodisto } = useContext(KoodistoContext);
    const [parentTiedot, setParentTiedot] = useState<ParentTiedot>({
        organisaatioTyypit: [],
        oid: '',
    });
    const [lomakeAvoinna, setLomakeAvoinna] = useState<string>(PERUSTIEDOTUUID);

    useEffect(() => {
        (async function () {
            const {
                organisaatio: { tyypit, oid },
            } = await readOrganisaatio(parentOid, true);
            setParentTiedot({ organisaatioTyypit: tyypit, oid });
        })();
    }, [parentOid]);

    const {
        reset: resetPerustiedot,
        watch: watchPerustiedot,
        setValue: setPerustiedotValue,
        getValues: getPerustiedotValues,
        register: registerPerustiedot,
        formState: { errors: perustiedotValidationErrors },
        handleSubmit: perustiedotHandleSubmit,
        control: perustiedotControl,
    } = useForm<Perustiedot>({ resolver: joiResolver(PerustietolomakeSchema) });

    const {
        getValues: getYhteystiedotValues,
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
        organisaatioTyypit: watchPerustiedot('organisaatioTyypit') || [],
        oppilaitosTyyppiUri: watchPerustiedot('oppilaitosTyyppiUri')?.value || '',
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
            <PaaOsio>
                <Spin />
            </PaaOsio>
        );
    }
    const opetusKielet = getPerustiedotValues('kielet')?.map((kieliOption) => kieliOption.label) || [];
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
                getPerustiedotValues={getPerustiedotValues}
            />
        );
        otsikot.push(i18n.translate('LOMAKE_PERUSTIEDOT'));
        lomakkeet.push(
            <YhteystietoLomake
                getYhteystiedotValues={getYhteystiedotValues}
                opetusKielet={opetusKielet}
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
            <YlaBanneri>
                <Link to={'/organisaatiot'}>
                    <Icon icon={homeIcon} />
                    {i18n.translate('UUSI_TOIMIJA_TITLE')}
                </Link>
            </YlaBanneri>
            <ValiContainer>
                <ValiOtsikko>
                    <h3>{i18n.translate('UUSI_TOIMIJA_TOIMIJA_TITLE')}</h3>
                    <h1>{i18n.translate('UUSI_TOIMIJA_UUDEN_TOIMIJAN_LISAAMINEN')}</h1>
                </ValiOtsikko>
            </ValiContainer>
            <PaaOsio>
                <Accordion {...accordionProps()} />
            </PaaOsio>
            <AlaBanneri>
                <VersioContainer>
                    <MuokattuKolumni>
                        <span style={{ color: '#999999' }}>{i18n.translate('VERSIOHISTORIA_MUOKATTU_VIIMEKSI')}</span>
                        <span>-</span>
                    </MuokattuKolumni>
                </VersioContainer>
                <div>
                    <LomakeButton label={'BUTTON_SULJE'} onClick={handleCancel} />
                    <LomakeButton label={'BUTTON_TALLENNA'} onClick={saveOrganisaatio} />
                </div>
            </AlaBanneri>
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
