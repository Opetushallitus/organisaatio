import React from 'react';
import '@testing-library/jest-dom/extend-expect';
import { render, screen } from '@testing-library/react';

import Ryhmat from './Ryhmat';
import axios, { AxiosResponse } from 'axios';
import { Ryhma } from '../../../types/types';

jest.mock('@opetushallitus/virkailija-ui-components/Spin', () => () => <div>Spin</div>);
jest.mock('@opetushallitus/virkailija-ui-components/Button', () => () => <button key={Math.random()}>btn</button>);
jest.mock('@opetushallitus/virkailija-ui-components/Input', () => () => <input />);
jest.mock('@opetushallitus/virkailija-ui-components/Select', () => () => <select>select</select>);
jest.mock('axios');

afterAll(() => {
    jest.clearAllMocks();
});

describe('Ryhmat', () => {
    describe('Rendering', () => {
        it('Renders Spinner when there is no data', () => {
            render(<Ryhmat />);
            expect(screen.getByText('Spin')).toBeInTheDocument();
        });

        it('Renders normaalitaulukko after data is fetched', async () => {
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
            (axios.get as jest.Mock).mockImplementationOnce(() => Promise.resolve(axiosResponse));

            render(<Ryhmat />);
            expect(await screen.findByRole('table')).toBeInTheDocument();
        });
    });
});
