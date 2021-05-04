import * as React from 'react';
import { useContext, useState } from 'react';
import styles from './LomakeSivu.module.css';
import PohjaSivu from '../PohjaSivu/PohjaSivu';
import Accordion from '../../Accordion/Accordion';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';

import homeIcon from '@iconify/icons-fa-solid/home';

import { LanguageContext } from '../../../contexts/contexts';
import { Koodi, Organisaatio, OrganisaatioNimiJaOid, YtjOrganisaatio } from '../../../types/types';
import PerustietoLomake from './Koulutustoimija/PerustietoLomake/PerustietoLomake';
import YhteystietoLomake from './Koulutustoimija/YhteystietoLomake/YhteystietoLomake';
import NimiHistoriaLomake from './Koulutustoimija/NimiHistoriaLomake/NimiHistoriaLomake';
import OrganisaatioHistoriaLomake from './Koulutustoimija/OrganisaatioHistoriaLomake/OrganisaatioHistoriaLomake';
import { useEffect } from 'react';
import Axios from 'axios';
import Icon from '@iconify/react';
import useAxios from 'axios-hooks';

const urlPrefix = process.env.NODE_ENV === 'development' ? '/api' : '/organisaatio';

const LomakeSivu = (props: any) => {
    const { i18n, language } = useContext(LanguageContext);
    const [{ data: organisaatioTyypit, loading: organisaatioTyypitLoading, error: organisaatioTyypitError }] = useAxios<
        Koodi[]
    >(`${urlPrefix}/koodisto/ORGANISAATIOTYYPPI/koodi`);
    const [{ data: maatJaValtiot, loading: maatJaValtiotLoading, error: maatJaValtiotError }] = useAxios<Koodi[]>(
        `${urlPrefix}/koodisto/MAATJAVALTIOT1/koodi`
    );
    const [
        {
            data: oppilaitoksenOpetuskielet,
            loading: oppilaitoksenOpetuskieletLoading,
            error: oppilaitoksenOpetuskieletError,
        },
    ] = useAxios<Koodi[]>(`${urlPrefix}/koodisto/OPPILAITOKSENOPETUSKIELI/koodi`);
    const [{ data: postinumerot, loading: postinumerotLoading, error: postinumerotError }] = useAxios<Koodi[]>(
        `${urlPrefix}/koodisto/POSTI/koodi?onlyValid=true`
    );
    const [organisaatio, setOrganisaatio] = useState<Organisaatio | undefined>(undefined);
    const [stashedOrganisaatio, setStashedOrganisaatio] = useState<Organisaatio | undefined>(undefined);
    const [organisaatioNimiPolku, setOrganisaatioNimiPolku] = useState<OrganisaatioNimiJaOid[]>([]);
    const {
        match: { params },
    } = props;
    useEffect(() => {
        async function fetch() {
            try {
                const response = await Axios.get(`${urlPrefix}/organisaatio/v4/${params.oid}?includeImage=true`);
                const organisaatio = response.data;
                if (organisaatio.parentOidPath) {
                    const idArr = organisaatio.parentOidPath.split('|').filter((val: string) => val !== '');
                    const orgTree = await Axios.post(`${urlPrefix}/organisaatio/v4/findbyoids`, idArr);
                    console.log('orgtee', orgTree.data, idArr, organisaatio.parentOidPath);
                    const organisaatioNimiPolku = idArr.map((oid: string) => ({
                        oid,
                        nimi: orgTree.data.find((o: Organisaatio) => o.oid === oid).nimi,
                    }));
                    setOrganisaatioNimiPolku(organisaatioNimiPolku);
                }
                setOrganisaatio(Object.assign({}, organisaatio));
            } catch (error) {
                console.error('error fetching', error);
            }
        }
        fetch();
    }, [params.oid]);

    async function putOrganisaatio() {
        try {
            if (organisaatio && organisaatio.oid) {
                const response = await Axios.put(`${urlPrefix}/organisaatio/v4/${organisaatio.oid}`, organisaatio);
                console.log('updated org response', response);
                props.history.push(`/lomake/${organisaatio.oid}`);
            }
        } catch (error) {
            console.error('error while updating org', error);
        } finally {
        }
    }

    // TODO täytyy tarkastaa mitä kaikkea tietoa tuolta Ytj:ltä tuleekaan? esim yrityksen lopetuksesta.
    const setYtjDataFetched = (ytjOrganisaatio: YtjOrganisaatio) => {
        const {
            nimi,
            postiOsoite,
            kayntiOsoite,
            yritysmuoto,
            yritysTunnus: { alkupvm, ytunnus },
        } = ytjOrganisaatio;
        const alkuPvm = alkupvm.split('.');
        [alkuPvm[0], alkuPvm[2]] = [alkuPvm[2], alkuPvm[0]]; // reverse date to YYYY-MM-DD format
        setStashedOrganisaatio(Object.assign({}, organisaatio));
        console.log('tallennettu alkuperäinen org muistiin', stashedOrganisaatio);
        organisaatio &&
            organisaatio.yhteystiedot
                .filter((yT) => yT.kieli === 'kieli_fi#1')
                .forEach((yT: any) => {
                    if (yT.osoiteTyyppi && yT.osoiteTyyppi === 'posti') {
                        const { katu: osoite, postinumero, toimipaikka: postitoimipaikka } = postiOsoite;
                        const postinumeroKoodi = postinumerot.find((p) => p.arvo === postinumero);
                        yT = Object.assign(yT, {
                            osoite,
                            postinumeroUri: (postinumeroKoodi && postinumeroKoodi.uri) || '',
                            postitoimipaikka,
                        });
                    } else if (yT.osoiteTyyppi && yT.osoiteTyyppi === 'kaynti') {
                        console.log(yT, ytjOrganisaatio);
                        const { katu: osoite, postinumero, toimipaikka: postitoimipaikka } = kayntiOsoite;
                        const postinumeroKoodi = postinumerot.find((p) => p.arvo === postinumero);
                        yT = Object.assign(yT, {
                            osoite,
                            postinumeroUri: (postinumeroKoodi && postinumeroKoodi.uri) || '',
                            postitoimipaikka,
                        });
                    } else if (yT.tyyppi && yT.tyyppi === 'puhelin') {
                        console.log(yT, ytjOrganisaatio);
                        yT.numero = ytjOrganisaatio.puhelin;
                    }
                });
        setOrganisaatio(
            Object.assign({}, organisaatio, { nimi: { fi: nimi }, alkuPvm: alkuPvm.join('-'), ytunnus, yritysmuoto })
        ); // TODO nimet?
        console.log('Korvattu org ytj:stä tulevalla datalla ainakin suurelta osin', organisaatio);
    };

    const handleOnChange = ({ name, value }: { name: keyof Organisaatio; value: any }) => {
        setOrganisaatio((organisaatio) => {
            const updatedOrg = Object.assign({}, organisaatio, { [name]: value });
            console.log('päiv', updatedOrg);
            return updatedOrg;
        });
    };
    if (
        !organisaatio ||
        organisaatioTyypitLoading ||
        organisaatioTyypitError ||
        maatJaValtiotLoading ||
        maatJaValtiotError ||
        oppilaitoksenOpetuskieletLoading ||
        oppilaitoksenOpetuskieletError ||
        postinumerotLoading ||
        postinumerotError
    ) {
        return (
            <div className={styles.PaaOsio}>
                <Spin>ladataan sivua </Spin>
            </div>
        );
    }

    return (
        <PohjaSivu>
            <div className={styles.YlaBanneri}>
                <div>
                    <a href="/">
                        <Icon icon={homeIcon} />
                    </a>
                </div>
                {organisaatioNimiPolku.map((o, index) => [
                    <div>
                        <a href={`/organisaatio/lomake/${o.oid}`}>
                            {o.nimi[language] || o.nimi['fi'] || o.nimi['sv'] || o.nimi['en']}
                        </a>
                    </div>,
                    organisaatioNimiPolku.length - 1 !== index && <div> &gt; </div>,
                ])}
            </div>
            <div className={styles.ValiContainer}>
                <div className={styles.ValiOtsikko}>
                    <h3>{organisaatio.tyypit[0]}</h3>
                    <h1>
                        {organisaatio.nimi[language] ||
                            organisaatio.nimi['fi'] ||
                            organisaatio.nimi['sv'] ||
                            organisaatio.nimi['en']}
                    </h1>
                </div>
                <div className={styles.ValiNappulat}>
                    <Button>{i18n.translate('YHDISTA_ORGANISAATIO')}</Button>
                    <Button>+ {i18n.translate('LISAA_UUSI_OPPILAITOS')}</Button>
                </div>
            </div>
            <div className={styles.PaaOsio}>
                {/*<YhdistysJaSiirto />*/}
                <Accordion
                    lomakkeet={[
                        <PerustietoLomake
                            setYtjDataFetched={setYtjDataFetched}
                            handleOnChange={handleOnChange}
                            organisaatioTyypit={organisaatioTyypit}
                            organisaatio={organisaatio}
                            language={language}
                            maatJaValtiot={maatJaValtiot}
                            opetuskielet={oppilaitoksenOpetuskielet}
                        />,
                        <YhteystietoLomake handleOnChange={handleOnChange} yhteystiedot={organisaatio.yhteystiedot} />,
                        <NimiHistoriaLomake handleOnChange={handleOnChange} nimet={organisaatio.nimet} />,
                        <OrganisaatioHistoriaLomake handleOnChange={handleOnChange} oid={organisaatio.oid} />,
                    ]}
                    otsikot={['Perustiedot', 'Yhteystiedot', 'Nimihistoria', 'Organisaatiohistoria']}
                />
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
                    <Button variant="outlined" className={styles.Versionappula} onClick={() => props.history.push('/')}>
                        {i18n.translate('SULJE_TIEDOT')}
                    </Button>
                    <Button className={styles.Versionappula} onClick={putOrganisaatio}>
                        {i18n.translate('TALLENNA')}
                    </Button>
                </div>
            </div>
        </PohjaSivu>
    );
};

export default LomakeSivu;
