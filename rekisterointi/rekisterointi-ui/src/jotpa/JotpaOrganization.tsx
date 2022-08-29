import axios from 'axios';
import React, { useEffect, useState } from 'react';

import { Header } from '../Header';
import { useJotpaRekisterointiSelector } from './store';

export function JotpaOrganization() {
    const { loading, organization } = useJotpaRekisterointiSelector((state) => state.organization);
    console.log(organization);
    console.log(loading);
    if (loading || !organization) {
        return null;
    }

    return (
        <>
            <Header title="Koulutuksen järjestäjien rekisteröityminen Jotpaa varten" />
            <main>
                <div className="content">
                    <label>Organisaation nimi</label>
                    <div>{organization.ytjNimi.nimi}</div>
                    <label>Y-tunnus</label>
                    <div>{organization.ytunnus}</div>
                </div>
            </main>
        </>
    );
}
