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
import {
    KoodistoSelectOption,
    Nimi,
    Perustiedot,
    ResolvedRakenne,
    UiOrganisaatioBase,
} from '../../../../../types/types';
import { FieldErrors } from 'react-hook-form/dist/types/errors';
import { Control, UseFormRegister } from 'react-hook-form/dist/types/form';
import { Controller, useWatch } from 'react-hook-form';
import ToimipisteenNimenmuutosModaali from '../../../../Modaalit/ToimipisteenNimenmuutos/ToimipisteenNimenmuutosModaali';

type PerustietoLomakeProps = {
    resolvedTyypit: KoodistoSelectOption[];
    rakenne: ResolvedRakenne | undefined;
    language: string;
    openYtjModal: () => void;
    validationErrors: FieldErrors<Perustiedot>;
    formRegister: UseFormRegister<Perustiedot>;
    formControl: Control<Perustiedot>;
    handleNimiUpdate: (nimi: Nimi) => void;
    getPerustiedotValues: () => Perustiedot;
    organisaatioBase: UiOrganisaatioBase;
};

const OrganisaationNimi = ({ defaultNimi, control }) => {
    const { i18n } = useContext(LanguageContext);
    const nimi = useWatch({ control, name: 'nimi', defaultValue: defaultNimi });
    return <span className={styles.ReadOnly}>{i18n.translateNimi(nimi)}</span>;
};

export default function PerustietoLomake(props: PerustietoLomakeProps) {
    const { i18n } = useContext(LanguageContext);
    const {
        organisaatioBase,
        getPerustiedotValues,
        openYtjModal,
        validationErrors,
        formRegister,
        formControl,
        handleNimiUpdate,
        rakenne,
        resolvedTyypit,
    } = props;
    const [nimenmuutosModaaliAuki, setNimenmuutosModaaliAuki] = useState<boolean>(false);
    const [lakkautusModaaliAuki, setLakkautusModaaliAuki] = useState<boolean>(false);

    const { kuntaKoodisto, maatJaValtiotKoodisto, oppilaitoksenOpetuskieletKoodisto } = useContext(KoodistoContext);
    const kunnatOptions = kuntaKoodisto.selectOptions();

    formRegister('nimi');
    const { nimi, organisaatioTyypit } = getPerustiedotValues();
    return (
        <div className={styles.UloinKehys}>
            <div className={styles.Rivi}>
                <div className={styles.Ruudukko}>
                    <span className={styles.AvainKevyestiBoldattu}>{i18n.translate('LABEL_OID')}</span>
                    <span className={styles.ReadOnly}>{organisaatioBase?.oid}</span>
                    {organisaatioBase?.yritysmuoto && [
                        <span key={'yritysmuoto_title'} className={styles.AvainKevyestiBoldattu}>
                            {i18n.translate('PERUSTIETO_YRITYSMUOTO')}
                        </span>,
                        <span key={'yritysmuoto_arvo'} className={styles.ReadOnly}>
                            {organisaatioBase.yritysmuoto}
                        </span>,
                    ]}
                    <span className={styles.AvainKevyestiBoldattu}>
                        {i18n.translate('PERUSTIETO_ORGANISAATION_NIMI')}
                    </span>
                    <OrganisaationNimi control={formControl} defaultNimi={nimi} />
                </div>
                <div>
                    <Button className={styles.Nappi} variant="outlined" onClick={() => setNimenmuutosModaaliAuki(true)}>
                        {i18n.translate('PERUSTIETO_MUOKKAA_ORGANISAATION_NIMEA')}
                    </Button>
                </div>
            </div>
            {rakenne?.showYtj && (
                <div className={styles.Rivi}>
                    <div className={styles.Kentta}>
                        <label>{i18n.translate('PERUSTIETO_Y_TUNNUS')}</label>
                        <Input error={!!validationErrors['ytunnus']} id={'ytunnus'} {...formRegister('ytunnus')} />
                    </div>
                    <Button className={styles.Nappi} variant="outlined" onClick={openYtjModal}>
                        {i18n.translate('PERUSTIETO_PAIVITA_YTJ_TIEDOT')}
                    </Button>
                </div>
            )}
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>{i18n.translate('PERUSTIETO_ORGANISAATIOTYYPPI')} *</label>
                    {organisaatioTyypit && (
                        <Controller
                            control={formControl}
                            name={'organisaatioTyypit'}
                            render={({ field: { ref, ...rest } }) => (
                                <CheckboxGroup {...rest} options={resolvedTyypit} />
                            )}
                        />
                    )}
                </div>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>{i18n.translate('PERUSTIETO_PERUSTAMISPAIVA')}</label>
                    <Controller
                        control={formControl}
                        name={'alkuPvm'}
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
                    <Controller
                        control={formControl}
                        name={'kotipaikka'}
                        render={({ field }) => (
                            <Select
                                id="PERUSTIETO_PAASIJAINTIKUNTA_SELECT"
                                {...field}
                                ref={undefined}
                                error={!!validationErrors['kotipaikka']}
                                options={kunnatOptions}
                            />
                        )}
                    />
                </div>
                <div className={styles.Kentta}>
                    <label>{i18n.translate('PERUSTIETO_MUUT_KUNNAT')}</label>
                    <Controller
                        control={formControl}
                        name={'muutKotipaikat'}
                        render={({ field: { ref, ...rest } }) => (
                            <Select
                                id="PERUSTIETO_MUUT_KUNNAT_SELECT"
                                {...rest}
                                error={!!validationErrors['muutKotipaikat']}
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
                    <Controller
                        control={formControl}
                        name={'maa'}
                        render={({ field: { ref, ...rest } }) => (
                            <Select
                                id="PERUSTIETO_MAA_SELECT"
                                {...rest}
                                error={!!validationErrors['maa']}
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
                        name={'kielet'}
                        render={({ field: { ref, ...rest } }) => (
                            <Select
                                isMulti
                                id="PERUSTIETO_OPETUSKIELI_SELECT"
                                {...rest}
                                error={!!validationErrors['kielet']}
                                options={oppilaitoksenOpetuskieletKoodisto.selectOptions()}
                            />
                        )}
                    />
                </div>
            </div>
            {nimenmuutosModaaliAuki && (
                <ToimipisteenNimenmuutosModaali
                    closeNimenmuutosModaali={() => setNimenmuutosModaaliAuki(false)}
                    handleNimiTallennus={handleNimiUpdate}
                    nimi={nimi}
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
        </div>
    );
}
