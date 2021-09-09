import * as React from 'react';
import { useContext, useState } from 'react';
import styles from './PerustietoLomake.module.css';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import CheckboxGroup from '@opetushallitus/virkailija-ui-components/CheckboxGroup';
import Select from '@opetushallitus/virkailija-ui-components/Select';
import { KoodistoContext, LanguageContext } from '../../../../../contexts/contexts';
import PohjaModaali from '../../../../Modaalit/PohjaModaali/PohjaModaali';
import TNHeader from '../../../../Modaalit/ToimipisteenNimenmuutos/TNHeader';
import TNBody from '../../../../Modaalit/ToimipisteenNimenmuutos/TNBody';
import TNFooter from '../../../../Modaalit/ToimipisteenNimenmuutos/TNFooter';
import TLHeader from '../../../../Modaalit/ToimipisteenLakkautus/TLHeader';
import TLBody from '../../../../Modaalit/ToimipisteenLakkautus/TLBody';
import TLFooter from '../../../../Modaalit/ToimipisteenLakkautus/TLFooter';
import DatePickerInput from '@opetushallitus/virkailija-ui-components/DatePickerInput';
import YTJHeader from '../../../../Modaalit/YTJModaali/YTJHeader';
import YTJBody from '../../../../Modaalit/YTJModaali/YTJBody';
import YTJFooter from '../../../../Modaalit/YTJModaali/YTJFooter';
import { Koodi, KoodiUri, Nimi, Organisaatio, YtjOrganisaatio } from '../../../../../types/types';

