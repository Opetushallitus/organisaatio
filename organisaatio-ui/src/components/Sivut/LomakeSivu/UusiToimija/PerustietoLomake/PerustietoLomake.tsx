import * as React from 'react';
import styles from './PerustietoLomake.module.css';
import Button from "@opetushallitus/virkailija-ui-components/Button";
import Input from "@opetushallitus/virkailija-ui-components/Input";
import CheckboxGroup from "@opetushallitus/virkailija-ui-components/CheckboxGroup";
import Select from "@opetushallitus/virkailija-ui-components/Select";
import {Dispatch, SetStateAction, useContext, useState} from "react";
import {KoodistoContext, LanguageContext} from "../../../../../contexts/contexts";
import PohjaModaali from "../../../../Modaalit/PohjaModaali/PohjaModaali";
import TNHeader from "../../../../Modaalit/ToimipisteenNimenmuutos/TNHeader";
import TNBody from "../../../../Modaalit/ToimipisteenNimenmuutos/TNBody";
import TNFooter from "../../../../Modaalit/ToimipisteenNimenmuutos/TNFooter";
import RadioGroup from "@opetushallitus/virkailija-ui-components/RadioGroup";
import {AccordionItemButton} from "react-accessible-accordion";
import {Organisaatio} from "../../../../../types/types";

type OrganisaatioProps = {
    organisaatio: any
    organisaatioTyypit: any
    maatJaValtiot: any
    opetuskielet: any
    setOrganisaatio?: Dispatch<SetStateAction<Organisaatio>>
}
// TODO optionsmapper ja paranna logiikkaa
export default function PerustietoLomake(props: OrganisaatioProps) {
    const { i18n, language } = useContext(LanguageContext);
    const { organisaatio, organisaatioTyypit, maatJaValtiot, opetuskielet } = props;
    const [nimenmuutosModaaliAuki, setNimenmuutosModaaliAuki] = useState<boolean>(false);
    const [onYunnus, setOnYtunnus] = useState<boolean>(true);
    const { kuntaKoodisto } = useContext(KoodistoContext);
    const kaikkiKunnat = kuntaKoodisto.koodit().map((k: any) => ({
        value: k.uri,
        label: k.nimi[language] || k.nimi['fi'] || k.nimi['sv'] || k.nimi['en'],
    }));
    console.log('o', organisaatio)
    return(
        <div className={styles.UloinKehys}>
            <div className={styles.Rivi}>
                <div className={styles.Kentta}>
                    <RadioGroup
                        value={onYunnus.toString() }
                        options={[
                            { value: 'true', label: i18n.translate('ORGANISAATIOLLA_ON_YTUNNUS')},
                            { value: 'false', label: i18n.translate('ORGANISAATIOLLA_EI_YTUNNUS')},
                        ]}
                        onChange={e => setOnYtunnus(e.target.value)}
                    />
                </div>
            </div>
                <div className={styles.Rivi}>
                    <div>
                        <div className={styles.Ruudukko}>
                            <span className={styles.AvainKevyestiBoldattu}>Yritysmuoto</span>
                            <span className={styles.ReadOnly}>ym</span>
                            <span className={styles.AvainKevyestiBoldattu}>Organisaation nimi</span>
                            <span className={styles.ReadOnly}>{organisaatio.nimi[language] || organisaatio.nimi['fi'] || organisaatio.nimi['sv'] || organisaatio.nimi['en']}</span>
                        </div>
                        <Button
                            className={styles.Nappi}
                            variant="outlined"
                            onClick={() => setNimenmuutosModaaliAuki(true)}
                        >{i18n.translate('MUOKKAA_ORGANISAATION_NIMEA')}</Button>
                    </div>
                </div>
                <div className={styles.Rivi}>
                    <div className={styles.Kentta}>
                        <label>Y-tunnus</label>
                        <Input value={organisaatio.ytunnus}/>
                    </div>
                    <Button className={styles.Nappi} variant="outlined">{i18n.translate('HAE_YTJ_TIEDOT')}</Button>
                </div>
                <div className={styles.Rivi}>
                    <div className={styles.Kentta}>
                        <label>{i18n.translate('ORGANISAATIOTYYPPI')}</label>
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
                        <label>{i18n.translate('ORGANISAATIO_PERUSTAMISPAIVA')}</label>
                        <Input value={organisaatio.alkuPvm} />
                    </div>
                    <div className={styles.Kentta}>
                        <label>{i18n.translate('ORGANISAATIO_PERUSTAMISPAIVA')} (MIKÄ TÄN KUULUU OLLA?)</label>
                        <Input value={organisaatio.alkuPvm} />
                    </div>
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
                            isMulti
                            value={opetuskielet.map((mv: any) => ({
                                value: mv.uri, label: mv.nimi[language] || mv.nimi['fi'] || mv.nimi['sv'] || mv.nimi['en']
                            })).find((mv: any) => {
                                return organisaatio.kieletUris.find((kU: string) => kU.slice(0, kU.length - 2) === mv.value);
                            })}
                            options={opetuskielet.map((mv: any) => ({
                                value: mv.uri, label: mv.nimi[language] || mv.nimi['fi'] || mv.nimi['sv'] || mv.nimi['en']
                            }))}/>
                    </div>
                </div>
            <div>
                <AccordionItemButton>
                    <Button>{i18n.translate('JATKA')}</Button>
                </AccordionItemButton>
            </div>
            {
            nimenmuutosModaaliAuki &&
            <PohjaModaali
                header={<TNHeader/>}
                body={<TNBody/>}
                footer={<TNFooter/>}
                suljeCallback={() => setNimenmuutosModaaliAuki(false)}
            />
            }
        </div>
    );
}