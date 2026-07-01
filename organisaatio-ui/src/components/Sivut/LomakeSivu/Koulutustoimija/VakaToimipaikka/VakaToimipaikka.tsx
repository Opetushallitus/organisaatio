import * as React from 'react';
import { AvainKevyestiBoldattu, ReadOnly, Rivi, Ruudukko, UloinKehys } from '../../LomakeFields/LomakeFields';
import { KoodistoSelectOption, Perustiedot, VakaPainotus, VakaToimipaikkaTiedot } from '../../../../../types/types';
import { Control, Controller } from 'react-hook-form';
import Checkbox from '@opetushallitus/virkailija-ui-components/Checkbox';
import { format } from 'date-fns';
import { UI_DATE_FORMAT } from '../../../../../tools/dateUtils';
const ShowPair = ({ label, value, first }: { label: string; value: string; first: boolean }) => {
    return (
        <>
            {first ? <AvainKevyestiBoldattu label={label} /> : <div></div>}
            <ReadOnly value={value} />
        </>
    );
};
const painotus = ({ data, label }: { data: VakaPainotus[]; label: string }) => {
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
                    value={`${a.painotus.label} ${format(a.alkupvm, UI_DATE_FORMAT)}${
                        a.loppupvm ? ' - ' + format(a.loppupvm, UI_DATE_FORMAT) : ''
                    }`}
                />
            );
        });
};
const jarjestamisMuoto = ({ data, label }: { data: KoodistoSelectOption[]; label: string }) => {
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