type OrganisaatioProps = {
    organisaatio: Organisaatio;
    language: string;
    organisaatioTyypit: Koodi[];
    maatJaValtiot: Koodi[];
    opetuskielet: Koodi[];
    handleOnChange: ({
        name,
        value,
    }: {
        name: keyof Organisaatio;
        value: { nimi: Nimi; alkuPvm: string }[] | Nimi | KoodiUri[] | Date | KoodiUri;
    }) => void;
    setYtjDataFetched: (organisaatio: YtjOrganisaatio) => void;
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
        organisaatioTyypit,
        maatJaValtiot,
        opetuskielet,
        handleOnChange,
        language,
        setYtjDataFetched,
    } = props;
    const [nimenmuutosModaaliAuki, setNimenmuutosModaaliAuki] = useState<boolean>(false);
    const [lakkautusModaaliAuki, setLakkautusModaaliAuki] = useState<boolean>(false);
    const [YTJModaaliAuki, setYTJModaaliAuki] = useState<boolean>(false);

    const { kuntaKoodisto } = useContext(KoodistoContext);
    const kaikkiKunnat: { value: string; label: string }[] = kuntaKoodisto.koodit().map((k: Koodi) => ({
        value: k.uri,
        label: k.nimi[language] || k.nimi['fi'] || k.nimi['sv'] || k.nimi['en'],
    }));

    const handleNimiTallennus = () => {
        const nimet = { nimi: Object.assign({}, nimi), alkuPvm: new Date().toISOString().split('T')[0] };
        handleOnChange({ name: 'nimet', value: [nimet] });
        handleOnChange({ name: 'nimi', value: nimi });
    };

    const handleKorvaaOrganisaatio = (ytjOrg: YtjOrganisaatio) => {
        setYtjDataFetched(ytjOrg);
        setYTJModaaliAuki(false);
    };

    const kielistetytOpetuskielet = opetuskielet.map((mv) => ({
        value: `${mv.uri}#${mv.versio}`,
        label: mv.nimi[language] || mv.nimi['fi'] || mv.nimi['sv'] || mv.nimi['en'],
    }));

    const [nimi, setNimi] = useState(organisaatio.nimi);
    console.log('orgkiele', organisaatio.kieletUris);
    return (
        <div className={styles.UloinKehys}>
            <div className={styles.Rivi}>
                <div className={styles.Ruudukko}>
                    <span className={styles.AvainKevyestiBoldattu}>{i18n.translate('LABEL_OID')}</span>
                    <span className={styles.ReadOnly}>{organisaatio.oid}</span>
                    {organisaatio.yritysmuoto && [
                        <span className={styles.AvainKevyestiBoldattu}>
                            {i18n.translate('PERUSTIETO_YRITYSMUOTO')}
                        </span>,
                        <span className={styles.ReadOnly}>{organisaatio.yritysmuoto}</span>,
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
                            options={organisaatioTyypit.map((oT) => ({
                                value: oT.uri,
                                label: oT.nimi[language] || oT.nimi['fi'] || oT.nimi['sv'] || oT.nimi['en'],
                            }))}
                            onChange={(tyypit) => {
                                console.log(tyypit);
                                handleOnChange({ name: 'tyypit', value: tyypit });
                            }}
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
                </div>
                <Button className={styles.Nappi} variant="outlined" onClick={() => setLakkautusModaaliAuki(true)}>
                    {i18n.translate('PERUSTIETO_MERKITSE_ORGANISAATIO_LAKKAUTETUKSI')}
                </Button>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>{i18n.translate('PERUSTIETO_PAASIJAINTIKUNTA')}</label>
                    <Select
                        value={kaikkiKunnat.find((kk) => kk.value === organisaatio.kotipaikkaUri)}
                        options={kaikkiKunnat}
                        onChange={(option) =>
                            handleOnChange({ name: 'kotipaikkaUri', value: (option as iOption).value })
                        }
                    />
                </div>
                <div className={styles.Kentta}>
                    <label>{i18n.translate('PERUSTIETO_MUUT_KUNNAT')}</label>
                    <Select
                        isMulti
                        value={
                            organisaatio.muutKotipaikatUris
                                ? organisaatio.muutKotipaikatUris.map(
                                      (mk) => kaikkiKunnat.find((kk) => kk.value === mk) || { label: '', value: '' }
                                  )
                                : []
                        }
                        options={kaikkiKunnat}
                        onChange={(option = []) => {
                            handleOnChange({
                                name: 'muutKotipaikatUris',
                                value: option ? (option as iOption[]).map((o) => o.value) : [],
                            });
                        }}
                    />
                </div>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>{i18n.translate('PERUSTIETO_MAA')}</label>
                    <Select
                        onChange={(selected) => handleOnChange({ name: 'maaUri', value: (selected as iOption).value })}
                        value={maatJaValtiot
                            .map((mv) => ({
                                value: mv.uri,
                                label: mv.nimi[language] || mv.nimi['fi'] || mv.nimi['sv'] || mv.nimi['en'],
                            }))
                            .find((mv) => {
                                return mv.value === organisaatio.maaUri;
                            })}
                        options={maatJaValtiot.map((mv) => ({
                            value: mv.uri,
                            label: mv.nimi[language] || mv.nimi['fi'] || mv.nimi['sv'] || mv.nimi['en'],
                        }))}
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
                            organisaatio.muutKotipaikatUris &&
                            organisaatio.kieletUris.map(
                                (ku) =>
                                    kielistetytOpetuskielet.find((ok) => ku === ok.value) || {
                                        value: '',
                                        label: '',
                                    }
                            )
                        }
                        options={kielistetytOpetuskielet}
                    />
                </div>
            </div>
            {nimenmuutosModaaliAuki && (
                <PohjaModaali
                    header={<TNHeader />}
                    body={<TNBody nimi={nimi} handleChange={setNimi} />}
                    footer={
                        <TNFooter
                            tallennaCallback={() => {
                                handleNimiTallennus();
                                setNimenmuutosModaaliAuki(false);
                            }}
                            peruutaCallback={() => {
                                setNimenmuutosModaaliAuki(false);
                            }}
                        />
                    }
                    suljeCallback={() => setNimenmuutosModaaliAuki(false)}
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
