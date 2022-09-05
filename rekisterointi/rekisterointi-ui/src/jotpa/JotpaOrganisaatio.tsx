import React, { useMemo } from 'react';
import { useFieldArray, useForm } from 'react-hook-form';
import { useNavigate } from 'react-router-dom';
import { yupResolver } from '@hookform/resolvers/yup';

import { Header } from '../Header';
import { useKoodistos } from '../KoodistoContext';
import { useJotpaRekisterointiDispatch, useJotpaRekisterointiSelector } from './store';
import { Select } from '../Select';
import { DatePicker } from '../DatePicker';
import { OrganizationFormState, OrganizationSchema, setForm } from '../organizationSlice';
import { Input } from '../Input';

import styles from './jotpa.module.css';
import { FormError } from '../FormError';
import { useLanguageContext } from '../LanguageContext';
import { findPostitoimipaikka } from '../addressUtils';

const AddEmailLogo = () => (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path fillRule="evenodd" clipRule="evenodd" d="M19 13H13V19H11V13H5V11H11V5H13V11H19V13Z" fill="#3A7A10" />
    </svg>
);

const RemoveEmailLogo = () => (
    <svg width="14" height="18" viewBox="0 0 14 18" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path
            fillRule="evenodd"
            clipRule="evenodd"
            d="M1 16C1 17.1 1.9 18 3 18H11C12.1 18 13 17.1 13 16V4H1V16ZM3 6H11V16H3V6ZM10.5 1L9.5 0H4.5L3.5 1H0V3H14V1H10.5Z"
            fill="#4C4C4C"
        />
    </svg>
);

