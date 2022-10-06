import React from 'react';
import '@testing-library/jest-dom/extend-expect';
import { render, screen } from '@testing-library/react';

import Ryhmat from './Ryhmat';
import axios, { AxiosResponse } from 'axios';
import { Ryhma } from '../../../types/types';
import { BrowserRouter } from 'react-router-dom';
import Loading from '../../Loading/Loading';

jest.mock('@opetushallitus/virkailija-ui-components/Spin', () => () => <div>Spin</div>);
jest.mock('@opetushallitus/virkailija-ui-components/Button', () => () => <button key={Math.random()}>btn</button>);
jest.mock('@opetushallitus/virkailija-ui-components/Input', () => () => <input />);
jest.mock('@opetushallitus/virkailija-ui-components/Select', () => () => <select>select</select>);
jest.mock('axios');

const axiosResponse = {
    data: [
        {
            nimi: {
                fi: 'fi',
            },
            ryhmatyypit: [],
            kayttoryhmat: [],
            status: 'AKTIIVINEN',
            oid: '1234',
        },
    ] as Partial<Ryhma>[],
} as Partial<AxiosResponse>;
beforeEach(() => {
    jest.clearAllMocks();
    jest.spyOn(console, 'warn').mockImplementation(() => {});
    jest.spyOn(console, 'error').mockImplementation(() => {});
    jest.spyOn(console, 'info').mockImplementation(() => {});
    (axios.get as jest.Mock).mockImplementation(async (a) => {
        if (a.startsWith) {
            if (a.startsWith('/organisaatio-service/api/ryhmat')) return Promise.resolve(axiosResponse);
            if (a.startsWith('/organisaatio-service/internal/lokalisointi')) return { data: [] };
            if (a.startsWith('/organisaatio-service/internal/config/frontproperties')) return { data: {} };
            if (a.includes('/kayttooikeus-service/cas/me')) return { data: {} };
            if (a.startsWith('/organisaatio-service/internal/koodisto/RYHMATYYPIT/koodi')) return { data: [] };
            if (a.startsWith('/organisaatio-service/internal/koodisto/KAYTTORYHMAT/koodi')) return { data: [] };
            if (a.startsWith('/organisaatio-service/internal/koodisto/RYHMANTILA/koodi')) return { data: [] };
        }
        return { data: {} };
    });
});

afterAll(() => {
    jest.clearAllMocks();
});

describe('Ryhmat', () => {
    describe('Rendering', () => {
        it('Renders Spinner when there is no data', () => {
            render(
                <React.Suspense fallback={<Loading />}>
                    <BrowserRouter basename={'/organisaatio'}>
                        <Ryhmat />
                    </BrowserRouter>
                </React.Suspense>
            );
            expect(screen.getByText('Spin')).toBeInTheDocument();
        });

        it('Renders normaalitaulukko after data is fetched', async () => {
            render(
                <React.Suspense fallback={<Loading />}>
                    <BrowserRouter basename={'/organisaatio'}>
                        <Ryhmat />
                    </BrowserRouter>
                </React.Suspense>
            );
            expect(await screen.findByRole('table')).toBeInTheDocument();
        });
    });
});
