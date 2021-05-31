import * as React from 'react';
import { useState } from 'react';
import { RouteComponentProps } from 'react-router-dom';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';
import { useEffect } from 'react';
import { AxiosResponse } from 'axios';
import { getRyhma, deleteRyhma, putRyhma } from '../../../HttpRequests';
import { Ryhma, SelectOptionType } from '../../../../types/types';
import { useLanguagedInput } from '../../../../customHooks/CustomHooks';
import { ActionMeta, ValueType } from 'react-select';
import MuokkausLomake from './MuokkausLomake';
import { useContext } from 'react';
import { LanguageContext } from '../../../../contexts/contexts';

export type RyhmanMuokausProps = {
    oid: string;
};

export type OrganisaatioPutResponseType = {
    status: string;
    organisaatio: Ryhma;
};

export const currentDateToStr = () => new Date().toISOString().split('T')[0];

const RyhmanMuokkaus = ({ match, history }: RouteComponentProps<RyhmanMuokausProps>) => {
    const { i18n } = useContext(LanguageContext);
    const [ryhma, setRyhma] = useState<Ryhma>();

    const onPassivoitu = !ryhma || ryhma.status === 'PASSIIVINEN';
    const { value: nimiFiValue, bind: nimiFiBind, setValue: setNimiFiValue } = useLanguagedInput(
        '',
        'nimiFi',
        onPassivoitu
    );
    const { value: nimiSvValue, bind: nimiSvBind, setValue: setNimiSvValue } = useLanguagedInput(
        '',
        'nimiSv',
        onPassivoitu
    );
    const { value: nimiEnValue, bind: nimiEnBind, setValue: setNimiEnValue } = useLanguagedInput(
        '',
        'nimiEn',
        onPassivoitu
    );
    const { value: kuvaus2FiValue, bind: kuvaus2FiBind, setValue: setKuvausFiValue } = useLanguagedInput(
        '',
        'kuvaus2Fi',
        onPassivoitu
    );
    const { value: kuvaus2SvValue, bind: kuvaus2SvBind, setValue: setKuvausSvValue } = useLanguagedInput(
        '',
        'kuvaus2Sv',
        onPassivoitu
    );
    const { value: kuvaus2EnValue, bind: kuvaus2EnBind, setValue: setKuvausEnValue } = useLanguagedInput(
        '',
        'kuvaus2En',
        onPassivoitu
    );

    useEffect(() => {
        async function fetch() {
            try {
                const response = (await getRyhma(match.params.oid)) as AxiosResponse;
                const ryhma = response.data as Ryhma;
                setRyhma(ryhma);
                ryhma.nimi['fi'] && setNimiFiValue(ryhma.nimi['fi']);
                ryhma.nimi['sv'] && setNimiSvValue(ryhma.nimi['sv']);
                ryhma.nimi['en'] && setNimiEnValue(ryhma.nimi['en']);
                ryhma.kuvaus2['kieli_fi#1'] && setKuvausFiValue(ryhma.kuvaus2['kieli_fi#1']);
                ryhma.kuvaus2['kieli_sv#1'] && setKuvausSvValue(ryhma.kuvaus2['kieli_sv#1']);
                ryhma.kuvaus2['kieli_en#1'] && setKuvausEnValue(ryhma.kuvaus2['kieli_en#1']);
            } catch (error) {
                console.error('error fetching', error);
            }
        }
        fetch();
    }, [
        match.params.oid,
        setKuvausEnValue,
        setKuvausSvValue,
        setKuvausFiValue,
        setNimiEnValue,
        setNimiSvValue,
        setNimiFiValue,
    ]);

    if (!ryhma) {
        return <Spin />;
    }

    const handleRyhmaSelectOnChange = (
        values: ValueType<SelectOptionType>[] | ValueType<SelectOptionType> | undefined,
        action: ActionMeta<SelectOptionType>
    ) => {
        const newRyhma = {
            ...ryhma,
            [action.name as string]:
                (values && (values as ValueType<SelectOptionType>[]).map((v) => (v as SelectOptionType).value)) || [],
        } as Ryhma;
        setRyhma(newRyhma);
    };

    const handleTallenna = async () => {
        if (ryhma && ryhma.oid) {
            const newRyhma = {
                ...ryhma,
                nimi: { fi: nimiFiValue, sv: nimiSvValue, en: nimiEnValue },
                kuvaus2: {
                    'kieli_fi#1': kuvaus2FiValue,
                    'kieli_sv#1': kuvaus2SvValue,
                    'kieli_en#1': kuvaus2EnValue,
                },
            };
            try {
                const {
                    data: { organisaatio: updatedRyhma },
                } = (await putRyhma(newRyhma)) as AxiosResponse;
                setRyhma(updatedRyhma);
                history.push('/ryhmat');
            } catch (error) {
                console.error('error while updating ryhma', error);
            }
        }
    };

    const handlePeruuta = () => {
        history.push('/ryhmat');
    };
    const handlePoista = async () => {
        const r = global.window.confirm(i18n.translate('RYHMAT_POISTO_VARMISTUSTEKSTI'));
        try {
            r && (await deleteRyhma(ryhma));
            history.push('/ryhmat');
        } catch (error) {
            console.error('error while deleting ryhma', error);
        }
    };

    const handlePassivoi = async () => {
        const { status } = ryhma as Ryhma;
        let newRyhma;
        if (status === 'AKTIIVINEN') {
            newRyhma = { ...ryhma, lakkautusPvm: currentDateToStr() };
        } else {
            const { lakkautusPvm, ...rest } = ryhma;
            newRyhma = rest;
        }
        const {
            data: { organisaatio: updatedRyhma },
        } = (await putRyhma(newRyhma)) as AxiosResponse<OrganisaatioPutResponseType>;
        setRyhma(updatedRyhma);
    };

    return (
        <MuokkausLomake
            nimiFiBind={nimiFiBind}
            nimiSvBind={nimiSvBind}
            nimiEnBind={nimiEnBind}
            kuvaus2FiBind={kuvaus2FiBind}
            kuvaus2SvBind={kuvaus2SvBind}
            kuvaus2EnBind={kuvaus2EnBind}
            ryhma={ryhma}
            handleRyhmaSelectOnChange={handleRyhmaSelectOnChange}
            handlePeruuta={handlePeruuta}
            handlePassivoi={handlePassivoi}
            handlePoista={handlePoista}
            handleTallenna={handleTallenna}
        />
    );
};

export default RyhmanMuokkaus;
