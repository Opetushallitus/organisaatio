import React from 'react';
import { frontPropertiesAtom } from '../../api/config';
import { useAtom } from 'jotai';

const virkailijaRaamitUrl = '/virkailija-raamit/apply-raamit.js';

export default function InitializeApp({ children }) {
    useAtom(frontPropertiesAtom);
    if (process.env.NODE_ENV === 'development' && !document.getElementById('virkailija-raamit-Script')) {
        const scriptElement = document.createElement('script');
        scriptElement.src = '/organisaatio-service/dev-raamit.js';
        scriptElement.id = 'virkailija-raamit-Script';
        document.body.appendChild(scriptElement);
    } else if (!document.getElementById('virkailija-raamit-Script')) {
        const scriptElement = document.createElement('script');
        scriptElement.src = virkailijaRaamitUrl;
        scriptElement.id = 'virkailija-raamit-Script';
        document.body.appendChild(scriptElement);
    }
    return <>{children}</>;
}
