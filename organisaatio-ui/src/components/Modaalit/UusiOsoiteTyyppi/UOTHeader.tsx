import React, {useContext} from "react";
import {LanguageContext} from "../../../contexts/contexts";

type Props = {

}

export default function UOTHeader({ }: Props) {
    const { i18n } = useContext(LanguageContext);
    return (
        <h3>{i18n.translate('UUDEN_OSOITETYYPIN_LISAYS')}</h3>
    );
}