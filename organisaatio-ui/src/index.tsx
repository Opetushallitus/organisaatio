import * as React from 'react';
import * as ReactDOM from 'react-dom';
import './index.css';
import axios from 'axios';
import Cookies from 'universal-cookie';
import { ROOT_OID } from './contexts/constants';
import InitializeApp from './components/InitializeApp/InitializeApp';
import OrganisaatioApp from './OrganisaatioApp';
import { Provider } from 'jotai';
import Loading from './components/Loading/Loading';
import ErrorPage from './components/Sivut/VirheSivu/VirheSivu';

const cookies = new Cookies();
axios.interceptors.request.use((config) => {
    if (config.headers) {
        config.headers['Caller-Id'] = `${ROOT_OID}.organisaatio-ui`;
        config.headers['CSRF'] = cookies.get('CSRF');
    }
    return config;
});

export class ErrorBoundary extends React.Component<unknown, { hasError: boolean }> {
    constructor(props: unknown) {
        super(props);
        this.state = { hasError: false };
    }
    static getDerivedStateFromError() {
        return { hasError: true };
    }
    componentDidCatch(error: unknown, errorInfo: unknown) {
        console.error(error, errorInfo);
    }
    render() {
        const { hasError } = this.state;
        if (hasError) {
            return <ErrorPage>Service Unavailable</ErrorPage>;
        }
        return this.props.children;
    }
}

async function main() {
    ReactDOM.render(
        <React.StrictMode>
            <Provider>
                <ErrorBoundary>
                    <React.Suspense fallback={<Loading />}>
                        <InitializeApp>
                            <OrganisaatioApp />
                        </InitializeApp>
                    </React.Suspense>
                </ErrorBoundary>
            </Provider>
        </React.StrictMode>,
        document.getElementById('root')
    );
}

main();
