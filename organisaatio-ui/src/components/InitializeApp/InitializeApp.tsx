import * as React from 'react';
import useFrontProperties from '../../api/config';
import Loading from '../Loading/Loading';
import ErrorPage from '../Sivut/VirheSivu/VirheSivu';
import devRaamit from '../../tools/devRaamit';

const virkailijaRaamitUrl = '/virkailija-raamit/apply-raamit.js';

export default function InitializeApp({ children }) {
    const { loading: frontPropertiesLoading, error: frontPropertiesError } = useFrontProperties();
    if (frontPropertiesLoading) {
        return <Loading />;
    } else if (frontPropertiesError) {
        return <ErrorPage>Service Unavailable</ErrorPage>;
    } else {
        if (process.env.NODE_ENV === 'development') {
            devRaamit(document);
        } else if (!document.getElementById('virkailija-raamit-Script')) {
            const scriptElement = document.createElement('script');
            scriptElement.src = virkailijaRaamitUrl;
            scriptElement.id = 'virkailija-raamit-Script';
            document.body.appendChild(scriptElement);
        }
        return children;
    }
}
