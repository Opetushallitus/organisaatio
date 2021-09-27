import React, { useContext } from 'react';
import { LanguageContext } from '../../../contexts/contexts';
import styles from './ToimipisteenYhdistys.module.css';
import DatePickerInput from '@opetushallitus/virkailija-ui-components/DatePickerInput';
import Checkbox from '@opetushallitus/virkailija-ui-components/Checkbox';
import Select from '@opetushallitus/virkailija-ui-components/Select';
import { iOption, Organisaatio, YhdistaOrganisaatioon } from '../../../types/types';
import { useOrganisaatioHaku } from '../../../api/organisaatio';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';

type TYProps = {
    yhdistaOrganisaatio: YhdistaOrganisaatioon;
    handleChange: (props: YhdistaOrganisaatioon) => void;
    organisaatio: Organisaatio;
};
const mapOrganisaatioToSelect = (o: Organisaatio | undefined, language: string) => {
    if (o)
        return {
            value: `${o.oid}`,
            label: `${o.nimi[language]} ${o.oid}`,
        };
    else return { value: '', label: '' };
};
const organisaatioSelectMapper = (organisaatiot: Organisaatio[], language: string) =>
    organisaatiot.map((o: Organisaatio) => mapOrganisaatioToSelect(o, language));

export default function TYBody({ yhdistaOrganisaatio, handleChange, organisaatio }: TYProps) {
    const { i18n, language } = useContext(LanguageContext);
    const { organisaatiot, organisaatiotLoading, organisaatiotError } = useOrganisaatioHaku({
        organisaatioTyyppi: 'organisaatiotyyppi_01',
    });

    if (organisaatiotLoading || organisaatiotError) {
        return <Spin />;
    }
    const newParent = organisaatiot.find((o) => o.oid === yhdistaOrganisaatio.newParent);
    const parentOrganisaatiot = organisaatioSelectMapper(organisaatiot, language);
    return (
        <div className={styles.BodyKehys}>
            <div className={styles.BodyRivi}>
                <div className={styles.BodyKentta}>
                    <label>{i18n.translate('TOIMIPISTEEN_YHDISTYS_SULAUTA')}</label>
                    <Checkbox
                        type="checkbox"
                        checked={yhdistaOrganisaatio.merge}
                        onChange={(e: React.ChangeEvent<HTMLInputElement>) => {
                            handleChange({ ...yhdistaOrganisaatio, merge: !!e.target.value });
                        }}
                    />
                </div>
            </div>
            <div className={styles.BodyRivi}>
                <div className={styles.BodyKentta}>
                    <label>{i18n.translate('TOIMIPISTEEN_YHDISTYS_TOINEN_ORGANISAATIO')}</label>
                    <Select
                        menuPortalTarget={document.body}
                        value={mapOrganisaatioToSelect(newParent, language)}
                        options={parentOrganisaatiot.filter(
                            (o) => ![organisaatio.oid, organisaatio.parentOid].includes(o.value)
                        )}
                        onChange={(option) => {
                            if (option) handleChange({ ...yhdistaOrganisaatio, newParent: (option as iOption).value });
                        }}
                    />
                </div>
                <div className={styles.BodyKentta}>
                    <label>{i18n.translate('TOIMIPISTEEN_YHDISTYS_PVM')}</label>
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
