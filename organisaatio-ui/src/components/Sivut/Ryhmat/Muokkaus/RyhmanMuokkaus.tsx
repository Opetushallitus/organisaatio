import * as React from 'react';
import { useState } from 'react';
import { RouteComponentProps } from 'react-router-dom';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';
import { useEffect } from 'react';
import { getRyhma, deleteRyhma, putRyhma, postRyhma } from '../../../HttpRequests';
import { Ryhma } from '../../../../types/types';
import MuokkausLomake from './MuokkausLomake';
import { useContext } from 'react';
import { LanguageContext } from '../../../../contexts/contexts';
import { FieldValues, SubmitHandler } from 'react-hook-form';

const ROOT_OID = '1.2.246.562.10.00000000001'; // KOVAKOODATTU AINAKIN TOISTAISEKSI

export type RyhmanMuokausProps = {
    oid?: string;
};

const emptyRyhma: Ryhma = {
    nimi: {
        fi: '',
    },
    kuvaus2: {
        'kieli_fi#1': '',
    },
    parentOid: ROOT_OID,
    oid: null,
    ryhmatyypit: [],
    kayttoryhmat: [],
    status: 'AKTIIVINEN',
    tyypit: ['Ryhma'],
};

export const currentDateToStr = () => new Date().toISOString().split('T')[0];

const RyhmanMuokkaus = ({ match, history, isNew }: RouteComponentProps<RyhmanMuokausProps> & { isNew?: boolean }) => {
    const { i18n } = useContext(LanguageContext);
    const [ryhma, setRyhma] = useState<Ryhma>();
    const onUusi = isNew || history.location.pathname.includes('uusi');

    useEffect(() => {
        async function fetch(oid) {
            try {
                const ryhma = await getRyhma(oid);
                setRyhma(ryhma as Ryhma);
            } catch (error) {
                console.error('error fetching', error);
            }
        }
        if (match.params.oid && !onUusi) {
            fetch(match.params.oid);
        } else {
            setRyhma({ ...emptyRyhma });
        }
    }, [onUusi, match.params.oid]);

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
                ryhmatyypit: ryhmatyypit.map((rt) => rt.value),
                kayttoryhmat: kayttoryhmat.map((rt) => rt.value),
            };
            try {
                const { organisaatio: updatedRyhma } = onUusi ? await postRyhma(newRyhma) : await putRyhma(newRyhma);
                setRyhma(updatedRyhma as Ryhma);
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
