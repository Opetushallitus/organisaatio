import React from 'react';
import devRaamit from '../../tools/devRaamit';

const virkailijaRaamitUrl = '/virkailija-raamit/apply-raamit.js';

export default function InitializeApp({ children }) {
    if (process.env.NODE_ENV === 'development') {
        devRaamit(document);
    } else if (!document.getElementById('virkailija-raamit-Script')) {
        const scriptElement = document.createElement('script');
        scriptElement.src = virkailijaRaamitUrl;
        scriptElement.id = 'virkailija-raamit-Script';
        document.body.appendChild(scriptElement);
    }
    return <>{children}</>;
}