export function JotpaOrganisaatio() {
    const { language } = useLanguageContext();
    const navigate = useNavigate();
    const { yritysmuodot, kunnat, posti, postinumerot } = useKoodistos();
    const { loading, initialOrganization, form } = useJotpaRekisterointiSelector((state) => state.organization);
    const dispatch = useJotpaRekisterointiDispatch();
    const {
        control,
        formState: { errors },
        handleSubmit,
        register,
        watch,
    } = useForm<OrganizationFormState>({
        defaultValues: useMemo(() => {
            return form;
        }, [form]),
        resolver: yupResolver(OrganizationSchema(yritysmuodot, kunnat, postinumerot, language)),
    });

    const {
        fields: emailFields,
        append: appendEmail,
        remove: removeEmail,
    } = useFieldArray<OrganizationFormState>({
        control,
        name: 'emails',
    });
    if (emailFields.length === 0) {
        appendEmail({ email: '' }, { shouldFocus: false });
    }

    if (loading || !initialOrganization || !yritysmuodot) {
        return null;
    }
    const onSubmit = (data: OrganizationFormState) => {
        const kayntiosoite = !data.copyKayntiosoite
            ? {
                  kayntiosoite: data.kayntiosoite!,
                  kayntipostinumero: data.kayntipostinumero!,
                  kayntipostitoimipaikka: kayntipostitoimipaikka!,
              }
            : {
                  kayntiosoite: data.postiosoite,
                  kayntipostinumero: data.postinumero,
                  kayntipostitoimipaikka: postitoimipaikka,
              };
        const formState: OrganizationFormState = {
            yritysmuoto: data.yritysmuoto,
            kotipaikka: data.kotipaikka,
            alkamisaika: data.alkamisaika,
            puhelinnumero: data.puhelinnumero,
            email: data.email,
            emails: data.emails.filter((e) => !!e.email) as { email: string }[],
            postiosoite: data.postiosoite,
            postinumero: data.postinumero,
            copyKayntiosoite: data.copyKayntiosoite,
            ...kayntiosoite,
        };

        dispatch(setForm(formState));
        navigate('/hakija/jotpa/paakayttaja');
    };

    const preventDefault = (fn: () => void) => (e: React.MouseEvent<HTMLButtonElement>) => {
        e.preventDefault();
        e.stopPropagation();
        fn();
    };

    const postinumero = watch('postinumero');
    const copyKayntiosoite = watch('copyKayntiosoite');
    const kayntipostinumero = watch('kayntipostinumero');
    const postitoimipaikka = findPostitoimipaikka(postinumero, posti, language);
    const kayntipostitoimipaikka = kayntipostinumero && findPostitoimipaikka(kayntipostinumero, posti, language);
    return (
        <>
            <Header title="Koulutuksen järjestäjien rekisteröityminen Jotpaa varten" />
            <form onSubmit={handleSubmit(onSubmit)}>
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
                                Palveluntuottajan tiedot tallennetaan Opetushallituksen Organisaatiopalveluun, kun kunta
                                on hyväksynyt rekisteröitymisen. Huomaa, että kaikki kentät ovat pakollisia.
                            </li>
                        </ul>
                        <div className="label">Organisaation nimi</div>
                        <div>{initialOrganization.ytjNimi.nimi}</div>
                        <div className="label">Y-tunnus</div>
                        <div>{initialOrganization.ytunnus}</div>
                        <label className="title" htmlFor="yritysmuoto">
                            Yritysmuoto *
                        </label>
                        <Select<OrganizationFormState>
                            name="yritysmuoto"
                            control={control}
                            error={errors.yritysmuoto?.value}
                            options={yritysmuodot.map((k) => ({ value: k.uri, label: k.nimi[language] || k.uri }))}
                        />
                        <div className="label">Organisaatiotyyppi</div>
                        <div>Koulutuksen järjestäjä</div>
                        <label className="title" htmlFor="kotipaikka">
                            Kotipaikka *
                        </label>
                        <Select<OrganizationFormState>
                            name="kotipaikka"
                            control={control}
                            error={errors.kotipaikka?.value}
                            options={kunnat.map((k) => ({ value: k.uri, label: k.nimi[language] || k.uri }))}
                        />
                        <label className="title" htmlFor="alkamisaika">
                            Toiminnan alkamisaika *
                        </label>
                        <DatePicker<OrganizationFormState>
                            name="alkamisaika"
                            control={control}
                            error={errors.alkamisaika}
                        />
                        <h2>Organisaation yhteystiedot</h2>
                        <div className={styles.info}>
                            Tarkista, että tiedot ovat oikein ja täytä puuttuvat kohdat ennen jatkamista.
                            Palveluntuottajan yhteystiedot tallennetaan Opetushallituksen Organisaatiopalveluun, kun
                            kunta on hyväksynyt rekisteröitymisen.
                        </div>
                        <label className="title" htmlFor="puhelinnumero">
                            Puhelinnumero *
                        </label>
                        <Input name="puhelinnumero" register={register} error={errors.puhelinnumero} />
                        <label className="title" htmlFor="email">
                            Yhteiskäyttöinen sähköpostiosoite *
                        </label>
                        <Input name="email" register={register} error={errors.email} />
                        <label className="title" htmlFor="postiosoite">
                            Postiosoite *
                        </label>
                        <Input name="postiosoite" register={register} error={errors.postiosoite} />
                        <label className="title" htmlFor="postinumero">
                            Postinumero *
                        </label>
                        <Input name="postinumero" register={register} error={errors.postinumero} />
                        <label className="title">Postitoimipaikka</label>
                        <div className={styles.postitoimipaikka}>{postitoimipaikka}</div>
                        <div className={styles.copyKayntiosoite}>
                            <label htmlFor="copyKayntiosoite">
                                <input id="copyKayntiosoite" type="checkbox" {...register('copyKayntiosoite')} />{' '}
                                Käyntiosoite on sama kuin postiosoite
                            </label>
                        </div>
                        {!copyKayntiosoite && (
                            <>
                                <label className="title" htmlFor="kayntiosoite">
                                    Käyntiosoite *
                                </label>
                                <Input
                                    name="kayntiosoite"
                                    required={false}
                                    register={register}
                                    error={errors.kayntiosoite}
                                />
                                <label className="title" htmlFor="kayntipostinumero">
                                    Käyntiosoitteen postinumero *
                                </label>
                                <Input
                                    name="kayntipostinumero"
                                    required={false}
                                    register={register}
                                    error={errors.kayntipostinumero}
                                />
                                <label className="title">Käyntiosoitteen postitoimipaikka</label>
                                <div className={styles.postitoimipaikka}>{kayntipostitoimipaikka}</div>
                            </>
                        )}
                        <h2>Sähköpostiosoite</h2>
                        <ul className={styles.infoList}>
                            <li>
                                Syötä yhden tai useamman henkilön sähköpostiosoite, jota käytetään palveluntuottajan
                                rekisteröitymiseen liittyvässä viestinnässä.
                            </li>
                            <li>
                                Sähköposteja lähetetään rekisteröitymisen vastaanottamisesta sekä rekisteröitymisen
                                hyväksymisestä tai hylkäämisestä.
                            </li>
                            <li>Sähköpostiosoitetta ei tallenneta Organisaatiopalveluun.</li>
                        </ul>
                        <label className="title" htmlFor="firstEmail">
                            Sähköpostiosoite *
                        </label>
                        <FormError error={errors?.emails?.message} />
                        {emailFields.map((field, index) => {
                            const error = errors.emails?.[index]?.email;
                            return (
                                <div key={field.id}>
                                    <div className={styles.emailRow}>
                                        <input
                                            id={index === 0 ? 'firstEmail' : undefined}
                                            className={`${styles.emailInput} ${error ? styles.errorInput : ''}`}
                                            type="text"
                                            {...register(`emails.${index}.email`)}
                                        />
                                        {index === 0 ? (
                                            <div className={styles.removeEmailPlaceholder} />
                                        ) : (
                                            <button
                                                className={styles.removeEmailButton}
                                                onClick={preventDefault(() => removeEmail(index))}
                                                aria-label="Poista sähköposti"
                                            >
                                                <RemoveEmailLogo />
                                            </button>
                                        )}
                                    </div>
                                    <FormError error={error?.message} />
                                </div>
                            );
                        })}
                        <button
                            className={styles.addEmailButton}
                            onClick={preventDefault(() => appendEmail({ email: '' }))}
                        >
                            <AddEmailLogo />
                            Lisää sähköpostiosoite
                        </button>
                        <div className={styles.buttons}>
                            <button
                                role="link"
                                className={styles.cancelButton}
                                onClick={() => (window.location.href = '/hakija/logout?redirect=/jotpa')}
                            >
                                Keskeytä
                            </button>
                            <input type="submit" value="Seuraava vaihe" />
                        </div>
                    </div>
                </main>
            </form>
        </>
    );
}
