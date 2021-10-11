import React, { useContext } from 'react';
import { LanguageContext } from '../../../contexts/contexts';
import styles from './ToimipisteenYhdistys.module.css';
import DatePickerInput from '@opetushallitus/virkailija-ui-components/DatePickerInput';
import Select from '@opetushallitus/virkailija-ui-components/Select';
import { Option, Organisaatio, ResolvedRakenne, YhdistaOrganisaatioon } from '../../../types/types';
import { useOrganisaatioHaku } from '../../../api/organisaatio';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';
import { warning } from '../../Notification/Notification';
import { mapOrganisaatioToSelect, organisaatioSelectMapper } from '../../../tools/organisaatio';

type TYProps = {
    yhdistaOrganisaatio: YhdistaOrganisaatioon;
    handleChange: (props: YhdistaOrganisaatioon) => void;
    organisaatio: Organisaatio;
    organisaatioRakenne: ResolvedRakenne;
};

export default function TYBody({ yhdistaOrganisaatio, handleChange, organisaatio, organisaatioRakenne }: TYProps) {
    const { i18n, language } = useContext(LanguageContext);
    const targetType =
        organisaatioRakenne && organisaatioRakenne.mergeTargetType ? organisaatioRakenne.mergeTargetType[0] : undefined;
    const { organisaatiot, organisaatiotLoading, organisaatiotError } = useOrganisaatioHaku({
        organisaatioTyyppi: targetType,
    });

    if (organisaatiotLoading || organisaatiotError) {
        return <Spin />;
    }
    if (!organisaatioRakenne || !organisaatioRakenne.mergeTargetType) warning({ message: 'PARENT_TYPE_NOT_AVAILABLE' });
    const newParent = organisaatiot.find((o) => o.oid === yhdistaOrganisaatio.newParent?.oid);
    const parentOrganisaatiot = organisaatioSelectMapper(organisaatiot, language);
    return (
        <div className={styles.BodyKehys}>
            <div className={styles.BodyRivi}>
                <div className={styles.BodyKentta}>
                    <label>{i18n.translate('ORGANISAATIO_YHDISTYS_TOINEN_ORGANISAATIO')}</label>
                    <Select
                        menuPortalTarget={document.body}
                        value={mapOrganisaatioToSelect(newParent, language)}
                        options={parentOrganisaatiot
                            .filter((o) => ![organisaatio.oid, organisaatio.parentOid].includes(o.value))
                            .sort((a, b) => a.label.localeCompare(b.label))}
                        onChange={(option) => {
                            if (option)
                                handleChange({
                                    ...yhdistaOrganisaatio,
                                    newParent: organisaatiot.find((a) => {
                                        return (option as Option).value === a.oid;
                                    }),
                                });
                        }}
                    />
                </div>
                <div className={styles.BodyKentta}>
                    <label>{i18n.translate('ORGANISAATIO_YHDISTYS_PVM')}</label>
                    <DatePickerInput
                        value={yhdistaOrganisaatio.date}
                        onChange={(e) => {
                            handleChange(yhdistaOrganisaatio);
                        }}
                    />
                </div>
            </div>
        </div>
    );
}
