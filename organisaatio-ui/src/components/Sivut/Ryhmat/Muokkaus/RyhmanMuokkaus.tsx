import * as React from 'react';
import { useContext, useEffect, useState } from 'react';
import { RouteComponentProps } from 'react-router-dom';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';
import { deleteRyhma, getRyhma, postRyhma, putRyhma } from '../../../../api/ryhma';
import { NewRyhma, Ryhma } from '../../../../types/types';
import MuokkausLomake from './MuokkausLomake';
import { ROOT_OID } from '../../../../contexts/constants';
import { FieldValues, SubmitHandler } from 'react-hook-form';
import { LanguageContext } from '../../../../contexts/LanguageContext';
import { formatUiDateStrToApi } from '../../../../tools/mappers';

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
            setRyhma({ ...(emptyRyhma as Ryhma) });
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
                ryhmatyypit: ryhmatyypit.map((a) => `${a.value}#${a.versio}`),
                kayttoryhmat: kayttoryhmat.map((a) => `${a.value}#${a.versio}`),
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
