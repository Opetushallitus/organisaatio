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
import { FieldErrors } from 'react-hook-form/dist/types/errors';
import { Control, UseFormRegister, UseFormSetValue, UseFormWatch } from 'react-hook-form/dist/types/form';
import { Controller } from 'react-hook-form';
import { Koodi, Perustiedot, Yhteystiedot } from '../../../../../types/types';
import YTJHeader from '../../../../Modaalit/YTJModaali/YTJHeader';
import YTJBody from '../../../../Modaalit/YTJModaali/YTJBody';
import YTJFooter from '../../../../Modaalit/YTJModaali/YTJFooter';
import { YtjOrganisaatio } from '../../../../../types/apiTypes';
import PohjaModaali from '../../../../Modaalit/PohjaModaali/PohjaModaali';
import { warning } from '../../../../Notification/Notification';

type UusiOrgPerustiedotProps = {
    organisaatioTyypit: Koodi[];
    validationErrors: FieldErrors<Perustiedot>;
    formRegister: UseFormRegister<Perustiedot>;
    formControl: Control<Perustiedot>;
    handleJatka: () => void;
    setPerustiedotValue: UseFormSetValue<Perustiedot>;
    setYhteystiedotValue: UseFormSetValue<Yhteystiedot>;
    watchPerustiedot: UseFormWatch<Perustiedot>;
};

export default function PerustietoLomake(props: UusiOrgPerustiedotProps) {
    const {
        handleJatka,
        validationErrors,
        formControl,
        formRegister,
        setPerustiedotValue,
        setYhteystiedotValue,
        organisaatioTyypit,
    } = props;
    const { i18n, language } = useContext(LanguageContext);
    const { kuntaKoodisto, maatJaValtiotKoodisto, oppilaitoksenOpetuskieletKoodisto } = useContext(KoodistoContext);
    const [YTJModaaliAuki, setYTJModaaliAuki] = useState<boolean>(false);
    const [onYunnus, setOnYtunnus] = useState<boolean>(true);

    const handleYtjData = (ytjOrg: YtjOrganisaatio) => {
        setPerustiedotValue('ytunnus', ytjOrg.ytunnus);
        setPerustiedotValue('nimi', { fi: ytjOrg.nimi, sv: ytjOrg.nimi, en: ytjOrg.nimi });
        setPerustiedotValue('alkuPvm', ytjOrg.aloitusPvm);
        const selectedKunta = kuntaKoodisto.koodit().find((a) => a.arvo === ytjOrg.kotiPaikkaKoodi);
        const selectedKuntaSelector = kuntaKoodisto
            .selectOptions()
            .find((a) => a.value.startsWith(selectedKunta?.uri || ''));
        if (selectedKunta && selectedKuntaSelector) setPerustiedotValue('kotipaikkaUri', selectedKuntaSelector);
        else warning({ message: 'YTJ_DATA_KOTIPAIKKA_NOT_FOUND_IN_KOODISTO' });
        const selectedKieli = oppilaitoksenOpetuskieletKoodisto
            .selectOptions()
            .find((a) => a.label === ytjOrg.yrityksenKieli?.toLowerCase());
        if (selectedKieli) setPerustiedotValue('kieletUris', [selectedKieli]);
        else warning({ message: 'YTJ_DATA_UNKNOWN_KIELI' });
        setYhteystiedotValue('kieli_fi#1', {
            postiOsoite: ytjOrg.postiOsoite.katu,
            postiOsoitePostiNro: ytjOrg.postiOsoite.postinumero,
            kayntiOsoite: ytjOrg.kayntiOsoite?.katu,
            kayntiOsoitePostiNro: ytjOrg.kayntiOsoite?.postinumero,
            puhelinnumero: ytjOrg.puhelin,
            email: ytjOrg.sahkoposti,
            www: ytjOrg.www,
        });
        setYhteystiedotValue('osoitteetOnEri', !!ytjOrg.kayntiOsoite);
        setYTJModaaliAuki(false);
    };
    return (
        <div className={styles.UloinKehys}>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>{i18n.translate('PERUSTIETO_ORGANISAATIOTYYPPI')}</label>
                    <Controller
                        control={formControl}
                        name={'tyypit'}
                        defaultValue={[]}
                        render={({ field: { ref, ...rest } }) => (
                            <CheckboxGroup
                                {...rest}
                                options={organisaatioTyypit.map((oT) => ({
                                    value: oT.uri,
                                    label: oT.nimi[language] || oT.nimi['fi'] || oT.nimi['sv'] || oT.nimi['en'], //TODO make better
                                }))}
                            />
                        )}
                    />
                </div>
            </div>
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
                    <Button className={styles.Nappi} variant="outlined" onClick={() => setYTJModaaliAuki(true)}>
                        {i18n.translate('BUTTON_HAE_YTJ_TIEDOT')}
                    </Button>
                </div>
            )}
            <div className={styles.Rivi}>
                <div className={styles.BodyKentta}>
                    <label>{i18n.translate('PERUSTIETO_NIMI_SUOMEKSI')}</label>
                    <Input
                        error={!!validationErrors['nimiFi']}
                        id={'organisaation_nimiFi'}
                        {...formRegister('nimi.fi')}
                        defaultValue={''}
                    />
                </div>{' '}
            </div>
            <div className={styles.Rivi}>
                <div className={styles.BodyKentta}>
                    <label>{i18n.translate('PERUSTIETO_NIMI_RUOTSIKSI')}</label>
                    <Input
                        error={!!validationErrors['nimiSv']}
                        id={'organisaation_nimiSv'}
                        {...formRegister('nimi.sv')}
                        defaultValue={''}
                    />
                </div>{' '}
            </div>
            <div className={styles.Rivi}>
                <div className={styles.BodyKentta}>
                    <label>{i18n.translate('PERUSTIETO_NIMI_ENGLANNIKSI')}</label>
                    <Input
                        error={!!validationErrors['nimiEn']}
                        id={'organisaation_nimiEn'}
                        {...formRegister('nimi.en')}
                        defaultValue={''}
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
                            <DatePickerInput error={!!validationErrors['alkuPvm']} {...rest} />
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
                        defaultValue={maatJaValtiotKoodisto.uri2SelectOption('maatjavaltiot1_fin')}
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
            {YTJModaaliAuki && (
                <PohjaModaali
                    header={<YTJHeader />}
                    body={<YTJBody ytunnus={''} korvaaOrganisaatio={handleYtjData} />}
                    footer={
                        <YTJFooter
                            peruutaCallback={() => {
                                setYTJModaaliAuki(false);
                            }}
                        />
                    }
                    suljeCallback={() => setYTJModaaliAuki(false)}
                />
            )}
        </div>
    );
}
