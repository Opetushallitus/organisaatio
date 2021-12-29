import * as React from 'react';
import { useContext, useState } from 'react';
import styles from './YhteystietoLomake.module.css';
import type { SupportedKieli, Yhteystiedot } from '../../../../../types/types';
import { enAltSchema, fiAltSchema, svAltSchema } from '../../../../../ValidationSchemas/YhteystietoLomakeSchema';
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
import { checkHasSomeValueByKieli, mapVisibleKieletFromOpetuskielet } from '../../../../../tools/mappers';
import { Rivi, UloinKehys } from '../../LomakeFields/LomakeFields';
import { useFormState } from 'react-hook-form';
import { LanguageContext } from '../../../../../contexts/LanguageContext';

export type Props = {
    opetusKielet: string[];
    setYhteystiedotValue: UseFormSetValue<Yhteystiedot>;
    yhteystiedot?: Yhteystiedot[];
    hasValidationErrors: boolean;
    formRegister: UseFormRegister<Yhteystiedot>;
    formControl: Control<Yhteystiedot>;
    watch: UseFormWatch<Yhteystiedot>;
    getYhteystiedotValues: UseFormGetValues<Yhteystiedot>;
    readOnly?: boolean;
};

const kaikkiOpetuskielet: SupportedKieli[] = ['fi', 'sv', 'en'];

const validationSchemas = {
    fi: fiAltSchema,
    sv: svAltSchema,
    en: enAltSchema,
};

const YhteystietoLomake = ({
    opetusKielet,
    formRegister,
    hasValidationErrors,
    watch,
    formControl,
    setYhteystiedotValue,
    getYhteystiedotValues,
    readOnly,
}: Props): React.ReactElement => {
    const { i18n } = useContext(LanguageContext);
    const [naytaMuutKielet, setNaytaMuutKielet] = useState(false);

    const osoitteetOnEri = watch('osoitteetOnEri') || false;
    const handleShowClick = () => {
        setNaytaMuutKielet(!naytaMuutKielet);
    };

    const visibleKieletByOpetuskielet = mapVisibleKieletFromOpetuskielet(opetusKielet);
    const haseSomeValueKielet = kaikkiOpetuskielet.filter((kieli) =>
        checkHasSomeValueByKieli(getYhteystiedotValues(kieli))
    );
    const visibleKielet = Array.from(new Set(visibleKieletByOpetuskielet.concat(haseSomeValueKielet)));
    const { isSubmitted } = useFormState({ control: formControl });
    const yhteystiedotValues = getYhteystiedotValues();
    return (
        <UloinKehys>
            <Rivi>
                <div className={styles.PiilotaNappiKentta}>
                    <Button onClick={handleShowClick}>
                        {naytaMuutKielet
                            ? i18n.translate('YHTEYSTIEDOT_PIILOTA_MUUT_KIELET')
                            : i18n.translate('YHTEYSTIEDOT_NAYTA_MUUT_KIELET')}
                    </Button>
                </div>
                <Checkbox disabled={readOnly} {...formRegister('osoitteetOnEri')} checked={osoitteetOnEri}>
                    {i18n.translate('YHTEYSTIEDOT_POSTIOSOITE_ON_ERI_KUIN_KAYNTIOSOITE')}
                </Checkbox>
            </Rivi>
            <div className={styles.KortitContainer}>
                {visibleKielet.map((kieli, index) => (
                    <YhteystietoKortti
                        readOnly={readOnly}
                        key={kieli}
                        yhteystiedotRegister={formRegister}
                        osoitteetOnEri={osoitteetOnEri}
                        kieli={kieli}
                        setYhteystiedotValue={setYhteystiedotValue}
                        validationErrors={
                            isSubmitted && hasValidationErrors
                                ? validationSchemas[kieli].validate(yhteystiedotValues)
                                : { value: yhteystiedotValues }
                        }
                        formControl={formControl}
                    />
                ))}
                {naytaMuutKielet &&
                    kaikkiOpetuskielet
                        .filter((kieli) => !visibleKielet.includes(kieli))
                        .map((kieli) => (
                            <YhteystietoKortti
                                readOnly={readOnly}
                                key={kieli}
                                osoitteetOnEri={osoitteetOnEri}
                                kieli={kieli}
                                yhteystiedotRegister={formRegister}
                                setYhteystiedotValue={setYhteystiedotValue}
                                validationErrors={
                                    isSubmitted && hasValidationErrors
                                        ? validationSchemas[kieli].validate(yhteystiedotValues)
                                        : { value: yhteystiedotValues }
                                }
                                formControl={formControl}
                            />
                        ))}
            </div>
        </UloinKehys>
    );
};

export default YhteystietoLomake;
