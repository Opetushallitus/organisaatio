import React from 'react';
import { useForm } from 'react-hook-form';
import * as yup from 'yup';
import { yupResolver } from '@hookform/resolvers/yup';

import { Header } from '../Header';
import { useKoodistos } from '../KoodistoContext';
import { useJotpaRekisterointiDispatch, useJotpaRekisterointiSelector } from './store';
import { Select } from '../Select';
import { Koodi, SelectOption } from '../types';
import { DatePicker } from '../DatePicker';
import { setAlkamisaika, setKotipaikka, setYritysmuoto } from '../organizationSlice';

import styles from './JotpaOrganization.module.css';

type OrganizationForm = {
    yritysmuoto: SelectOption;
    kotipaikka: SelectOption;
    alkamisaika: Date;
};

const KoodiSchema = (koodit: Koodi[]) =>
    yup
        .object()
        .shape({
            label: yup
                .string()
                .required()
                .oneOf(
                    koodit.map((k) => k.nimi.fi),
                    'Väärä arvo'
                ),
            value: yup
                .string()
                .required()
                .oneOf(
                    koodit.map((k) => k.uri),
                    'Väärä arvo'
                ),
        })
        .required();

const OrganizationSchema = (yritysmuodot: Koodi[], kunnat: Koodi[]): yup.SchemaOf<OrganizationForm> =>
    yup.object().shape({
        yritysmuoto: KoodiSchema(yritysmuodot),
        kotipaikka: KoodiSchema(kunnat),
        alkamisaika: yup.date().required(),
    });

export function JotpaOrganization() {
    const { yritysmuodot, kunnat } = useKoodistos();
    const { loading, initialOrganization } = useJotpaRekisterointiSelector((state) => state.organization);
    const dispatch = useJotpaRekisterointiDispatch();
    const {
        handleSubmit,
        control,
        getValues,
        formState: { errors },
    } = useForm<OrganizationForm>({
        resolver: yupResolver(OrganizationSchema(yritysmuodot, kunnat)),
    });
    if (loading || !initialOrganization || !yritysmuodot) {
        return null;
    }
    const onSubmit = (data: OrganizationForm) => {
        dispatch(setYritysmuoto(data.yritysmuoto));
        dispatch(setKotipaikka(data.kotipaikka));
        dispatch(setAlkamisaika(data.alkamisaika));
    };

    return (
        <form onSubmit={handleSubmit(onSubmit)}>
            <Header title="Koulutuksen järjestäjien rekisteröityminen Jotpaa varten" />
            <main>
                <div className="content">
                    <h2>Organisaation perustiedot</h2>
                    <ul className={styles.infoList}>
                        <li>Tarkista, että tiedot ovat oikein ja täytä puuttuvat kohdat ennen jatkamista.</li>
                        <li>Anna organisaatiolle kuvaava nimi, jos kenttä on tyhjä.</li>
                        <li>
                            Esitäytetyt tiedot tulevat Yritys- ja yhteisötietojärjestelmästä ja Opetushallituksen
                            Organisaatiopalvelusta. Jos esitäytetyissä tiedoissa on virheitä, tiedot tulee päivittää
                            Yritys- ja yhteisötietojärjestelmään tai Organisaatiopalveluun.
                        </li>
                        <li>
                            Palveluntuottajan tiedot tallennetaan Opetushallituksen Organisaatiopalveluun, kun kunta on
                            hyväksynyt rekisteröitymisen. Huomaa, että kaikki kentät ovat pakollisia.
                        </li>
                    </ul>
                    <label>Organisaation nimi</label>
                    <div>{initialOrganization.ytjNimi.nimi}</div>
                    <label>Y-tunnus</label>
                    <div>{initialOrganization.ytunnus}</div>
                    <label>Yritysmuoto *</label>
                    <Select<OrganizationForm>
                        name="yritysmuoto"
                        control={control}
                        error={errors.yritysmuoto?.value}
                        options={yritysmuodot.map((k) => ({ value: k.uri, label: k.nimi.fi || k.uri }))}
                    />
                    <label>Organisaatiotyyppi</label>
                    <div>Koulutuksen järjestäjä</div>
                    <label>Kotipaikka *</label>
                    <Select<OrganizationForm>
                        name="kotipaikka"
                        control={control}
                        error={errors.kotipaikka?.value}
                        options={kunnat.map((k) => ({ value: k.uri, label: k.nimi.fi || k.uri }))}
                    />
                    <label>Toiminnan alkamisaika *</label>
                    <DatePicker<OrganizationForm> name="alkamisaika" control={control} error={errors.alkamisaika} />
                    <h2>Organisaation yhteystiedot</h2>
                    <div className={styles.buttons}>
                        <button
                            role="link"
                            className={styles.cancelButton}
                            onClick={() => (window.location.href = '/hakija/logout?redirect=/jotpa')}
                        >
                            Keskeytä
                        </button>
                        <input type="submit" title="Seuraava vaihe" />
                    </div>
                </div>
                {JSON.stringify(getValues())}
                <br />
                {JSON.stringify(initialOrganization)}
            </main>
        </form>
    );
}
