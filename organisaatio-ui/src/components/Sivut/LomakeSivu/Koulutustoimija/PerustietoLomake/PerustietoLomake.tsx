import * as React from 'react';
import { useContext, useState } from 'react';
import styles from './PerustietoLomake.module.css';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import CheckboxGroup from '@opetushallitus/virkailija-ui-components/CheckboxGroup';
import Select from '@opetushallitus/virkailija-ui-components/Select';
import { KoodistoContext, LanguageContext } from '../../../../../contexts/contexts';
import PohjaModaali from '../../../../Modaalit/PohjaModaali/PohjaModaali';
import TLHeader from '../../../../Modaalit/ToimipisteenLakkautus/TLHeader';
import TLBody from '../../../../Modaalit/ToimipisteenLakkautus/TLBody';
import TLFooter from '../../../../Modaalit/ToimipisteenLakkautus/TLFooter';
import DatePickerInput from '@opetushallitus/virkailija-ui-components/DatePickerInput';
import YTJHeader from '../../../../Modaalit/YTJModaali/YTJHeader';
import YTJBody from '../../../../Modaalit/YTJModaali/YTJBody';
import YTJFooter from '../../../../Modaalit/YTJModaali/YTJFooter';
import { KoodiUri, Nimi, Organisaatio, YtjOrganisaatio } from '../../../../../types/types';
import { FieldErrors } from 'react-hook-form/dist/types/errors';
import { Control, UseFormRegister } from 'react-hook-form/dist/types/form';
import { FieldValues } from 'react-hook-form/dist/types/fields';
import { Controller } from 'react-hook-form';
import ToimipisteenNimenmuutosModaali from '../../../../Modaalit/ToimipisteenNimenmuutos/ToimipisteenNimenmuutosModaali';

type OrganisaatioProps = {
    organisaatio: Organisaatio;
    language: string;
    handleOnChange: ({
        name,
        value,
    }: {
        name: keyof Organisaatio;
        value: { nimi: Nimi; alkuPvm: string }[] | Nimi | KoodiUri[] | Date | KoodiUri;
    }) => void;
    setYtjDataFetched: (organisaatio: YtjOrganisaatio) => void;
    validationErrors: FieldErrors<FieldValues>;
    formRegister: UseFormRegister<FieldValues>;
    formControl: Control<FieldValues>;
};

interface iOption {
    label: string;
    value: string;
}

