import * as React from 'react';
import styles from './PerustietoLomake.module.css';
import Button from "@opetushallitus/virkailija-ui-components/Button";
import Input from "@opetushallitus/virkailija-ui-components/Input";
import CheckboxGroup from "@opetushallitus/virkailija-ui-components/CheckboxGroup";
import Select from "@opetushallitus/virkailija-ui-components/Select";
import {useContext, useState} from "react";
import {KoodistoContext} from "../../../../../contexts/contexts";
import PohjaModaali from "../../../../Modaalit/PohjaModaali/PohjaModaali";
import TNHeader from "../../../../Modaalit/ToimipisteenNimenmuutos/TNHeader";
import TNBody from "../../../../Modaalit/ToimipisteenNimenmuutos/TNBody";
import TNFooter from "../../../../Modaalit/ToimipisteenNimenmuutos/TNFooter";
import TLHeader from "../../../../Modaalit/ToimipisteenLakkautus/TLHeader";
import TLBody from "../../../../Modaalit/ToimipisteenLakkautus/TLBody";
import TLFooter from "../../../../Modaalit/ToimipisteenLakkautus/TLFooter";

type OrganisaatioProps = {
    organisaatio: any
    language: string
    organisaatioTyypit: any
    maatJaValtiot: any
    opetuskielet: any
}
// TODO optionsmapper ja paranna logiikkaa
export default function PerustietoLomake(props: OrganisaatioProps) {
    const { organisaatio, language, organisaatioTyypit, maatJaValtiot, opetuskielet } = props;
    const [nimenmuutosModaaliAuki, setNimenmuutosModaaliAuki] = useState<boolean>(false);
    const [lakkautusModaaliAuki, setLakkautusModaaliAuki] = useState<boolean>(false);
    const { kuntaKoodisto } = useContext(KoodistoContext);
    const kaikkiKunnat = kuntaKoodisto.koodit().map((k: any) => ({
        value: k.uri,
        label: k.nimi[language] || k.nimi['fi'] || k.nimi['sv'] || k.nimi['en'],
    }));
    console.log('o', organisaatio)
    return(
        <div className={styles.UloinKehys}>
                <div className={styles.Rivi}>
                    <div className={styles.Ruudukko}>
                        <span className={styles.AvainKevyestiBoldattu}> OID</span>
                        <span className={styles.ReadOnly}>{organisaatio.oid}</span>
                            {organisaatio.yritysmuoto && [<span className={styles.AvainKevyestiBoldattu}>Yritysmuoto</span>
                            ,<span className={styles.ReadOnly}>{organisaatio.yritysmuoto}</span>]}
                        <span className={styles.AvainKevyestiBoldattu}>Organisaation nimi</span>
                        <span className={styles.ReadOnly}>{organisaatio.nimi[language] || organisaatio.nimi['fi'] || organisaatio.nimi['sv'] || organisaatio.nimi['en']}</span>
                    </div>
                    <div>
                        <Button
                            className={styles.Nappi}
                            variant="outlined"
                            onClick={() => setNimenmuutosModaaliAuki(true)}
                        >Muokkaa organisaation nimeä</Button>
                    </div>
                </div>
                <div className={styles.Rivi}>
                    <div className={styles.Kentta}>
                        <label>Y-tunnus</label>
                        <Input value={organisaatio.ytunnus || ''}/>
                    </div>
                    <Button className={styles.Nappi} variant="outlined">Päivitä YTJ-tiedot</Button>
                </div>
                <div className={styles.Rivi}>
                    <div className={styles.Kentta}>
                        <label>Organisaatiotyyppi *</label>
                        <CheckboxGroup
                            value={[...organisaatio.tyypit]}
                            options={organisaatioTyypit.map((oT: any) => ({
                                value: oT.uri, label: oT.nimi[language] || oT.nimi['fi'] || oT.nimi['sv'] || oT.nimi['en']
                            }))}
                            onChange={() => {}}
                        />
                    </div>
                </div>
                <div className={styles.Rivi}>
                    <div className={styles.Kentta}>
                        <label>Organisaatio merkitty rekisteriin (perustamispäivämäärä)</label>
                        <Input value={organisaatio.alkuPvm} />
                    </div>
                    <Button
                        className={styles.Nappi}
                        variant="outlined"
                        onClick={() => setLakkautusModaaliAuki(true)}
                    >Merkitse organisaatio lakkautetuksi</Button>
                </div>
                <div className={styles.Rivi}>
                    <div className={styles.Kentta}>
                        <label>Pääsijaintikunta</label>
                        <Select
                            value={kaikkiKunnat.find(kk => kk.value === organisaatio.kotipaikkaUri)}
                            options={kaikkiKunnat}/>
                    </div>
                    <div className={styles.Kentta}>
                        <label>Muut kunnat</label>
                        <Select
                            isMulti
                            value={organisaatio.muutKotipaikatUris.length ? kaikkiKunnat.filter(kk => !!organisaatio.muutKotipaikatUris.find(kk.value)) : []}
                            options={kaikkiKunnat}/>
                    </div>
                </div>
                <div className={styles.Rivi}>
                    <div className={styles.Kentta}>
                        <label>maa</label>
                        <Select
                            value={maatJaValtiot.map((mv: any) => ({
                                value: mv.uri, label: mv.nimi[language] || mv.nimi['fi'] || mv.nimi['sv'] || mv.nimi['en']
                            })).find((mv: any) => {
                                return mv.value === organisaatio.maaUri
                            })}
                            options={maatJaValtiot.map((mv: any) => ({
                                value: mv.uri, label: mv.nimi[language] || mv.nimi['fi'] || mv.nimi['sv'] || mv.nimi['en']
                            }))}/>
                    </div>
                </div>
                <div className={styles.Rivi}>
                    <div className={styles.Kentta}>
                        <label>Opetuskieli</label>
                        <Select
                            value={opetuskielet.map((mv: any) => ({
                                value: mv.uri, label: mv.nimi[language] || mv.nimi['fi'] || mv.nimi['sv'] || mv.nimi['en']
                            })).find((mv: any) => {
                                return organisaatio.kieletUris.find((kU: string) => kU.slice(0, kU.length - 2) === mv.value);
                            })}
                            options={opetuskielet.map((mv: any) => ({
                                value: mv.uri, label: mv.nimi[language] || mv.nimi['fi'] || mv.nimi['sv'] || mv.nimi['en']
                            }))}/>
                    </div>
                </div>{
            nimenmuutosModaaliAuki &&
            <PohjaModaali
                header={<TNHeader/>}
                body={<TNBody/>}
                footer={<TNFooter/>}
                suljeCallback={() => setNimenmuutosModaaliAuki(false)}
            />
            }
            {lakkautusModaaliAuki &&
            <PohjaModaali
                header={<TLHeader/>}
                body={<TLBody/>}
                footer={<TLFooter/>}
                suljeCallback={() => setLakkautusModaaliAuki(false)}
            />
        }
        </div>
    );
}