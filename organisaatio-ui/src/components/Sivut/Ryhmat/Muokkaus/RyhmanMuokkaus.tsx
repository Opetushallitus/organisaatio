import * as React from 'react';
import { useEffect, useState } from 'react';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';
import { deleteRyhma, getRyhma, postRyhma, putRyhma } from '../../../../api/ryhma';
import { NewRyhma, Ryhma } from '../../../../types/types';
import MuokkausLomake from './MuokkausLomake';
import { ROOT_OID } from '../../../../contexts/constants';
import { FieldValues, SubmitHandler } from 'react-hook-form';
import { formatUiDateStrToApi } from '../../../../tools/mappers';
import { useAtom } from 'jotai';
import { languageAtom } from '../../../../api/lokalisaatio';
import { useLocation, useNavigate, useParams } from 'react-router-dom';

export type RyhmanMuokausProps = {
    oid?: string;
};

const emptyRyhma: Partial<NewRyhma> = {
    nimi: {
        fi: '',
    },
    kuvaus2: {
        'kieli_fi#1': '',
    },
    parentOid: ROOT_OID,
    ryhmatyypit: [],
    kayttoryhmat: [],
    status: 'AKTIIVINEN',
    tyypit: ['Ryhma'],
};

const RyhmanMuokkaus = ({ isNew }: { isNew?: boolean }) => {
    const location = useLocation();
    const params = useParams();
    const navigate = useNavigate();
    const [i18n] = useAtom(languageAtom);
    const [ryhma, setRyhma] = useState<Ryhma>();
    const onUusi = isNew || location.pathname.includes('uusi');

    useEffect(() => {
        async function fetch(oid: string) {
            const ryhma = await getRyhma(oid);
            setRyhma(ryhma as Ryhma);
        }
        if (params.oid && !onUusi) {
            fetch(params.oid);
        } else {
            setRyhma({ ...(emptyRyhma as Ryhma) });
        }
    }, [onUusi, params.oid]);

    if (!ryhma) {
        return <Spin />;
    }

    const handleTallenna: SubmitHandler<FieldValues> = async ({
        nimiFi,
        nimiSv,
        nimiEn,
        kuvaus2Fi,
        kuvaus2Sv,
        kuvaus2En,
        ryhmatyypit,
        kayttoryhmat,
    }) => {
        if (ryhma) {
            const newRyhma = {
                ...ryhma,
                nimi: { fi: nimiFi, sv: nimiSv, en: nimiEn },
                kuvaus2: {
                    'kieli_fi#1': kuvaus2Fi,
                    'kieli_sv#1': kuvaus2Sv,
                    'kieli_en#1': kuvaus2En,
                },
                ryhmatyypit: ryhmatyypit.map((a: { value: string; versio: number }) => `${a.value}#${a.versio}`),
                kayttoryhmat: kayttoryhmat.map((a: { value: string; versio: number }) => `${a.value}#${a.versio}`),
            };
            onUusi ? await postRyhma(newRyhma) : await putRyhma(newRyhma);
            navigate('/ryhmat');
        }
    };

    const handlePeruuta = () => {
        navigate('/ryhmat');
    };
    const handlePoista = async () => {
        const r = global.window.confirm(i18n.translate('RYHMAT_POISTO_VARMISTUSTEKSTI'));
        r && (await deleteRyhma(ryhma));
        navigate('/ryhmat');
    };

    const handlePassivoi = async () => {
        const { status } = ryhma as Ryhma;
        let newRyhma;
        if (status === 'AKTIIVINEN') {
            newRyhma = { ...ryhma, lakkautusPvm: formatUiDateStrToApi() };
        } else {
            const { lakkautusPvm, ...rest } = ryhma;
            newRyhma = rest;
        }
        const { organisaatio: updatedRyhma } = await putRyhma(newRyhma);
        setRyhma(updatedRyhma);
    };
    return (
        <MuokkausLomake
            onUusi={onUusi}
            ryhma={ryhma}
            handlePeruuta={handlePeruuta}
            handlePassivoi={handlePassivoi}
            handlePoista={handlePoista}
            handleTallenna={handleTallenna}
        />
    );
};

export default RyhmanMuokkaus;
