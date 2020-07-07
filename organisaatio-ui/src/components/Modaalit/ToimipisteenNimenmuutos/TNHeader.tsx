import React, {useContext} from "react";
import {LanguageContext} from "../../../contexts/contexts";

type Props = {

}

export default function TNHeader({ }: Props) {
    const { i18n } = useContext(LanguageContext);
    return (
        <h3>{i18n.translate('TOIMIPISTEEN_NIMENMUUTOS')}</h3>
    );
}