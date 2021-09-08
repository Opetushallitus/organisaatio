import * as React from 'react';
import { useContext, useEffect, useState } from 'react';
import styles from './LomakeSivu.module.css';
import PohjaSivu from '../PohjaSivu/PohjaSivu';
import Accordion from '../../Accordion/Accordion';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';

import homeIcon from '@iconify/icons-fa-solid/home';

import { LanguageContext, ROOT_OID } from '../../../contexts/contexts';
import { Koodi, Organisaatio, OrganisaatioNimiJaOid, YtjOrganisaatio } from '../../../types/types';
import PerustietoLomake from './Koulutustoimija/PerustietoLomake/PerustietoLomake';
import YhteystietoLomake from './Koulutustoimija/YhteystietoLomake/YhteystietoLomake';
import NimiHistoriaLomake from './Koulutustoimija/NimiHistoriaLomake/NimiHistoriaLomake';
import OrganisaatioHistoriaLomake from './Koulutustoimija/OrganisaatioHistoriaLomake/OrganisaatioHistoriaLomake';
import Axios from 'axios';
import Icon from '@iconify/react';
import useAxios from 'axios-hooks';
import { Link } from 'react-router-dom';
type LomakeSivuProps = {
    match: { params: { oid: string } };
    history: string[];
};
const LomakeSivu = (props: LomakeSivuProps) => {
    const { i18n, language } = useContext(LanguageContext);
    const [{ data: organisaatioTyypit, loading: organisaatioTyypitLoading, error: organisaatioTyypitError }] = useAxios<
        Koodi[]
    >(`/organisaatio/koodisto/ORGANISAATIOTYYPPI/koodi`);
    const [{ data: maatJaValtiot, loading: maatJaValtiotLoading, error: maatJaValtiotError }] = useAxios<Koodi[]>(
        `/organisaatio/koodisto/MAATJAVALTIOT1/koodi`
    );
    const [
        {
            data: oppilaitoksenOpetuskielet,
            loading: oppilaitoksenOpetuskieletLoading,
            error: oppilaitoksenOpetuskieletError,
        },
    ] = useAxios<Koodi[]>(`/organisaatio/koodisto/OPPILAITOKSENOPETUSKIELI/koodi`);
    const [{ data: postinumerot, loading: postinumerotLoading, error: postinumerotError }] = useAxios<Koodi[]>(
        `/organisaatio/koodisto/POSTI/koodi?onlyValid=true`
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
                const response = await Axios.get(`/organisaatio/organisaatio/v4/${params.oid}?includeImage=true`);
                const organisaatio = response.data;
                if (organisaatio.parentOidPath) {
                    const idArr = organisaatio.parentOidPath.split('|').filter((val: string) => val !== '');
                    const orgTree = await Axios.post(`/organisaatio/organisaatio/v4/findbyoids`, idArr);
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
                const response = await Axios.put(`/organisaatio/organisaatio/v4/${organisaatio.oid}`, organisaatio);
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
            organisaatio.yhteystiedot &&
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

    const accordionProps = () => {
        const lomakkeet = [] as React.ReactElement[];
        const otsikot = [] as string[];
        lomakkeet.push(
            <PerustietoLomake
                setYtjDataFetched={setYtjDataFetched}
                handleOnChange={handleOnChange}
                organisaatioTyypit={organisaatioTyypit}
                organisaatio={organisaatio}
                language={language}
                maatJaValtiot={maatJaValtiot}
                opetuskielet={oppilaitoksenOpetuskielet}
            />
        );
        otsikot.push(i18n.translate('LOMAKE_PERUSTIEDOT'));
        if (organisaatio.yhteystiedot) {
            lomakkeet.push(
                <YhteystietoLomake handleOnChange={handleOnChange} yhteystiedot={organisaatio.yhteystiedot} />
            );
            otsikot.push(i18n.translate('LOMAKE_YHTEYSTIEDOT'));
        }
        lomakkeet.push(<NimiHistoriaLomake handleOnChange={handleOnChange} nimet={organisaatio.nimet} />);
        otsikot.push(i18n.translate('LOMAKE_NIMIHISTORIA'));

        if (organisaatio.oid !== ROOT_OID) {
            lomakkeet.push(<OrganisaatioHistoriaLomake handleOnChange={handleOnChange} oid={organisaatio.oid} />);
            otsikot.push(i18n.translate('LOMAKE_RAKENNE'));
        }

        return { lomakkeet: lomakkeet, otsikot: otsikot };
    };

    return (
        <PohjaSivu>
            <div className={styles.YlaBanneri}>
                <div>
                    <Link to="/">
                        <Icon icon={homeIcon} />
                    </Link>
                </div>
                {organisaatioNimiPolku.map((o, index) => [
                    <div>
                        <Link to={`${o.oid}`}>{o.nimi[language] || o.nimi['fi'] || o.nimi['sv'] || o.nimi['en']}</Link>
                    </div>,
                    organisaatioNimiPolku.length - 1 !== index && <div> &gt; </div>,
                ])}
            </div>
            <div className={styles.ValiContainer}>
                <div className={styles.ValiOtsikko}>
                    <h3>{organisaatio.tyypit ? organisaatio.tyypit[0] : 'N/A'}</h3>
                    <h1>
                        {organisaatio.nimi[language] ||
                            organisaatio.nimi['fi'] ||
                            organisaatio.nimi['sv'] ||
                            organisaatio.nimi['en']}
                    </h1>
                </div>
                <div className={styles.ValiNappulat}>
                    <Button>{i18n.translate('LOMAKE_YHDISTA_ORGANISAATIO')}</Button>
                    <Button>+ {i18n.translate('LOMAKE_LISAA_UUSI_OPPILAITOS')}</Button>
                </div>
            </div>
            <div className={styles.PaaOsio}>
                {/*<YhdistysJaSiirto />*/}
                <Accordion {...accordionProps()} />
            </div>
            <div className={styles.AlaBanneri}>
                <div className={styles.VersioContainer}>
                    <Button variant="outlined" className={styles.Versionappula}>
                        <span className="material-icons">timeline</span>
                        <span className={styles.VersionappulanTeksti}>{i18n.translate('BUTTON_VERSIOHISTORIA')}</span>
                    </Button>
                    <div className={styles.MuokattuKolumni}>
                        <span>{i18n.translate('VERSIOHISTORIA_MUOKATTU_VIIMEKSI')}</span>
                        <span>01.01.2020 16:39 ngo Schimpff</span>
                    </div>
                </div>
                <div>
                    <Button variant="outlined" className={styles.Versionappula} onClick={() => props.history.push('/')}>
                        {i18n.translate('BUTTON_SULJE')}
                    </Button>
                    <Button className={styles.Versionappula} onClick={putOrganisaatio}>
                        {i18n.translate('BUTTON_TALLENNA')}
                    </Button>
                </div>
            </div>
        </PohjaSivu>
    );
};

export default LomakeSivu;
