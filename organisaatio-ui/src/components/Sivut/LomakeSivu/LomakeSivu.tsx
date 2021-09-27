import * as React from 'react';
import { useContext, useEffect, useState } from 'react';
import styles from './LomakeSivu.module.css';
import PohjaSivu from '../PohjaSivu/PohjaSivu';
import Accordion from '../../Accordion/Accordion';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';

import homeIcon from '@iconify/icons-fa-solid/home';

import { LanguageContext, ROOT_OID } from '../../../contexts/contexts';
import {
    KoodiUri,
    Nimi,
    Organisaatio,
    OrganisaatioNimiJaOid,
    YhdistaOrganisaatioon,
    Yhteystiedot,
    YtjOrganisaatio,
} from '../../../types/types';
import PerustietoLomake from './Koulutustoimija/PerustietoLomake/PerustietoLomake';
import YhteystietoLomake from './Koulutustoimija/YhteystietoLomake/YhteystietoLomake';
import NimiHistoriaLomake from './Koulutustoimija/NimiHistoriaLomake/NimiHistoriaLomake';
import OrganisaatioHistoriaLomake from './Koulutustoimija/OrganisaatioHistoriaLomake/OrganisaatioHistoriaLomake';
import Icon from '@iconify/react';
import { Link } from 'react-router-dom';
import useKoodisto from '../../../api/koodisto';
import { mergeOrganisaatio, readOrganisaatio, updateOrganisaatio } from '../../../api/organisaatio';
import PohjaModaali from '../../Modaalit/PohjaModaali/PohjaModaali';
import TYFooter from '../../Modaalit/ToimipisteenYhdistys/TYFooter';
import TYBody from '../../Modaalit/ToimipisteenYhdistys/TYBody';
import TYHeader from '../../Modaalit/ToimipisteenYhdistys/TYHeader';

type LomakeSivuProps = {
    match: { params: { oid: string } };
    history: string[];
};

