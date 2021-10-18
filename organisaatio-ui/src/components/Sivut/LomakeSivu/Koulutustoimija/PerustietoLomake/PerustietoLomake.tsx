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
import { Koodi, Nimi, Organisaatio } from '../../../../../types/types';
import { FieldErrors } from 'react-hook-form/dist/types/errors';
import { Control, UseFormRegister } from 'react-hook-form/dist/types/form';
import { FieldValues } from 'react-hook-form/dist/types/fields';
import { Controller } from 'react-hook-form';
import ToimipisteenNimenmuutosModaali from '../../../../Modaalit/ToimipisteenNimenmuutos/ToimipisteenNimenmuutosModaali';
import { YtjOrganisaatio } from '../../../../../types/apiTypes';

type PerustietoLomakeProps = {
    organisaatioTyypit: Koodi[];
    organisaatio: Organisaatio;
    language: string;
    setYtjDataFetched: (organisaatio: YtjOrganisaatio) => void;
    validationErrors: FieldErrors<FieldValues>;
    formRegister: UseFormRegister<FieldValues>;
    formControl: Control<FieldValues>;
    handleNimiUpdate: (nimi: Nimi) => void;
};

export default function PerustietoLomake(props: PerustietoLomakeProps) {
    const { i18n } = useContext(LanguageContext);
    const {
        organisaatio,
        language,
        setYtjDataFetched,
        validationErrors,
        formRegister,
        formControl,
        handleNimiUpdate,
        organisaatioTyypit,
    } = props;
    const [nimenmuutosModaaliAuki, setNimenmuutosModaaliAuki] = useState<boolean>(false);
    const [lakkautusModaaliAuki, setLakkautusModaaliAuki] = useState<boolean>(false);
    const [YTJModaaliAuki, setYTJModaaliAuki] = useState<boolean>(false);

    const { kuntaKoodisto, maatJaValtiotKoodisto, oppilaitoksenOpetuskieletKoodisto } = useContext(KoodistoContext);
    const kunnatOptions = kuntaKoodisto.selectOptions();
    const handleKorvaaOrganisaatio = (ytjOrg: YtjOrganisaatio) => {
        setYtjDataFetched(ytjOrg);
        setYTJModaaliAuki(false);
    };

    formRegister('nimi');
    const preselected = organisaatio.kieletUris.map((kieliUri) =>
        oppilaitoksenOpetuskieletKoodisto.uri2SelectOption(kieliUri)
    );
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
                    <span className={styles.ReadOnly}>{i18n.translateNimi(organisaatio.nimi)}</span>
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
                        <Controller
                            control={formControl}
                            name={'tyypit'}
                            defaultValue={[...organisaatio.tyypit]}
                            render={({ field: { ref, ...rest } }) => (
                                <CheckboxGroup
                                    {...rest}
                                    options={organisaatioTyypit.map((oT) => ({
                                        value: oT.uri,
                                        label: oT.nimi[language] || oT.nimi['fi'] || oT.nimi['sv'] || oT.nimi['en'],
                                    }))}
                                />
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
                    <Controller
                        control={formControl}
                        name={'kieletUris'}
                        defaultValue={preselected}
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
                    closeNimenmuutosModaali={() => setNimenmuutosModaaliAuki(false)}
                    handleNimiTallennus={handleNimiUpdate}
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
            {YTJModaaliAuki && (
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
