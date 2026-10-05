import * as React from 'react';
import { useState } from 'react';
import styles from './YhteystietoLomake.module.css';
import type { Language, Yhteystiedot } from '../../../../../types/types';
import { useFormState, type Control, type UseFormGetValues, type UseFormRegister, type UseFormSetValue, type UseFormWatch } from 'react-hook-form';
import { YhteystietoKortti } from './YhteystietoKortti';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import Checkbox from '@opetushallitus/virkailija-ui-components/Checkbox';
import { checkHasSomeValueByKieli, mapVisibleKieletFromOpetuskielet } from '../../../../../tools/mappers';
import { Rivi, UloinKehys } from '../../LomakeFields/LomakeFields';
import { useAtom } from 'jotai';
import { languageAtom } from '../../../../../api/lokalisaatio';
import { fiAltSchema, svAltSchema, enAltSchema } from '../../../../../ValidationSchemas/YhteystietoLomakeSchema';

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
    isYtj: boolean;
};
const kaikkiOpetuskielet: Language[] = ['fi', 'sv', 'en'];
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
    isYtj,
}: Props): React.ReactElement => {
    const [i18n] = useAtom(languageAtom);
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
                <Checkbox
                    disabled={readOnly}
                    {...(!readOnly && formRegister('osoitteetOnEri'))}
                    checked={osoitteetOnEri}
                >
                    {i18n.translate('YHTEYSTIEDOT_POSTIOSOITE_ON_ERI_KUIN_KAYNTIOSOITE')}
                </Checkbox>
            </Rivi>
            <div className={styles.KortitContainer}>
                {visibleKielet.map((kieli: Language) => (
                    <YhteystietoKortti
                        isYtj={isYtj}
                        readOnly={readOnly}
                        key={kieli}
                        yhteystiedotRegister={formRegister}
                        osoitteetOnEri={osoitteetOnEri}
                        kieli={kieli}
                        setYhteystiedotValue={setYhteystiedotValue}
                        validationErrors={
                            isSubmitted && hasValidationErrors
                                ? validationSchemas[kieli].validate(
                                    yhteystiedotValues,
                                    { abortEarly: false }
                                )
                                : {
                                    value: yhteystiedotValues,
                                    error: undefined,
                                }
                        }
                        formControl={formControl}
                    />
                ))}
                {naytaMuutKielet &&
                    kaikkiOpetuskielet
                        .filter((kieli: Language) => !visibleKielet.includes(kieli))
                        .map((kieli: Language) => (
                            <YhteystietoKortti
                                isYtj={isYtj}
                                readOnly={readOnly}
                                key={kieli}
                                yhteystiedotRegister={formRegister}
                                osoitteetOnEri={osoitteetOnEri}
                                kieli={kieli}
                                setYhteystiedotValue={setYhteystiedotValue}
                                validationErrors={
                                    isSubmitted && hasValidationErrors
                                        ? validationSchemas[kieli].validate(yhteystiedotValues)
                                        : { value: yhteystiedotValues, error: undefined }
                                }
                                formControl={formControl}
                            />
                        ))}
            </div>
        </UloinKehys>
    );
};

export default YhteystietoLomake;
