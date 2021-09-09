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
import TNFooter from '../../../../Modaalit/ToimipisteenNimenmuutos/TNFooter';
import RadioGroup from '@opetushallitus/virkailija-ui-components/RadioGroup';
import TNUusiBody from '../../../../Modaalit/ToimipisteenNimenmuutos/TNUusiBody';
import DatePickerInput from '@opetushallitus/virkailija-ui-components/DatePickerInput';
import { Koodi, KoodiUri, Organisaatio, Yhteystiedot } from '../../../../../types/types';

type OrganisaatioProps = {
    organisaatio: Organisaatio;
    organisaatioTyypit: Koodi[];
    maatJaValtiot: Koodi[];
    opetuskielet: Koodi[];
    handleOnChange: ({
        name,
        value,
    }: {
        name: string;
        value: { nimi: Nimi; alkuPvm: string }[] | Nimi | KoodiUri[] | Date | KoodiUri | Yhteystiedot[];
    }) => void;
    handleJatka: () => void;
};
type Nimi = {
    fi: string;
    sv: string;
    en: string;
    alkuPvm?: string;
};

const tyhjaNimi: Nimi = {
    fi: '',
    sv: '',
    en: '',
};

interface iOption {
    label: string;
    value: string;
}

// TODO optionsmapper ja paranna logiikkaa
export default function PerustietoLomake(props: OrganisaatioProps) {
    const { i18n, language } = useContext(LanguageContext);
    const { organisaatio, organisaatioTyypit, maatJaValtiot, opetuskielet, handleOnChange, handleJatka } = props;
    const [nimenmuutosModaaliAuki, setNimenmuutosModaaliAuki] = useState<boolean>(false);
    const [onYunnus, setOnYtunnus] = useState<boolean>(true);
    const { kuntaKoodisto } = useContext(KoodistoContext);
    const kaikkiKunnat = kuntaKoodisto.koodit().map((k) => ({
        value: k.uri,
        label: k.nimi[language] || k.nimi['fi'] || k.nimi['sv'] || k.nimi['en'] || '',
    }));
    const [nimi, setNimi] = useState(tyhjaNimi);

    const handleNimiTallennus = () => {
        const nimet = { nimi: Object.assign({}, nimi), alkuPvm: new Date().toISOString().split('T')[0] };
        handleOnChange({ name: 'nimet', value: [nimet] });
        handleOnChange({ name: 'nimi', value: nimi });
    };
    console.log('o', organisaatio, nimi, onYunnus, opetuskielet);
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
                            name="ytunnus"
                            onChange={(e) => handleOnChange({ name: e.target.name, value: e.target.value })}
                            value={organisaatio.ytunnus}
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
                        <span className={styles.AvainKevyestiBoldattu}>
                            {i18n.translate('PERUSTIETO_ORGANISAATIO_TYYPPI')}
                        </span>,
                        <span className={styles.ReadOnly}>{i18n.translate('PERUSTIETO_TODO')}</span>,
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
                <div className={styles.Kentta}>
                    <Button className={styles.Nappi} variant="outlined" onClick={() => setNimenmuutosModaaliAuki(true)}>
                        {i18n.translate('BUTTON_MUOKKAA_ORGANISAATION_NIMEA')}
                    </Button>
                </div>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>{i18n.translate('PERUSTIETO_ORGANISAATIOTYYPPI')}</label>
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
                <div className={styles.Kentta}>
                    <label>{i18n.translate('PERUSTIETO__LAKKAUTUSPAIVA')} (MIKÄ TÄN KUULUU OLLA?)</label>
                    <Input value={organisaatio.alkuPvm} />
                </div>
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
                    <label>{i18n.translate('PERUSTIETO_MUUT_KUNNATs')}</label>
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
                        onChange={(option) => {
                            console.log('opts', option);
                            handleOnChange({
                                name: 'muutKotipaikatUris',
                                value: (option as iOption[]).map((o) => o.value),
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
                                label: mv.nimi[language] || mv.nimi['fi'] || mv.nimi['sv'] || mv.nimi['en'] || '',
                            }))
                            .find((mv) => {
                                return mv.value === organisaatio.maaUri;
                            })}
                        options={maatJaValtiot.map((mv) => ({
                            value: mv.uri,
                            label: mv.nimi[language] || mv.nimi['fi'] || mv.nimi['sv'] || mv.nimi['en'] || '',
                        }))}
                    />
                </div>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>{i18n.translate('PERUSTIETO_OPETUSKIELI')}</label>
                    <Select
                        onChange={(selected) =>
                            handleOnChange({ name: 'kieletUris', value: (selected as iOption[]).map((o) => o.value) })
                        }
                        isMulti
                        value={opetuskielet
                            .map((mv) => ({
                                value: `${mv.uri}#${mv.versio}`,
                                label: mv.nimi[language] || mv.nimi['fi'] || mv.nimi['sv'] || mv.nimi['en'] || '',
                            }))
                            .find((mv) => {
                                return organisaatio.kieletUris.find(
                                    (kU: string) => kU.slice(0, kU.length - 2) === mv.value
                                );
                            })}
                        options={opetuskielet.map((mv) => ({
                            value: `${mv.uri}#${mv.versio}`,
                            label: mv.nimi[language] || mv.nimi['fi'] || mv.nimi['sv'] || mv.nimi['en'] || '',
                        }))}
                    />
                </div>
            </div>
            <div>
                <Button onClick={handleJatka}>{i18n.translate('BUTTON_JATKA')}</Button>
            </div>
            {nimenmuutosModaaliAuki && (
                <PohjaModaali
                    header={<TNHeader />}
                    body={<TNUusiBody nimi={nimi} handleChange={setNimi} />}
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
        </div>
    );
}
