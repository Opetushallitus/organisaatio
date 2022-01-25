import PohjaModaali from '../PohjaModaali/PohjaModaali';
import * as React from 'react';
import { useContext, useState } from 'react';
import { Confirmation } from '../Confirmation/Confirmation';
import { LiitaOrganisaatioon, UiOrganisaatioBase } from '../../../types/types';
import Header from '../Header/Header';
import Footer from '../Footer/Footer';
import LiitosBody from './LiitosBody';
import { LanguageContext } from '../../../contexts/LanguageContext';

export function LiitaOrganisaatio({
    liitaOrganisaatioon,
    organisaatioBase,
    handleChange,
    tallennaCallback,
    peruutaCallback,
    suljeCallback,
    targetType,
    labels,
}: {
    liitaOrganisaatioon: LiitaOrganisaatioon;
    organisaatioBase: UiOrganisaatioBase;
    handleChange: (value: ((prevState: LiitaOrganisaatioon) => LiitaOrganisaatioon) | LiitaOrganisaatioon) => void;
    tallennaCallback: () => void;
    peruutaCallback: () => void;
    suljeCallback: () => void;
    targetType: string;
    labels: { title: string; confirmTitle: string; confirmMessage: string; otherOrg: string; liitosPvm: string };
}) {
    const { i18n } = useContext(LanguageContext);
    const [confirmationModaaliAuki, setConfirmationModaaliAuki] = useState<boolean>(false);
    const { currentNimi, apiOrganisaatio, oid } = organisaatioBase;
    const fromIdentifier = apiOrganisaatio.ytunnus || apiOrganisaatio.oppilaitosKoodi || oid;
    const toIdentifier =
        liitaOrganisaatioon.newParent?.ytunnus ||
        liitaOrganisaatioon.newParent?.oppilaitosKoodi ||
        liitaOrganisaatioon.newParent?.oid;
    return (
        <>
            {!confirmationModaaliAuki && (
                <PohjaModaali
                    header={<Header label={labels.title} />}
                    body={
                        <LiitosBody
                            organisaatioBase={organisaatioBase}
                            liitaOrganisaatio={liitaOrganisaatioon}
                            handleChange={handleChange}
                            targetType={targetType}
                            labels={labels}
                        />
                    }
                    footer={
                        <Footer
                            tallennaCallback={() => setConfirmationModaaliAuki(true)}
                            peruutaCallback={peruutaCallback}
                        />
                    }
                    suljeCallback={suljeCallback}
                />
            )}
            {confirmationModaaliAuki && (
                <Confirmation
                    header={labels.confirmTitle}
                    message={labels.confirmMessage}
                    replacements={[
                        {
                            key: 'from',
                            value: `${i18n.translateNimi(currentNimi?.nimi)} (${fromIdentifier})
                            }`,
                        },
                        {
                            key: 'to',
                            value: `${i18n.translateNimi(liitaOrganisaatioon.newParent?.nimi)} (${toIdentifier})`,
                        },
                    ]}
                    tallennaCallback={() => {
                        setConfirmationModaaliAuki(false);
                        tallennaCallback();
                    }}
                    peruutaCallback={() => setConfirmationModaaliAuki(false)}
                    suljeCallback={() => setConfirmationModaaliAuki(false)}
                />
            )}
        </>
    );
}
