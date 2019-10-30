import React, {useContext, useEffect, useState} from "react";
import {LanguageContext} from '../contexts';
import Button from "@opetushallitus/virkailija-ui-components/Button";

type Props = {
    valitut: number[]
}

export default function PaatosKontrollit({ valitut }: Props) {
    const { i18n } = useContext(LanguageContext);
    const [kaytossa, asetaKaytossa] = useState(false);

    function hylkaa() {

    }

    function hyvaksy() {

    }

    useEffect(() => {
        asetaKaytossa(valitut && valitut.length > 0);
    }, [valitut]);

    return (
        <div>
            <Button disabled={!kaytossa} onClick={hylkaa}>
                {i18n.translate('REKISTEROINNIT_HYLKAA_VALITUT')}
            </Button>
            <Button disabled={!kaytossa} onClick={hyvaksy}>
                {i18n.translate('REKISTEROINNIT_HYVAKSY_VALITUT')}
            </Button>
        </div>
    );
}
