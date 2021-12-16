import React from 'react';

import { Link } from 'react-router-dom';
import VirkailijaRaamit from '@opetushallitus/virkailija-ui-components/VirkailijaRaamit';

export default function Raamit() {
    const virkailijaRaamitUrl = '/virkailija-raamit/apply-raamit.js';
    if (process.env.NODE_ENV === 'development') {
        return (
            <div style={{ width: '100%', height: '5rem', background: 'blue' }}>
                <h1>RAAMIT</h1>
                <Link to={'/organisaatiot'} style={{ color: 'white' }}>
                    /organisaatiot
                </Link>
                &nbsp;
                <Link to={'/ryhmat'} style={{ color: 'white' }}>
                    /ryhmat
                </Link>
            </div>
        );
    } else {
        return <VirkailijaRaamit scriptUrl={virkailijaRaamitUrl} />;
    }
}
