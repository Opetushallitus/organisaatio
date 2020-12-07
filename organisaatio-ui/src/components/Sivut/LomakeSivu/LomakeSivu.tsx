import * as React from 'react';
import {useContext, useState} from "react";
import styles from './LomakeSivu.module.css';
import PohjaSivu from "../PohjaSivu/PohjaSivu";
import Accordion from "../../Accordion/Accordion"
import Button from "@opetushallitus/virkailija-ui-components/Button";
import Spin from "@opetushallitus/virkailija-ui-components/Spin";

import homeIcon from '@iconify/icons-fa-solid/home';

import {LanguageContext} from "../../../contexts/contexts";
import {Koodi, Organisaatio, OrganisaatioNimiJaOid} from "../../../types/types"
import PerustietoLomake from "./Koulutustoimija/PerustietoLomake/PerustietoLomake";
import YhteystietoLomake from "./Koulutustoimija/YhteystietoLomake/YhteystietoLomake";
import NimiHistoriaLomake from "./Koulutustoimija/NimiHistoriaLomake/NimiHistoriaLomake";
import OrganisaatioHistoriaLomake from "./Koulutustoimija/OrganisaatioHistoriaLomake/OrganisaatioHistoriaLomake";
import {useEffect} from "react";
import Axios from "axios";
import Icon from "@iconify/react";
import useAxios from "axios-hooks";

const urlPrefix = process.env.NODE_ENV === 'development' ? '/api' : '/organisaatio';

const LomakeSivu = (props: any) => {
    const { i18n, language } = useContext(LanguageContext);
    const [{ data: organisaatioTyypit, loading: organisaatioTyypitLoading, error: organisaatioTyypitError}] = useAxios<Koodi[]>(
        `${urlPrefix}/koodisto/ORGANISAATIOTYYPPI/koodi`);
    const [{ data: maatJaValtiot, loading: maatJaValtiotLoading, error: maatJaValtiotError}] = useAxios<Koodi[]>(
        `${urlPrefix}/koodisto/MAATJAVALTIOT1/koodi`);
    const [{ data: oppilaitoksenOpetuskielet, loading: oppilaitoksenOpetuskieletLoading, error: oppilaitoksenOpetuskieletError}] = useAxios<Koodi[]>(
        `${urlPrefix}/koodisto/OPPILAITOKSENOPETUSKIELI/koodi`);
    const [organisaatio, setOrganisaatio] = useState<Organisaatio | undefined>(undefined);
    const [organisaatioNimiPolku, setOrganisaatioNimiPolku] = useState<OrganisaatioNimiJaOid[]>([]);
    const { match: { params } } = props;
    useEffect(() => {
        async function fetch() {
            try {
                const response = await Axios.get(`${urlPrefix}/organisaatio/v4/${params.oid}?includeImage=true`);
                const organisaatio = response.data;
                if (organisaatio.parentOidPath) {
                    const idArr = organisaatio.parentOidPath.split('|').filter((val: string) => val !== "");
                    const orgTree = await Axios.post(`${urlPrefix}/organisaatio/v4/findbyoids`, idArr);
                    console.log('äorgtee', orgTree.data, idArr, organisaatio.parentOidPath);
                    const organisaatioNimiPolku = idArr.map((oid: String) => ({ oid, nimi: orgTree.data.find((o: Organisaatio) => o.oid === oid).nimi }));
                    setOrganisaatioNimiPolku(organisaatioNimiPolku);
                }
                setOrganisaatio(organisaatio);
            } catch (error) {
                console.error('error fetching', error)
            }
        }
        fetch();
    }, [params.oid]);
    if (!organisaatio || organisaatioTyypitLoading || organisaatioTyypitError || maatJaValtiotLoading || maatJaValtiotError || oppilaitoksenOpetuskieletLoading || oppilaitoksenOpetuskieletError) {
        return (<div className={styles.PaaOsio}>
            <Spin>ladataan sivua </Spin>
        </div>);
    }
    return(
        <PohjaSivu>
            <div className={styles.YlaBanneri}>
                <div>
                    <a href="/"><Icon icon={homeIcon} /></a>
                </div>
                {organisaatioNimiPolku.map((o, index) => ([
                    <div>
                        <a href={`/organisaatio/lomake/${o.oid}`}>{o.nimi[language] || o.nimi['fi'] || o.nimi['sv'] || o.nimi['en']}</a>
                    </div>,
                    (organisaatioNimiPolku.length - 1) !== index && <div> > </div>])
                )}
            </div>
            <div className={styles.ValiContainer}>
                <div className={styles.ValiOtsikko}>
                    <h3>{organisaatio.tyypit[0]}</h3>
                    <h1>{organisaatio.nimi[language] || organisaatio.nimi['fi'] || organisaatio.nimi['sv'] || organisaatio.nimi['en']}</h1>
                </div>
                <div className={styles.ValiNappulat}>
                    <Button>{i18n.translate('YHDISTA_ORGANISAATIO')}</Button>
                    <Button>+ {i18n.translate('LISAA_UUSI_OPPILAITOS')}</Button>
                </div>
            </div>
            <div className={styles.PaaOsio} >
                {/*<YhdistysJaSiirto />*/}
                <Accordion
                    lomakkeet={[
                        <PerustietoLomake
                            organisaatioTyypit={organisaatioTyypit}
                            organisaatio={organisaatio}
                            language={language}
                            maatJaValtiot={maatJaValtiot}
                            opetuskielet={oppilaitoksenOpetuskielet}
                        />,
                        <YhteystietoLomake
                            yhteystiedot={organisaatio.yhteystiedot}
                        />,
                        <NimiHistoriaLomake
                            nimet={organisaatio.nimet}
                        />,
                        <OrganisaatioHistoriaLomake
                            oid={organisaatio.oid}
                        />,
                    ]}
                    otsikot={['Perustiedot', 'Yhteystiedot', 'Nimihistoria', 'Organisaatiohistoria']}
                />
                 }
            </div>
            <div className={styles.AlaBanneri}>
                <div className={styles.VersioContainer}>
                    <Button variant="outlined" className={styles.Versionappula}>
                        <span className="material-icons">timeline</span>
                        <span className={styles.VersionappulanTeksti}>{i18n.translate('VERSIOHISTORIA')}</span>
                    </Button>
                    <div className={styles.MuokattuKolumni}>
                        <span>{i18n.translate('MUOKATTU_VIIMEKSI')}</span>
                        <span>01.01.2020 16:39 ngo Schimpff</span>
                    </div>
                </div>
                <div>
                    <Button variant="outlined" className={styles.Versionappula}>{i18n.translate('SULJE_TIEDOT')}
                    </Button>
                    <Button className={styles.Versionappula}>{i18n.translate('TALLENNA')}
                    </Button>
                </div>
            </div>
        </PohjaSivu>
    );
}

export default LomakeSivu;