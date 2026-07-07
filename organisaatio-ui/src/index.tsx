import * as React from 'react';
import { createRoot } from 'react-dom/client';
import './index.css';
import axios from 'axios';
import Cookies from 'universal-cookie';
import { ROOT_OID } from './contexts/constants';
import InitializeApp from './components/InitializeApp/InitializeApp';
import OrganisaatioApp from './OrganisaatioApp';
import { Provider } from 'jotai';
import Loading from './components/Loading/Loading';
import ErrorPage from './components/Sivut/VirheSivu/VirheSivu';
import { createJotaiStore } from './jotaiStore';

const cookies = new Cookies();
axios.interceptors.request.use((config) => {
    if (config.headers) {
        config.headers['Caller-Id'] = `${ROOT_OID}.organisaatio-ui`;
        config.headers['CSRF'] = cookies.get('CSRF');
    }
    return config;
});

type ErrorBoundaryProps = React.PropsWithChildren;

export class ErrorBoundary extends React.Component<ErrorBoundaryProps, { hasError: boolean }> {
    constructor(props: ErrorBoundaryProps) {
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
    const rootElement = document.getElementById('root');
    if (!rootElement) {
        throw new Error('Root element not found');
    }

    createRoot(rootElement).render(
        <React.StrictMode>
            <Provider unstable_createStore={createJotaiStore}>
                <ErrorBoundary>
                    <React.Suspense fallback={<Loading />}>
                        <InitializeApp>
                            <OrganisaatioApp />
                        </InitializeApp>
                    </React.Suspense>
                </ErrorBoundary>
            </Provider>
        </React.StrictMode>
    );
}

main();
