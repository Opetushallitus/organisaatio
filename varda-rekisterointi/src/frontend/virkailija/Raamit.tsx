import React from 'react';

const VIRKAILIJA_RAAMIT_PROD_URL = '/virkailija-raamit/apply-raamit.js';
const VIRKAILIJA_RAAMIT_DEV_URL = '/varda-rekisterointi/dev-raamit.js';
const SCRIPT_ELEMENT_ID = 'virkailija-raamit-script';

export const Raamit: React.FC = ({ children }) => {
    if (process.env.NODE_ENV === 'development' && !document.getElementById(SCRIPT_ELEMENT_ID)) {
        const scriptElement = document.createElement('script');
        scriptElement.src = VIRKAILIJA_RAAMIT_DEV_URL;
        scriptElement.id = SCRIPT_ELEMENT_ID;
        document.body.appendChild(scriptElement);
    } else if (!document.getElementById(SCRIPT_ELEMENT_ID)) {
        const scriptElement = document.createElement('script');
        scriptElement.src = VIRKAILIJA_RAAMIT_PROD_URL;
        scriptElement.id = SCRIPT_ELEMENT_ID;
        document.body.appendChild(scriptElement);
    }
    return <>{children}</>;
};
