import * as React from 'react';
import styles from './YhteystietoLomake.module.css';
import { useContext, useState } from 'react';
import type { KoodistoSelectOption, SupportedKieli, Yhteystiedot } from '../../../../../types/types';
import { FieldErrors } from 'react-hook-form/dist/types/errors';
import {
    Control,
    UseFormGetValues,
    UseFormRegister,
    UseFormSetValue,
    UseFormWatch,
} from 'react-hook-form/dist/types/form';
import { YhteystietoKortti } from './YhteystietoKortti';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import Checkbox from '@opetushallitus/virkailija-ui-components/Checkbox';
import { LanguageContext } from '../../../../../contexts/contexts';
import {
    checkHasSomeValueByKieli,
    mapVisibleKieletFromOpetuskielet,
    mergeKieliArrays,
} from '../../../../../tools/mappers';

export type Props = {
    kielet: KoodistoSelectOption[];
    setYhteystiedotValue: UseFormSetValue<Yhteystiedot>;
    yhteystiedot?: Yhteystiedot[];
    validationErrors: FieldErrors<Yhteystiedot>;
    formRegister: UseFormRegister<Yhteystiedot>;
    formControl: Control<Yhteystiedot>;
    watch: UseFormWatch<Yhteystiedot>;
    getYhteystiedotValues: UseFormGetValues<Yhteystiedot>;
};

const kaikkiOpetuskielet: SupportedKieli[] = ['fi', 'sv', 'en'];

const YhteystietoLomake = ({
    kielet,
    formRegister,
    validationErrors,
    watch,
    formControl,
    setYhteystiedotValue,
    getYhteystiedotValues,
}: Props): React.ReactElement => {
    const { i18n } = useContext(LanguageContext);
    const [naytaMuutKielet, setNaytaMuutKielet] = useState(false);

    const osoitteetOnEri = watch('osoitteetOnEri') || false;
    const handleShowClick = () => {
        setNaytaMuutKielet(!naytaMuutKielet);
    };

    const visibleKieletByOpetuskielet = mapVisibleKieletFromOpetuskielet(kielet);
    const haseSomeValueKielet = kaikkiOpetuskielet.filter((kieli) =>
        checkHasSomeValueByKieli(getYhteystiedotValues(kieli), kieli)
    );
    const visibleKielet = mergeKieliArrays(visibleKieletByOpetuskielet, haseSomeValueKielet);
    return (
        <div className={styles.UloinKehys}>
            <div className={styles.Rivi}>
                <div className={styles.PiilotaNappiKentta}>
                    <Button onClick={handleShowClick}>
                        {naytaMuutKielet ? 'Piilota muun kieliset' : 'Näytä muun kieliset'}
                    </Button>
                </div>
                <Checkbox {...formRegister('osoitteetOnEri')} checked={osoitteetOnEri}>
                    {i18n.translate('YHTEYSTIEDOT_POSTIOSOITE_ON_ERI_KUIN_KAYNTIOSOITE')}
                </Checkbox>
            </div>
            <div className={styles.KortitContainer}>
                {visibleKielet.map((kieli, index) => (
                    <YhteystietoKortti
                        key={kieli}
                        isFirst={index === 0}
                        yhteystiedotRegister={formRegister}
                        osoitteetOnEri={osoitteetOnEri}
                        kieli={kieli}
                        setYhteystiedotValue={setYhteystiedotValue}
                        validationErrors={validationErrors}
                        formControl={formControl}
                    />
                ))}
                {naytaMuutKielet &&
                    kaikkiOpetuskielet
                        .filter((kieli) => !visibleKielet.includes(kieli))
                        .map((kieli) => (
                            <YhteystietoKortti
                                key={kieli}
                                isFirst={false}
                                osoitteetOnEri={osoitteetOnEri}
                                kieli={kieli}
                                yhteystiedotRegister={formRegister}
                                setYhteystiedotValue={setYhteystiedotValue}
                                validationErrors={validationErrors}
                                formControl={formControl}
                            />
                        ))}
            </div>
        </div>
    );
};

export default YhteystietoLomake;
