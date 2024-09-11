import * as React from 'react';
import { useEffect, useState } from 'react';
import PohjaSivu from '../../PohjaSivu/PohjaSivu';
import Accordion from '../../../Accordion/Accordion';
import homeIcon from '@iconify/icons-fa-solid/home';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';
import { rakenne } from '../../../../contexts/constants';
import { Nimi, ParentTiedot, Perustiedot } from '../../../../types/types';
import PerustietoLomake from './PerustietoLomake/PerustietoLomake';
import YhteystietoLomake from '../Koulutustoimija/YhteystietoLomake/YhteystietoLomake';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { joiResolver } from '@hookform/resolvers/joi';
import { createOrganisaatio, readOrganisaatio } from '../../../../api/organisaatio';
import { mapApiYhteystiedotToUi, mapUiOrganisaatioToApiToSave } from '../../../../api/organisaatioMappers';
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
import IconWrapper from '../../../IconWapper/IconWrapper';
import { useAtom } from 'jotai';
import { koodistotAtom, organisaatioTyypitKoodistoAtom, postinumerotKoodistoAtom } from '../../../../api/koodisto';
import { languageAtom } from '../../../../api/lokalisaatio';

const PERUSTIEDOTUUID = 'perustietolomake';
const YHTEYSTIEDOTUUID = 'yhteystietolomake';

const UusiToimijaLomake = () => {
    const location = useLocation();
    const navigate = useNavigate();
    const [i18n] = useAtom(languageAtom);
    useAtom(koodistotAtom);
    const [YTJModaaliAuki, setYTJModaaliAuki] = useState<boolean>(false);
    const parentOid = resolveParentOidByQuery(location.search);
    const [organisaatioTyypitKoodisto] = useAtom(organisaatioTyypitKoodistoAtom);
    const [postinumerotKoodisto] = useAtom(postinumerotKoodistoAtom);
    const [parentTiedot, setParentTiedot] = useState<ParentTiedot>({
        organisaatioTyypit: [],
        oid: '',
    });
    const [lomakeAvoinna, setLomakeAvoinna] = useState<string>(PERUSTIEDOTUUID);
    const [isLoading, setIsLoading] = useState<boolean>(false);

    useEffect(() => {
        (async function () {
            const data = await readOrganisaatio(parentOid, true);
            if (data) {
                const {
                    organisaatio: { tyypit, oid },
                } = data;
                setParentTiedot({ organisaatioTyypit: tyypit, oid });
            }
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
                try {
                    setIsLoading(true);
                    const apiOrganisaatio = mapUiOrganisaatioToApiToSave(
                        postinumerotKoodisto,
                        yhteystiedotFormValues,
                        perustiedotFormValues,
                        parentOid
                    );
                    const savedOrganisaatio = await createOrganisaatio(apiOrganisaatio);
                    if (savedOrganisaatio) {
                        navigate(`/lomake/${savedOrganisaatio.oid}`);
                    }
                } finally {
                    setIsLoading(false);
                }
            })();
        })();
    }

    if (!organisaatioRakenne || !resolvedTyypit || isLoading) {
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
                hasValidationErrors={!!Object.keys(yhteystiedotValidationErrors).length}
                formRegister={registerYhteystiedot}
                key={YHTEYSTIEDOTUUID}
                isYtj={!!getPerustiedotValues('ytunnus')}
            />
        );
        otsikot.push(i18n.translate('LOMAKE_YHTEYSTIEDOT'));
        return { lomakkeet, otsikot, handleUuidChange: validateChanges, preExpanded: lomakeAvoinna };
    };

    function handleCancel() {
        resetPerustiedot();
        resetYhteystiedot();
        navigate(-1);
    }

    return (
        <PohjaSivu>
            <YlaBanneri>
                <Link to={'/organisaatiot'}>
                    <IconWrapper icon={homeIcon} />
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
                    suljeModaali={(nimi?: Nimi) => {
                        if (nimi) setPerustiedotValue('nimi', nimi);
                        setYTJModaaliAuki(false);
                    }}
                />
            )}
        </PohjaSivu>
    );
};

export default UusiToimijaLomake;
