import React, {useContext} from "react";
import {LanguageContext} from "../contexts";

import Checkbox from "@opetushallitus/virkailija-ui-components/Checkbox";

type Props = {
    kaikkiValittu: boolean
    kaikkiValittuCallback: () => void
}

export default function RekisterointiListaOtsikko({ kaikkiValittu, kaikkiValittuCallback }: Props) {
    const { i18n } = useContext(LanguageContext);

    return (
        <thead>
            <tr key="otsikko">
                <th><Checkbox checked={kaikkiValittu} onChange={_ => kaikkiValittuCallback()}/></th>
                <th>{i18n.translate("ORGANISAATION_NIMI")}</th>
                <th>{i18n.translate("VASTUUHENKILO")}</th>
                <th>{i18n.translate("YTUNNUS")}</th>
                <th>{i18n.translate("SAAPUMISAIKA")}</th>
            </tr>
        </thead>
    )
}