// TODO optionsmapper ja paranna logiikkaa
export default function PerustietoLomake(props: OrganisaatioProps) {
    const { i18n } = useContext(LanguageContext);
    const {
        organisaatio,
        handleOnChange,
        language,
        setYtjDataFetched,
        validationErrors,
        formRegister,
        formControl,
    } = props;
    const [nimenmuutosModaaliAuki, setNimenmuutosModaaliAuki] = useState<boolean>(false);
    const [lakkautusModaaliAuki, setLakkautusModaaliAuki] = useState<boolean>(false);
    const [YTJModaaliAuki, setYTJModaaliAuki] = useState<boolean>(false);

    const {
        kuntaKoodisto,
        organisaatioTyypitKoodisto,
        maatJaValtiotKoodisto,
        oppilaitoksenOpetuskieletKoodisto,
    } = useContext(KoodistoContext);
    const kunnatOptions = kuntaKoodisto.selectOptions();
    const handleNimiTallennus = (nimi) => {
        const nimet = { nimi: Object.assign({}, nimi), alkuPvm: new Date().toISOString().split('T')[0] };
        handleOnChange({ name: 'nimet', value: [nimet] });
        handleOnChange({ name: 'nimi', value: nimi });
    };

    const handleKorvaaOrganisaatio = (ytjOrg: YtjOrganisaatio) => {
        setYtjDataFetched(ytjOrg);
        setYTJModaaliAuki(false);
    };

    return (
        <div className={styles.UloinKehys}>
            <div className={styles.Rivi}>
                <div className={styles.Ruudukko}>
                    <span className={styles.AvainKevyestiBoldattu}>{i18n.translate('LABEL_OID')}</span>
                    <span className={styles.ReadOnly}>{organisaatio.oid}</span>
                    {organisaatio.yritysmuoto && [
                        <span key={'yritysmuoto_title'} className={styles.AvainKevyestiBoldattu}>
                            {i18n.translate('PERUSTIETO_YRITYSMUOTO')}
                        </span>,
                        <span key={'yritysmuoto_arvo'} className={styles.ReadOnly}>
                            {organisaatio.yritysmuoto}
                        </span>,
                    ]}
                    <span className={styles.AvainKevyestiBoldattu}>
                        {i18n.translate('PERUSTIETO_ORGANISAATION_NIMI')}
                    </span>
                    <span className={styles.ReadOnly}>
                        {organisaatio.nimi[language] ||
                            organisaatio.nimi['fi'] ||
                            organisaatio.nimi['sv'] ||
                            organisaatio.nimi['en']}
                    </span>
                </div>
                <div>
                    <Button className={styles.Nappi} variant="outlined" onClick={() => setNimenmuutosModaaliAuki(true)}>
                        {i18n.translate('PERUSTIETO_MUOKKAA_ORGANISAATION_NIMEA')}
                    </Button>
                </div>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>{i18n.translate('PERUSTIETO_Y_TUNNUS')}</label>
                    <Input
                        name="ytunnus"
                        value={organisaatio.ytunnus || ''}
                        onChange={(e) => handleOnChange({ name: e.target.name, value: e.target.value })}
                    />
                    <Input
                        error={!!validationErrors['ytunnus']}
                        id={'ytunnus'}
                        {...formRegister('ytunnus')}
                        defaultValue={organisaatio.ytunnus}
                    />
                </div>
                <Button className={styles.Nappi} variant="outlined" onClick={() => setYTJModaaliAuki(true)}>
                    {i18n.translate('PERUSTIETO_PAIVITA_YTJ_TIEDOT')}
                </Button>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>{i18n.translate('PERUSTIETO_ORGANISAATIOTYYPPI')} *</label>
                    {organisaatio.tyypit && (
                            <CheckboxGroup
                                value={[...organisaatio.tyypit]}
                                options={organisaatioTyypitKoodisto.selectOptions()}
                                onChange={(tyypit) => {
                                    handleOnChange({ name: 'tyypit', value: tyypit });
                                }}
                            />
                        ) && (
                            <Controller
                                control={formControl}
                                name={'tyypit'}
                                defaultValue={[...organisaatio.tyypit]}
                                render={({ field: { ref, ...rest } }) => (
                                    <CheckboxGroup {...rest} options={organisaatioTyypitKoodisto.selectOptions()} />
                                )}
                            />
                        )}
                </div>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>{i18n.translate('PERUSTIETO_PERUSTAMISPAIVA')}</label>
                    <DatePickerInput
                        value={organisaatio.alkuPvm || ''}
                        onChange={(date: Date) => handleOnChange({ name: 'alkuPvm', value: date })}
                    />
                    <Controller
                        control={formControl}
                        name={'alkuPvm'}
                        defaultValue={organisaatio.alkuPvm}
                        render={({ field: { ref, ...rest } }) => (
                            <DatePickerInput {...rest} error={!!validationErrors['alkuPvm']} />
                        )}
                    />
                </div>
                <Button className={styles.Nappi} variant="outlined" onClick={() => setLakkautusModaaliAuki(true)}>
                    {i18n.translate('PERUSTIETO_MERKITSE_ORGANISAATIO_LAKKAUTETUKSI')}
                </Button>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>{i18n.translate('PERUSTIETO_PAASIJAINTIKUNTA')}</label>
                    <Select
                        value={kuntaKoodisto.uri2SelectOption(organisaatio.kotipaikkaUri)}
                        options={kunnatOptions}
                        onChange={(option) =>
                            handleOnChange({ name: 'kotipaikkaUri', value: (option as iOption).value })
                        }
                    />
                    <Controller
                        control={formControl}
                        name={'kotipaikkaUri'}
                        defaultValue={kuntaKoodisto.uri2SelectOption(organisaatio.kotipaikkaUri)}
                        render={({ field }) => (
                            <Select
                                id="PERUSTIETO_PAASIJAINTIKUNTA_SELECT"
                                {...field}
                                ref={undefined}
                                error={!!validationErrors['kotipaikkaUri']}
                                options={kunnatOptions}
                            />
                        )}
                    />
                </div>
                <div className={styles.Kentta}>
                    <label>{i18n.translate('PERUSTIETO_MUUT_KUNNAT')}</label>
                    <Select
                        isMulti
                        value={(organisaatio.muutKotipaikatUris || []).map((muuKotipaikkaUri) =>
                            kuntaKoodisto.uri2SelectOption(muuKotipaikkaUri)
                        )}
                        options={kunnatOptions}
                        onChange={(option = []) => {
                            handleOnChange({
                                name: 'muutKotipaikatUris',
                                value: option ? (option as iOption[]).map((o) => o.value) : [],
                            });
                        }}
                    />
                    <Controller
                        control={formControl}
                        name={'muutKotipaikatUris'}
                        defaultValue={(organisaatio.muutKotipaikatUris || []).map((muuKotipaikkaUri) =>
                            kuntaKoodisto.uri2SelectOption(muuKotipaikkaUri)
                        )}
                        render={({ field: { ref, ...rest } }) => (
                            <Select
                                id="PERUSTIETO_MUUT_KUNNAT_SELECT"
                                {...rest}
                                error={!!validationErrors['muutKotipaikatUris']}
                                isMulti
                                options={kunnatOptions}
                            />
                        )}
                    />
                </div>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>{i18n.translate('PERUSTIETO_MAA')}</label>
                    <Select
                        onChange={(selected) => handleOnChange({ name: 'maaUri', value: (selected as iOption).value })}
                        value={maatJaValtiotKoodisto.uri2SelectOption(organisaatio.maaUri)}
                        options={maatJaValtiotKoodisto.selectOptions()}
                    />
                    <Controller
                        control={formControl}
                        name={'maaUri'}
                        defaultValue={maatJaValtiotKoodisto.uri2SelectOption(organisaatio.maaUri)}
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
                    <Select
                        onChange={(selected = []) =>
                            handleOnChange({
                                name: 'kieletUris',
                                value: selected ? (selected as iOption[]).map((o) => o.value) : [],
                            })
                        }
                        isMulti
                        value={
                            organisaatio.kieletUris &&
                            organisaatio.kieletUris.map((kieliUri) =>
                                oppilaitoksenOpetuskieletKoodisto.uri2SelectOption(kieliUri)
                            )
                        }
                        options={oppilaitoksenOpetuskieletKoodisto.selectOptions()}
                    />
                    <Controller
                        control={formControl}
                        name={'kieletUris'}
                        defaultValue={organisaatio.kieletUris.map((kieliUri) =>
                            oppilaitoksenOpetuskieletKoodisto.uri2SelectOption(kieliUri)
                        )}
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
            {nimenmuutosModaaliAuki && (
                <ToimipisteenNimenmuutosModaali
                    setNimenmuutosModaaliAuki={setNimenmuutosModaaliAuki}
                    handleNimiTallennus={handleNimiTallennus}
                    nimi={organisaatio.nimi}
                />
            )}
            {lakkautusModaaliAuki && (
                <PohjaModaali
                    header={<TLHeader />}
                    body={<TLBody />}
                    footer={
                        <TLFooter
                            peruutaCallback={() => {
                                setLakkautusModaaliAuki(false);
                            }}
                        />
                    }
                    suljeCallback={() => setLakkautusModaaliAuki(false)}
                />
            )}
            {YTJModaaliAuki && organisaatio && organisaatio.ytunnus && (
                <PohjaModaali
                    header={<YTJHeader />}
                    body={<YTJBody ytunnus={organisaatio.ytunnus} korvaaOrganisaatio={handleKorvaaOrganisaatio} />}
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
