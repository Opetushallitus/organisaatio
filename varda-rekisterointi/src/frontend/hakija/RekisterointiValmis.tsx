import React from 'react';

export default function RekisterointiValmis() {
    return (
        <form>
            <fieldset className="oph-fieldset">
                <legend className="oph-label">Rekisteröinnin käsittely</legend>
            </fieldset>
            <div>
                Rekisteröitymisessä tallennetut varhaiskasvatustoimijan tiedot on lähetetty käsiteltäviksi.
                Käsittelijänä toimii organisaatiosi kunta.
                Vahvistus käsittelystä lähetetään kaikille niille henkilöille, joiden yhteystiedot on annettu rekisteröitymisessä.
            </div>
            <div>Voit sulkea tämän sivun.</div>
        </form>
    );
}
