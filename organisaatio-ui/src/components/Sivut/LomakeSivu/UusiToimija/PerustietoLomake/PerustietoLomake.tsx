import * as React from 'react';
import { useContext, useState } from 'react';
import styles from './PerustietoLomake.module.css';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import CheckboxGroup from '@opetushallitus/virkailija-ui-components/CheckboxGroup';
import Select from '@opetushallitus/virkailija-ui-components/Select';
import { KoodistoContext, LanguageContext } from '../../../../../contexts/contexts';
import RadioGroup from '@opetushallitus/virkailija-ui-components/RadioGroup';
import DatePickerInput from '@opetushallitus/virkailija-ui-components/DatePickerInput';
import {FieldErrors} from "react-hook-form/dist/types/errors";
import {FieldValues} from "react-hook-form/dist/types/fields";
import {Control, UseFormRegister, UseFormWatch} from "react-hook-form/dist/types/form";
import {Controller} from "react-hook-form";
import ToimipisteenNimenmuutosModaali
    from "../../../../Modaalit/ToimipisteenNimenmuutos/ToimipisteenNimenmuutosModaali";

type OrganisaatioProps = {
    validationErrors: FieldErrors<FieldValues>;
    formRegister: UseFormRegister<FieldValues>;
    formControl: Control<FieldValues>;
    handleJatka: () => void;
    handleNimiUpdate: (nimi: Nimi) => void;
    watchPerustiedot: UseFormWatch<FieldValues>;
};

type Nimi = {
    fi: string;
    sv: string;
    en: string;
    alkuPvm?: string;
};

