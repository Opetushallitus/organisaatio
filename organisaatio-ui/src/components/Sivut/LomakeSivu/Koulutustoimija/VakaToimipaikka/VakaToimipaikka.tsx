import * as React from 'react';
import { AvainKevyestiBoldattu, ReadOnly, Rivi, Ruudukko, UloinKehys } from '../../LomakeFields/LomakeFields';
import { VakaToimipaikkaTiedot } from '../../../../../types/types';
import moment from 'moment';

export default function VakaToimipaikka({ vaka }: { vaka: VakaToimipaikkaTiedot }) {
    console.log(vaka);

    const painotus = ({ data, label }) => {
        return data
            .sort((a, b) => {
                return a.alkupvm > b.alkupvm ? 1 : -1;
            })
            .map((a, index) => {
                return (
                    <>
                        {index === 0 ? <AvainKevyestiBoldattu label={label} /> : <div></div>}
                        <ReadOnly
                            value={`${a.painotus.label} ${moment(a.alkupvm).format('D.M.yyyy')}${
                                a.loppupvm ? ' - ' + moment(a.loppupvm).format('D.M.yyyy') : ''
                            }`}
                        />
                    </>
                );
            });
    };
    const jarjestamisMuoto = ({ data, label }) => {
        return data
            .sort((a, b) => (a.label > b.label ? 1 : -1))
            .map((a, index) => {
                return (
                    <>
                        {index === 0 ? <AvainKevyestiBoldattu label={'VAKA_JARJESTAMISMUOTO'} /> : <div></div>}
                        <ReadOnly value={`${a.label}`} />
                    </>
                );
            });
    };
    return (
        <UloinKehys>
            <Rivi>
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
                    </Ruudukko>
                </Rivi>
            </Rivi>
        </UloinKehys>
    );
}
