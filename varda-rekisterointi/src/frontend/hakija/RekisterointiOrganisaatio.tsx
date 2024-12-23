import React, { useContext } from 'react';
import { Organisaatio, Koodi } from '../types/types';
import OrganisaatioYhteystiedot from './OrganisaatioYhteystiedot';
import OrganisaatioTiedot from './OrganisaatioTiedot';
import { LanguageContext } from '../contexts';
import OrganisaatioSahkopostit from './OrganisaatioSahkopostit';
import Fieldset from '../Fieldset';
import OrganisaatioKunnat from './OrganisaatioKunnat';
import AriaVirheMapper from '../virhe/AriaVirheMapper';

type Props = {
    initialOrganisaatio: Organisaatio;
    organisaatio: Organisaatio;
    setOrganisaatio: (organisaatio: Partial<Organisaatio>) => void;
    kaikkiKunnat: Koodi[];
    kunnat: string[];
    setKunnat: (kunnat: string[]) => void;
    sahkopostit: string[];
    setSahkopostit: (sahkopostit: string[]) => void;
    errors: Record<string, string>;
};

export default function RekisterointiOrganisaatio(props: Props) {
    const { i18n } = useContext(LanguageContext);
    return (
        <form>
            {Object.keys(props.errors).length > 0 && (
                <AriaVirheMapper listId="rekisterointi_organisaatio_virheet" errors={props.errors} />
            )}
            <Fieldset
                title={i18n.translate('ORGANISAATION_TIEDOT')}
                description={i18n.translate('ORGANISAATION_TIEDOT_KUVAUS')}
            >
                <OrganisaatioTiedot
                    readOnly={!!props.initialOrganisaatio.oid}
                    kaikkiKunnat={props.kaikkiKunnat}
                    initialOrganisaatio={props.initialOrganisaatio}
                    organisaatio={props.organisaatio}
                    setOrganisaatio={props.setOrganisaatio}
                    errors={props.errors}
                />
            </Fieldset>
            <Fieldset
                title={i18n.translate('ORGANISAATION_YHTEYSTIEDOT')}
                description={i18n.translate('ORGANISAATION_YHTEYSTIEDOT_KUVAUS')}
            >
                <OrganisaatioYhteystiedot
                    readOnly={!!props.initialOrganisaatio.oid}
                    initialOrganisaatio={props.initialOrganisaatio}
                    organisaatio={props.organisaatio}
                    setOrganisaatio={props.setOrganisaatio}
                    errors={props.errors}
                />
            </Fieldset>
            <Fieldset
                title={i18n.translate('ORGANISAATION_KUNNAT')}
                description={i18n.translate('ORGANISAATION_KUNNAT_OHJE')}
            >
                <OrganisaatioKunnat
                    readOnly={false}
                    kaikkiKunnat={props.kaikkiKunnat}
                    kunnat={props.kunnat}
                    setKunnat={props.setKunnat}
                    errors={props.errors}
                />
            </Fieldset>
            <Fieldset
                title={i18n.translate('ORGANISAATION_SAHKOPOSTIT')}
                description={i18n.translate('ORGANISAATION_SAHKOPOSTIT_KUVAUS')}
            >
                <OrganisaatioSahkopostit
                    sahkopostit={props.sahkopostit}
                    setSahkopostit={props.setSahkopostit}
                    errors={props.errors}
                />
            </Fieldset>
        </form>
    );
}
