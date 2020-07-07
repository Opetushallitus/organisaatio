import React, {useContext} from "react";
import {LanguageContext} from "../../../contexts/contexts";

type Props = {

}

export default function TLHeader({ }: Props) {
    const { i18n } = useContext(LanguageContext);
    return (
        <h3>{i18n.translate('TOIMIPISTEEN_LAKKAUTUS')}</h3>
    );
}