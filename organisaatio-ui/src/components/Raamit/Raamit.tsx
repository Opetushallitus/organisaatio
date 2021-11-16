import React from 'react';
import VirkailijaRaamit from '@opetushallitus/virkailija-ui-components/VirkailijaRaamit';

export default function Raamit(props) {
    const virkailijaRaamitUrl = '/virkailija-raamit/apply-raamit.js';
    if (process.env.NODE_ENV === 'development') {
        return <>RAAMIT</>;
    } else {
        return <VirkailijaRaamit scriptUrl={virkailijaRaamitUrl} />;
    }
}
