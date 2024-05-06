import React from 'react';
import DatePickerInput from '@opetushallitus/virkailija-ui-components/DatePickerInput';
import Select from 'react-select';
import { LiitaOrganisaatioon, Option, UiOrganisaatioBase } from '../../../types/types';
import { useOrganisaatioHaku } from '../../../api/organisaatio';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';
import { mapOrganisaatioToSelect, organisaatioSelectMapper } from '../../../tools/organisaatio';
import { BodyKehys, BodyKentta, BodyRivi } from '../ModalFields/ModalFields';
import { useAtom } from 'jotai';
import { casMeAtom } from '../../../api/kayttooikeus';
import LiitosDescription from './LiitosDescription';

type TSProps = {
    liitaOrganisaatio: LiitaOrganisaatioon;
    handleChange: (props: LiitaOrganisaatioon) => void;
    organisaatioBase: UiOrganisaatioBase;
    targetType: string;
    labels: { otherOrg: string; liitosPvm: string };
};

export default function LiitosBody({ liitaOrganisaatio, handleChange, organisaatioBase, targetType, labels }: TSProps) {
    const [{ lang: language }] = useAtom(casMeAtom);

    const { organisaatiot, organisaatiotLoading, organisaatiotError } = useOrganisaatioHaku({
        organisaatiotyyppi: targetType,
    });

    if (organisaatiotLoading || organisaatiotError) {
        return <Spin />;
    }
    const newParent = organisaatiot.find((o) => o.oid === liitaOrganisaatio.newParent?.oid);
    const parentOrganisaatiot = organisaatioSelectMapper(organisaatiot, language);
    return (
        <>
            <LiitosDescription sourceOid={organisaatioBase.oid} />
            <BodyKehys>
                <BodyRivi>
                    <BodyKentta label={labels.otherOrg}>
                        <Select
                            menuPortalTarget={document.body}
                            value={mapOrganisaatioToSelect(newParent, language)}
                            options={parentOrganisaatiot
                                .filter((o) => ![organisaatioBase.oid, organisaatioBase.parentOid].includes(o.value))
                                .sort((a, b) => a.label.localeCompare(b.label))}
                            onChange={(option) => {
                                if (option)
                                    handleChange({
                                        ...liitaOrganisaatio,
                                        newParent: organisaatiot.find((a) => {
                                            return (option as Option).value === a.oid;
                                        }),
                                    });
                            }}
                            styles={{ menuPortal: (base) => ({ ...base, zIndex: 9999 }) }}
                        />
                    </BodyKentta>
                </BodyRivi>
                <BodyRivi>
                    <BodyKentta label={labels.liitosPvm}>
                        <DatePickerInput
                            value={liitaOrganisaatio.date}
                            onChange={(e) => {
                                handleChange({ ...liitaOrganisaatio, date: e });
                            }}
                        />
                    </BodyKentta>
                </BodyRivi>
            </BodyKehys>
        </>
    );
}
