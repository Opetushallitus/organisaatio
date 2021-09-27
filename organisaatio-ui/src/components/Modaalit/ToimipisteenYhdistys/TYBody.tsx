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

const organisaatioSelectMapper = (organisaatiot: Organisaatio[], language) =>
    organisaatiot.map((o: Organisaatio) => ({
        value: `${o.oid}`,
        label: o.nimi[language],
    }));

export default function TYBody({ yhdistaOrganisaatio, handleChange, organisaatio }: TYProps) {
    const { i18n, language } = useContext(LanguageContext);
    const { organisaatiot, organisaatiotLoading, organisaatiotError } = useOrganisaatioHaku({
        organisaatioTyyppi: 'organisaatiotyyppi_07',
    });

    if (organisaatiotLoading || organisaatiotError) {
        return <Spin />;
    }
    const newParent = organisaatiot.find((o) => o.oid === yhdistaOrganisaatio.newParent);
    const newParentNAme = newParent && newParent.nimi[language];
    const parentOrganisaatiot = organisaatioSelectMapper(organisaatiot, language);
    console.log('inselect', organisaatiot, parentOrganisaatiot);
    console.log('inselect', organisaatiot[0], parentOrganisaatiot[0]);
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
                    <label>{i18n.translate('TOIMIPISTEEN_YHDISTYS_PVM')}</label>
                    <DatePickerInput
                        value={yhdistaOrganisaatio.date}
                        onChange={(e) => {
                            handleChange(yhdistaOrganisaatio);
                        }}
                    />
                </div>
                <div className={styles.BodyKentta}>
                    <label>{i18n.translate('TOIMIPISTEEN_YHDISTYS_TOINEN_ORGANISAATIO')}</label>
                    <Select
                        value={{
                            label: newParentNAme || '',
                            value: yhdistaOrganisaatio.newParent || '',
                        }}
                        options={parentOrganisaatiot.filter(
                            (o) => ![organisaatio.oid, organisaatio.parentOid].includes(o.value)
                        )}
                        onChange={(option) => {
                            if (option) handleChange({ ...yhdistaOrganisaatio, newParent: (option as iOption).value });
                        }}
                    />
                </div>
            </div>
        </div>
    );
}
