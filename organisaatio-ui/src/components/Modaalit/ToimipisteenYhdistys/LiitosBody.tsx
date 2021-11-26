import React, { useContext } from 'react';
import { LanguageContext } from '../../../contexts/contexts';
import DatePickerInput from '@opetushallitus/virkailija-ui-components/DatePickerInput';
import Select from '@opetushallitus/virkailija-ui-components/Select';
import { LiitaOrganisaatioon, Option, ResolvedRakenne, UiOrganisaatioBase } from '../../../types/types';
import { useOrganisaatioHaku } from '../../../api/organisaatio';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';
import { warning } from '../../Notification/Notification';
import { mapOrganisaatioToSelect, organisaatioSelectMapper } from '../../../tools/organisaatio';
import { BodyKehys, BodyKentta, BodyRivi } from '../ModalFields/ModalFields';

type TSProps = {
    liitaOrganisaatio: LiitaOrganisaatioon;
    handleChange: (props: LiitaOrganisaatioon) => void;
    organisaatioBase: UiOrganisaatioBase;
    organisaatioRakenne: ResolvedRakenne;
    targetType: string;
    labels: { otherOrg: string; liitosPvm: string };
};

export default function LiitosBody({
    liitaOrganisaatio,
    handleChange,
    organisaatioBase,
    organisaatioRakenne,
    targetType,
    labels,
}: TSProps) {
    const { language } = useContext(LanguageContext);

    const { organisaatiot, organisaatiotLoading, organisaatiotError } = useOrganisaatioHaku({
        organisaatiotyyppi: targetType,
    });

    if (organisaatiotLoading || organisaatiotError) {
        return <Spin />;
    }
    if (!organisaatioRakenne || !organisaatioRakenne.mergeTargetType) warning({ message: 'PARENT_TYPE_NOT_AVAILABLE' });
    const newParent = organisaatiot.find((o) => o.oid === liitaOrganisaatio.newParent?.oid);
    const parentOrganisaatiot = organisaatioSelectMapper(organisaatiot, language);
    return (
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
                    />
                </BodyKentta>
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
    );
}
