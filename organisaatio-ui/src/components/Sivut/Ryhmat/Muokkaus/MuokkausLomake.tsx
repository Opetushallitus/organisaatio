import * as React from 'react';
import styles from './MuokkausLomake.module.css';
import Icon from '@iconify/react';
import homeIcon from '@iconify/icons-fa-solid/home';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import Select from '@opetushallitus/virkailija-ui-components/Select';
import { ActionMeta, ValueType } from 'react-select';
import { TranslatedInputBind, Ryhma, SelectOptionType } from '../../../../types/types';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import PohjaSivu from '../../PohjaSivu/PohjaSivu';
import { useContext } from 'react';
import { KoodistoContext, LanguageContext } from '../../../../contexts/contexts';
import { mapKoodistoOptions, mapLocalizedKoodiToLang, mapValuesToSelect } from '../../../mappers';

export type MuokkausLomakeProps = {
    onUusi: boolean;
    ryhma: Ryhma;
    nimiBinds: TranslatedInputBind[];
    kuvausBinds: TranslatedInputBind[];
    handleRyhmaSelectOnChange: (
        values: ValueType<SelectOptionType>[] | ValueType<SelectOptionType> | undefined,
        action: ActionMeta<SelectOptionType>
    ) => void;
    handlePeruuta: () => void;
    handlePassivoi: () => void;
    handlePoista: () => void;
    handleTallenna: () => void;
};

export type InputBindProps = {
    bind: TranslatedInputBind;
};

export const InputRivi = ({ bind }: InputBindProps) => {
    const { i18n } = useContext(LanguageContext);
    const { localizationKey, ...rest } = bind;
    return (
        <div className={styles.Rivi}>
            <label htmlFor={bind.name}>{i18n.translate(localizationKey)}</label>
            <div className={styles.PitkaInput}>
                <Input id={bind.name} {...rest} />
            </div>
        </div>
    );
};

const MuokkausLomake = ({
    onUusi,
    ryhma,
    nimiBinds,
    kuvausBinds,
    handleRyhmaSelectOnChange,
    handlePeruuta,
    handlePassivoi,
    handlePoista,
    handleTallenna,
}: MuokkausLomakeProps) => {
    const { i18n, language } = useContext(LanguageContext);
    const { ryhmaTyypitKoodisto, kayttoRyhmatKoodisto } = useContext(KoodistoContext);

    const ryhmaTyypitOptions = mapKoodistoOptions(ryhmaTyypitKoodisto.koodit(), language);
    const kayttoRyhmatOptions = mapKoodistoOptions(kayttoRyhmatKoodisto.koodit(), language);

    const kayttoRyhmat = mapValuesToSelect(ryhma.kayttoryhmat, kayttoRyhmatOptions);
    const ryhmaTyypit = mapValuesToSelect(ryhma.ryhmatyypit, ryhmaTyypitOptions);
    return (
        <PohjaSivu>
            <div className={styles.YlaBanneri}>
                <div>
                    <a href="/organisaatio/ryhmat">
                        <Icon icon={homeIcon} />
                    </a>
                </div>
                <div>
                    <a href="/organisaatio/ryhmat">{i18n.translate('KAIKKI_RYHMAT')}</a>
                </div>
            </div>
            <div className={styles.PaaKehys}>
                <div className={styles.ValiContainer}>
                    <div className={styles.ValiOtsikko}>
                        <h3>{i18n.translate('RYHMA')}</h3>
                        <h1>{mapLocalizedKoodiToLang(language, 'nimi', ryhma)}</h1>
                    </div>
                </div>
                <div className={styles.PaaOsio}>
                    <div className={styles.OtsikkoRivi}>
                        <div className={styles.Otsikko}>
                            <h3>{i18n.translate('RYHMAN_TIEDOT_OTSIKKO')}</h3>
                        </div>
                    </div>
                    <div className={styles.OidRivi}>
                        <span className={styles.AvainKevyestiBoldattu}>{i18n.translate('OID')}</span>
                        <span className={styles.ReadOnly}>{ryhma.oid || ''}</span>
                    </div>
                    <div className={styles.Rivi}>
                        <div className={styles.Kentta}>
                            <label>{i18n.translate('RYHMAN_NIMI')}</label>
                            {nimiBinds.map((bind) => (
                                <InputRivi key={bind.name} bind={bind} />
                            ))}
                        </div>
                    </div>
                    <div className={styles.Rivi}>
                        <div className={styles.Kentta}>
                            <label>{i18n.translate('RYHMAN_KUVAUS')}</label>
                            {kuvausBinds.map((bind) => (
                                <InputRivi key={bind.name} bind={bind} />
                            ))}
                        </div>
                    </div>
                    <div className={styles.Rivi}>
                        <div className={styles.Kentta}>
                            <label>{i18n.translate('RYHMAN_TYYPPI')}</label>
                            <Select
                                isDisabled={ryhma.status === 'PASSIIVINEN'}
                                name={'ryhmatyypit'}
                                isMulti
                                value={ryhmaTyypit}
                                options={ryhmaTyypitOptions}
                                onChange={handleRyhmaSelectOnChange}
                            />
                        </div>
                    </div>
                    <div className={styles.Rivi}>
                        <div className={styles.Kentta}>
                            <label>{i18n.translate('RYHMAN_KAYTTOTARKOITUS')}</label>
                            <Select
                                isDisabled={ryhma.status === 'PASSIIVINEN'}
                                name={'kayttoryhmat'}
                                isMulti
                                value={kayttoRyhmat}
                                options={kayttoRyhmatOptions}
                                onChange={handleRyhmaSelectOnChange}
                            />
                        </div>
                    </div>
                    {!onUusi && (
                        <div className={styles.AlinRivi}>
                            <Button name="passivoibutton" variant="outlined" onClick={handlePassivoi}>
                                {ryhma.status === 'AKTIIVINEN'
                                    ? i18n.translate('PASSIVOI_RYHMA')
                                    : i18n.translate('AKTIVOI_RYHMA')}
                            </Button>
                            ,
                            <Button name="poistabutton" variant="outlined" onClick={handlePoista}>
                                {i18n.translate('POISTA_RYHMA')}
                            </Button>
                        </div>
                    )}
                </div>
            </div>
            <div className={styles.AlaBanneri}>
                <div>
                    <Button
                        name="peruutabutton"
                        variant="outlined"
                        className={styles.Versionappula}
                        onClick={handlePeruuta}
                    >
                        {i18n.translate('SULJE_TIEDOT')}
                    </Button>
                    <Button
                        disabled={ryhma.status === 'PASSIIVINEN'}
                        name="tallennabutton"
                        className={styles.Versionappula}
                        onClick={handleTallenna}
                    >
                        {i18n.translate('TALLENNA')}
                    </Button>
                </div>
            </div>
        </PohjaSivu>
    );
};

export default MuokkausLomake;