const LomakeSivu = ({ match: { params }, history }: LomakeSivuProps) => {
    const { i18n, language } = useContext(LanguageContext);
    const [yhdistaOrganisaatioModaaliAuki, setYhdistaOrganisaatioModaaliAuki] = useState<boolean>(false);
    const [yhdistaOrganisaatio, setYhdistaOrganisaatio] = useState<YhdistaOrganisaatioon>({
        merge: false,
        date: new Date(),
        newParent: undefined,
    });
    const {
        data: organisaatioTyypit,
        loading: organisaatioTyypitLoading,
        error: organisaatioTyypitError,
    } = useKoodisto('ORGANISAATIOTYYPPI');
    const { data: maatJaValtiot, loading: maatJaValtiotLoading, error: maatJaValtiotError } = useKoodisto(
        'MAATJAVALTIOT1'
    );
    const {
        data: oppilaitoksenOpetuskielet,
        loading: oppilaitoksenOpetuskieletLoading,
        error: oppilaitoksenOpetuskieletError,
    } = useKoodisto('OPPILAITOKSENOPETUSKIELI');
    const { data: postinumerot, loading: postinumerotLoading, error: postinumerotError } = useKoodisto('POSTI', true);

    const [organisaatio, setOrganisaatio] = useState<Organisaatio | undefined>(undefined);
    const [organisaatioNimiPolku, setOrganisaatioNimiPolku] = useState<OrganisaatioNimiJaOid[]>([]);
    useEffect(() => {
        (async function () {
            const a = await readOrganisaatio(params.oid);
            if (a) {
                setOrganisaatioNimiPolku(a.polku);
                setOrganisaatio(Object.assign({}, a.organisaatio));
            }
        })();
    }, [params.oid]);
    const handleLisaaUusiToimija = () => {
        return history.push(`/lomake/uusi?parentOid=${organisaatio ? organisaatio.oid : ROOT_OID}`);
    };
    async function handleYhdistaOrganisaatio(props: YhdistaOrganisaatioon) {
        console.log(props);
        if (organisaatio && organisaatio.oid) {
            await mergeOrganisaatio({
                oid: organisaatio.oid,
                ...props,
            });
            const a = await readOrganisaatio(params.oid);
            if (a) {
                setOrganisaatioNimiPolku(a.polku);
                setOrganisaatio(Object.assign({}, a.organisaatio));
            }
        }
    }
    async function putOrganisaatio() {
        if (organisaatio) {
            const data = await updateOrganisaatio(organisaatio);
            if (data) {
                setOrganisaatio(data);
                history.push(`/lomake/${organisaatio.oid}`);
            }
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
        organisaatio &&
            organisaatio.yhteystiedot &&
            organisaatio.yhteystiedot
                .filter((yT) => yT.kieli === 'kieli_fi#1')
                .forEach((yT: any) => {
                    // TODO fixing type is complicated, must refactor types for yhteystiedot from ytj
                    if (yT.osoiteTyyppi && yT.osoiteTyyppi === 'posti') {
                        const { katu: osoite, postinumero, toimipaikka: postitoimipaikka } = postiOsoite;
                        const postinumeroKoodi = postinumerot.find((p) => p.arvo === postinumero);
                        yT = Object.assign(yT, {
                            osoite,
                            postinumeroUri: (postinumeroKoodi && postinumeroKoodi.uri) || '',
                            postitoimipaikka,
                        });
                    } else if (yT.osoiteTyyppi && yT.osoiteTyyppi === 'kaynti') {
                        const { katu: osoite, postinumero, toimipaikka: postitoimipaikka } = kayntiOsoite;
                        const postinumeroKoodi = postinumerot.find((p) => p.arvo === postinumero);
                        yT = Object.assign(yT, {
                            osoite,
                            postinumeroUri: (postinumeroKoodi && postinumeroKoodi.uri) || '',
                            postitoimipaikka,
                        });
                    } else if (yT.tyyppi && yT.tyyppi === 'puhelin') {
                        yT.numero = ytjOrganisaatio.puhelin;
                    }
                });
        setOrganisaatio(
            Object.assign({}, organisaatio, { nimi: { fi: nimi }, alkuPvm: alkuPvm.join('-'), ytunnus, yritysmuoto })
        ); // TODO nimet?
    };

    const handleOnChange = ({
        name,
        value,
    }: {
        name: keyof Organisaatio;
        value: { nimi: Nimi; alkuPvm: string }[] | Nimi | KoodiUri[] | Date | KoodiUri | Yhteystiedot[];
    }) => {
        setOrganisaatio((organisaatio) => {
            const updatedOrg = Object.assign({}, organisaatio, { [name]: value });
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
                key={'perustietolomake'}
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
                <YhteystietoLomake
                    key={'yhteystietolomake'}
                    handleOnChange={handleOnChange}
                    yhteystiedot={organisaatio.yhteystiedot}
                />
            );
            otsikot.push(i18n.translate('LOMAKE_YHTEYSTIEDOT'));
        }
        lomakkeet.push(<NimiHistoriaLomake key={'nimihistorialomake'} nimet={organisaatio.nimet} />);
        otsikot.push(i18n.translate('LOMAKE_NIMIHISTORIA'));

        if (organisaatio.oid !== ROOT_OID && organisaatio.oid) {
            lomakkeet.push(<OrganisaatioHistoriaLomake key={'organisaatiohistorialomake'} oid={organisaatio.oid} />);
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
                    <div key={o.oid}>
                        <Link to={`${o.oid}`}>{o.nimi[language] || o.nimi['fi'] || o.nimi['sv'] || o.nimi['en']}</Link>
                    </div>,
                    organisaatioNimiPolku.length - 1 !== index && <div> &gt; </div>,
                ])}
            </div>
            <div className={styles.ValiContainer}>
                <div className={styles.ValiOtsikko}>
                    <h3>{organisaatio.tyypit ? organisaatio.tyypit[0] : i18n.translate('LABEL_NOT_AVAILABLE')}</h3>
                    <h1>
                        {organisaatio.nimi[language] ||
                            organisaatio.nimi['fi'] ||
                            organisaatio.nimi['sv'] ||
                            organisaatio.nimi['en']}
                    </h1>
                </div>
                <div className={styles.ValiNappulat}>
                    <Button onClick={() => setYhdistaOrganisaatioModaaliAuki(true)}>
                        {i18n.translate('LOMAKE_YHDISTA_ORGANISAATIO')}
                    </Button>
                    <Button onClick={handleLisaaUusiToimija}>{i18n.translate('LOMAKE_LISAA_UUSI_TOIMIJA')}</Button>
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
                    <Button variant="outlined" className={styles.Versionappula} onClick={() => history.push('/')}>
                        {i18n.translate('BUTTON_SULJE')}
                    </Button>
                    <Button className={styles.Versionappula} onClick={putOrganisaatio}>
                        {i18n.translate('BUTTON_TALLENNA')}
                    </Button>
                </div>
            </div>
            {yhdistaOrganisaatioModaaliAuki && (
                <PohjaModaali
                    header={<TYHeader />}
                    body={
                        <TYBody
                            organisaatio={organisaatio}
                            yhdistaOrganisaatio={yhdistaOrganisaatio}
                            handleChange={setYhdistaOrganisaatio}
                        />
                    }
                    footer={
                        <TYFooter
                            tallennaCallback={() => {
                                handleYhdistaOrganisaatio(yhdistaOrganisaatio);
                                setYhdistaOrganisaatioModaaliAuki(false);
                            }}
                            peruutaCallback={() => {
                                setYhdistaOrganisaatioModaaliAuki(false);
                            }}
                        />
                    }
                    suljeCallback={() => setYhdistaOrganisaatioModaaliAuki(false)}
                />
            )}
        </PohjaSivu>
    );
};

export default LomakeSivu;
