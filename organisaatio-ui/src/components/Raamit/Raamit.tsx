import React from 'react';
import VirkailijaRaamit from '@opetushallitus/virkailija-ui-components/VirkailijaRaamit';

export default function Raamit(props) {
    const virkailijaRaamitUrl = '/virkailija-raamit/apply-raamit.js';
    if (process.env.NODE_ENV === 'development') {
        return <div style={{ width: '100%', height: '5rem', background: 'blue' }}>RAAMIT</div>;
    } else {
        return <VirkailijaRaamit scriptUrl={virkailijaRaamitUrl} />;
    }
}