// TODO optionsmapper ja paranna logiikkaa
export default function PerustietoLomake(props: OrganisaatioProps) {
    const { i18n, language } = useContext(LanguageContext);
    const { handleJatka, validationErrors, formControl, formRegister, handleNimiUpdate,  watchPerustiedot } = props;
    const {
        kuntaKoodisto,
        organisaatioTyypitKoodisto,
        maatJaValtiotKoodisto,
        oppilaitoksenOpetuskieletKoodisto,
    } = useContext(KoodistoContext);
    const [nimenmuutosModaaliAuki, setNimenmuutosModaaliAuki] = useState<boolean>(false);
    const [onYunnus, setOnYtunnus] = useState<boolean>(true);

    const fi = watchPerustiedot('nimi.fi');
    const sv = watchPerustiedot('nimi.sv');
    const en = watchPerustiedot('nimi.en');
    const nimi = {fi, sv, en};
    console.log('ve', validationErrors);
    return (
        <div className={styles.UloinKehys}>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <RadioGroup
                        value={onYunnus.toString()}
                        options={[
                            { value: 'true', label: i18n.translate('PERUSTIETO_ON_YTUNNUS') },
                            { value: 'false', label: i18n.translate('PERUSTIETO_EI_YTUNNUS') },
                        ]}
                        onChange={(e) => setOnYtunnus(!onYunnus)}
                    />
                </div>
            </div>
            {onYunnus && (
                <div className={styles.Rivi}>
                    <div className={styles.Kentta}>
                        <label>Y-tunnus</label>
                        <Input
                            error={!!validationErrors['ytunnus']}
                            id={'ytunnus'}
                            {...formRegister('ytunnus')}
                            defaultValue={''}
                        />
                    </div>
                    <Button className={styles.Nappi} variant="outlined">
                        {i18n.translate('BUTTON_HAE_YTJ_TIEDOT')}
                    </Button>
                </div>
            )}
            <div className={styles.Rivi}>
                <div className={styles.Ruudukko}>
                    {onYunnus && [
                        <span key="PERUSTIETO_ORGANISAATIO_TYYPPI" className={styles.AvainKevyestiBoldattu}>
                            {i18n.translate('PERUSTIETO_ORGANISAATIO_TYYPPI')}
                        </span>,
                        <span key="PERUSTIETO_ORGANISAATIO_TODO" className={styles.ReadOnly}>{i18n.translate('PERUSTIETO_TODO')}</span>,
                    ]}
                    <span className={styles.AvainKevyestiBoldattu}>
                        {i18n.translate('PERUSTIETO_ORGANISAATION_NIMI')}
                    </span>
                    <span className={styles.ReadOnly}>
                        {(nimi[language] ||
                            nimi['fi'] ||
                            nimi['sv'] ||
                            nimi['en'])}
                    </span>
                </div>
                <div className={styles.Kentta}>
                    <Button className={styles.Nappi} variant="outlined" onClick={() => setNimenmuutosModaaliAuki(true)}>
                        {i18n.translate('BUTTON_MUOKKAA_ORGANISAATION_NIMEA')}
                    </Button>
                </div>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>{i18n.translate('PERUSTIETO_ORGANISAATIOTYYPPI')}</label>
                        <Controller
                            control={formControl}
                            name={'tyypit'}
                            defaultValue={[]}
                            render={({ field: { ref, ...rest } }) => (
                                <CheckboxGroup {...rest} options={organisaatioTyypitKoodisto.selectOptions()} />
                            )}
                        />
                </div>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>{i18n.translate('PERUSTIETO_PERUSTAMISPAIVA')}</label>
                    <Controller
                        control={formControl}
                        name={'alkuPvm'}
                        render={({ field: { ref, ...rest } }) => (
                            <DatePickerInput error={!!validationErrors['alkuPvm']} {...rest}/>
                        )}
                    />
                </div>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>{i18n.translate('PERUSTIETO_PAASIJAINTIKUNTA')}</label>
                    <Controller
                        control={formControl}
                        name={'kotipaikkaUri'}
                        render={({ field }) => (
                            <Select
                                id="PERUSTIETO_PAASIJAINTIKUNTA_SELECT"
                                {...field}
                                ref={undefined}
                                error={!!validationErrors['kotipaikkaUri']}
                                options={kuntaKoodisto.selectOptions()}
                            />
                        )}
                    />
                </div>
                <div className={styles.Kentta}>
                    <label>{i18n.translate('PERUSTIETO_MUUT_KUNNATs')}</label>
                    <Controller
                        control={formControl}
                        name={'muutKotipaikatUris'}
                        render={({ field: { ref, ...rest } }) => (
                            <Select
                                id="PERUSTIETO_MUUT_KUNNAT_SELECT"
                                {...rest}
                                error={!!validationErrors['muutKotipaikatUris']}
                                isMulti
                                options={kuntaKoodisto.selectOptions()}
                            />
                        )}
                    />
                </div>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>{i18n.translate('PERUSTIETO_MAA')}</label>
                    <Controller
                        control={formControl}
                        name={'maaUri'}
                        render={({ field: { ref, ...rest } }) => (
                            <Select
                                id="PERUSTIETO_MAA_SELECT"
                                {...rest}
                                error={!!validationErrors['maaUri']}
                                options={maatJaValtiotKoodisto.selectOptions()}
                            />
                        )}
                    />
                </div>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>{i18n.translate('PERUSTIETO_OPETUSKIELI')}</label>
                    <Controller
                        control={formControl}
                        name={'kieletUris'}
                        render={({ field: { ref, ...rest } }) => (
                            <Select
                                isMulti
                                id="PERUSTIETO_OPETUSKIELI_SELECT"
                                {...rest}
                                error={!!validationErrors['kieletUris']}
                                options={oppilaitoksenOpetuskieletKoodisto.selectOptions()}
                            />
                        )}
                    />
                </div>
            </div>
            <div>
                <Button onClick={handleJatka}>{i18n.translate('BUTTON_JATKA')}</Button>
            </div>
            {nimenmuutosModaaliAuki && (
                <ToimipisteenNimenmuutosModaali
                    setNimenmuutosModaaliAuki={setNimenmuutosModaaliAuki}
                    handleNimiTallennus={handleNimiUpdate}
                    nimi={nimi}
                />
            )}
        </div>
    );
}
