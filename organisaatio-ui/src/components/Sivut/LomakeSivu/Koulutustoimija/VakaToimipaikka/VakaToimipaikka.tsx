import * as React from 'react';
import { AvainKevyestiBoldattu, ReadOnly, Rivi, Ruudukko, UloinKehys } from '../../LomakeFields/LomakeFields';
import { Perustiedot, VakaToimipaikkaTiedot } from '../../../../../types/types';
import moment from 'moment';
import { Control, Controller } from 'react-hook-form';
import Checkbox from '@opetushallitus/virkailija-ui-components/Checkbox';
const ShowPair = ({ label, value, first }: { label: string; value: string; first: boolean }) => {
    return (
        <>
            {first ? <AvainKevyestiBoldattu label={label} /> : <div></div>}
            <ReadOnly value={value} />
        </>
    );
};
const painotus = ({ data, label }) => {
    return data
        .sort((a, b) => {
            return a.alkupvm > b.alkupvm ? 1 : -1;
        })
        .map((a, index) => {
            return (
                <ShowPair
                    key={a.painotus.arvo}
                    first={index === 0}
                    label={label}
                    value={`${a.painotus.label} ${moment(a.alkupvm).format('D.M.yyyy')}${
                        a.loppupvm ? ' - ' + moment(a.loppupvm).format('D.M.yyyy') : ''
                    }`}
                />
            );
        });
};
const jarjestamisMuoto = ({ data, label }) => {
    return data
        .sort((a, b) => (a.label > b.label ? 1 : -1))
        .map((a, index) => {
            return <ShowPair key={a.arvo} first={index === 0} label={label} value={`${a.label}`} />;
        });
};
export default function VakaToimipaikka({
    vaka,
    control,
}: {
    vaka: VakaToimipaikkaTiedot;
    control: Control<Perustiedot>;
}) {
    return (
        <UloinKehys>
            <Rivi>
                <Ruudukko>
                    <AvainKevyestiBoldattu label={'VAKA_TOIMINTAMUOTO'} />
                    <ReadOnly value={vaka.toimintamuoto.label} />
                    <AvainKevyestiBoldattu label={'VAKA_JARJESTELMA'} />
                    <ReadOnly value={vaka.kasvatusopillinenJarjestelma.label} />
                    {painotus({ label: 'VAKA_PAINOTUS', data: vaka.varhaiskasvatuksenToiminnallinenpainotukset })}
                    <AvainKevyestiBoldattu label={'VAKA_PAIKAT'} />
                    <ReadOnly value={vaka.paikkojenLukumaara} />
                    {jarjestamisMuoto({
                        data: vaka.varhaiskasvatuksenJarjestamismuodot,
                        label: 'VAKA_JARJESTAMISMUOTO',
                    })}
                    {painotus({ label: 'VAKA_KIELIPAINOTUKSET', data: vaka.varhaiskasvatuksenKielipainotukset })}
                    <AvainKevyestiBoldattu label={'VAKA_PIILOTETTU'} />
                    <Controller
                        control={control}
                        name="piilotettu"
                        render={({ field: { value, ref, ...rest } }) => (
                            <Checkbox {...rest} checked={value} inputRef={ref} />
                        )}
                    />
                </Ruudukko>
            </Rivi>
        </UloinKehys>
    );
}
