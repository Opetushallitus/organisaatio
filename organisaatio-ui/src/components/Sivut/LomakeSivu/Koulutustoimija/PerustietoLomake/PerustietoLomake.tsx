import * as React from 'react';
import styles from './PerustietoLomake.module.css';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import CheckboxGroup from '@opetushallitus/virkailija-ui-components/CheckboxGroup';
import Select from '@opetushallitus/virkailija-ui-components/Select';
import { useContext, useState } from 'react';
import { KoodistoContext } from '../../../../../contexts/contexts';
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
import { YtjOrganisaatio } from '../../../../../types/types';

type OrganisaatioProps = {
    organisaatio: any;
    language: string;
    organisaatioTyypit: any;
    maatJaValtiot: any;
    opetuskielet: any;
    handleOnChange: ({ name, value }: { name: string; value: any }) => void;
    setYtjDataFetched: (organisaatio: YtjOrganisaatio) => void;
};

interface iOption {
    label: string;
    value: string;
}

// TODO optionsmapper ja paranna logiikkaa
export default function PerustietoLomake(props: OrganisaatioProps) {
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
    const kaikkiKunnat = kuntaKoodisto.koodit().map((k: any) => ({
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

    const kielistetytOpetuskielet = opetuskielet.map((mv: any) => ({
        value: `${mv.uri}#${mv.versio}`,
        label: mv.nimi[language] || mv.nimi['fi'] || mv.nimi['sv'] || mv.nimi['en'],
    }));

    const [nimi, setNimi] = useState(organisaatio.nimi);
    console.log('orgkiele', organisaatio.kieletUris);
    return (
        <div className={styles.UloinKehys}>
            <div className={styles.Rivi}>
                <div className={styles.Ruudukko}>
                    <span className={styles.AvainKevyestiBoldattu}> OID</span>
                    <span className={styles.ReadOnly}>{organisaatio.oid}</span>
                    {organisaatio.yritysmuoto && [
                        <span className={styles.AvainKevyestiBoldattu}>Yritysmuoto</span>,
                        <span className={styles.ReadOnly}>{organisaatio.yritysmuoto}</span>,
                    ]}
                    <span className={styles.AvainKevyestiBoldattu}>Organisaation nimi</span>
                    <span className={styles.ReadOnly}>
                        {organisaatio.nimi[language] ||
                            organisaatio.nimi['fi'] ||
                            organisaatio.nimi['sv'] ||
                            organisaatio.nimi['en']}
                    </span>
                </div>
                <div>
                    <Button className={styles.Nappi} variant="outlined" onClick={() => setNimenmuutosModaaliAuki(true)}>
                        Muokkaa organisaation nimeä
                    </Button>
                </div>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>Y-tunnus</label>
                    <Input
                        name="ytunnus"
                        value={organisaatio.ytunnus || ''}
                        onChange={(e) => handleOnChange({ name: e.target.name, value: e.target.value })}
                    />
                </div>
                <Button className={styles.Nappi} variant="outlined" onClick={() => setYTJModaaliAuki(true)}>
                    Päivitä YTJ-tiedot
                </Button>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>Organisaatiotyyppi *</label>
                    <CheckboxGroup
                        value={[...organisaatio.tyypit]}
                        options={organisaatioTyypit.map((oT: any) => ({
                            value: oT.uri,
                            label: oT.nimi[language] || oT.nimi['fi'] || oT.nimi['sv'] || oT.nimi['en'],
                        }))}
                        onChange={(tyypit) => {
                            console.log(tyypit);
                            handleOnChange({ name: 'tyypit', value: tyypit });
                        }}
                    />
                </div>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>Organisaatio merkitty rekisteriin (perustamispäivämäärä)</label>
                    <DatePickerInput
                        value={organisaatio.alkuPvm}
                        onChange={(date: Date) => handleOnChange({ name: 'alkuPvm', value: date })}
                    />
                </div>
                <Button className={styles.Nappi} variant="outlined" onClick={() => setLakkautusModaaliAuki(true)}>
                    Merkitse organisaatio lakkautetuksi
                </Button>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>Pääsijaintikunta</label>
                    <Select
                        value={kaikkiKunnat.find((kk) => kk.value === organisaatio.kotipaikkaUri)}
                        options={kaikkiKunnat}
                        onChange={(option) =>
                            handleOnChange({ name: 'kotipaikkaUri', value: (option as iOption).value })
                        }
                    />
                </div>
                <div className={styles.Kentta}>
                    <label>Muut kunnat</label>
                    <Select
                        isMulti
                        value={
                            organisaatio.muutKotipaikatUris.length
                                ? organisaatio.muutKotipaikatUris.map((mk: string) =>
                                      kaikkiKunnat.find((kk) => kk.value === mk)
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
                    <label>maa</label>
                    <Select
                        onChange={(selected) => handleOnChange({ name: 'maaUri', value: (selected as iOption).value })}
                        value={maatJaValtiot
                            .map((mv: any) => ({
                                value: mv.uri,
                                label: mv.nimi[language] || mv.nimi['fi'] || mv.nimi['sv'] || mv.nimi['en'],
                            }))
                            .find((mv: any) => {
                                return mv.value === organisaatio.maaUri;
                            })}
                        options={maatJaValtiot.map((mv: any) => ({
                            value: mv.uri,
                            label: mv.nimi[language] || mv.nimi['fi'] || mv.nimi['sv'] || mv.nimi['en'],
                        }))}
                    />
                </div>
            </div>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <label>Opetuskieli</label>
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
                            organisaatio.kieletUris.map((ku: string) =>
                                kielistetytOpetuskielet.find((ok: any) => ku === ok.value)
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