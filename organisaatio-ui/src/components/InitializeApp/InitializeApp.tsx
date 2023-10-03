import React, { ReactNode } from 'react';
import { useAtom } from 'jotai';
import { frontPropertiesAtom } from '../../api/config';
import { casMeAtom } from '../../api/kayttooikeus';

const virkailijaRaamitUrl = `/virkailija-raamit/apply-raamit.js?t=${Date.now()}`;

export default function InitializeApp({ children }: { children: ReactNode }) {
    useAtom(frontPropertiesAtom);
    useAtom(casMeAtom);
    if (process.env.NODE_ENV === 'development' && !document.getElementById('virkailija-raamit-Script')) {
        const scriptElement = document.createElement('script');
        scriptElement.src = `/organisaatio-service/dev-raamit.js?t=${Date.now()}`;
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
