import React from 'react';
import '@testing-library/jest-dom/extend-expect';
import { History } from 'history';

import RyhmanMuokkaus, { RyhmanMuokausProps } from './RyhmanMuokkaus';
import { RouteComponentProps, match } from 'react-router-dom';
import { render, screen } from '@testing-library/react';
import { Ryhma } from '../../../../types/types';
import axios, { AxiosResponse } from 'axios';

interface Interface {
    history: Partial<History<unknown>>;
    match: Partial<match>;
}

const MINIMAL_PROPS: Partial<Interface> = {
    match: {
        params: {
            oid: '1234',
        },
    },
    history: {
        push: jest.fn(),
    },
};

jest.mock('@opetushallitus/virkailija-ui-components/Spin', () => () => <div>Spin</div>);
jest.mock('@opetushallitus/virkailija-ui-components/Button', () => () => <button key={Math.random()}>btn</button>);
jest.mock('@opetushallitus/virkailija-ui-components/Input', () => () => <input />);
jest.mock('@opetushallitus/virkailija-ui-components/Select', () => () => <select>select</select>);
jest.mock('axios');

afterAll(() => {
    jest.clearAllMocks();
});

describe('RyhmanMuokkaus', () => {
    it('Renders Spinner when there is no ryhma', () => {
        render(<RyhmanMuokkaus {...(MINIMAL_PROPS as RouteComponentProps<RyhmanMuokausProps>)} />);
        expect(screen.getByText('Spin')).toBeInTheDocument();
    });

    it('Renders form after there is ryhma', async () => {
        const axiosResponse = {
            data: {
                nimi: {
                    fi: 'Suominimi',
                },
                ryhmatyypit: [],
                kayttoryhmat: [],
                status: 'AKTIIVINEN',
                oid: '1234',
            } as Partial<Ryhma>,
        } as Partial<AxiosResponse>;
        (axios.get as jest.Mock).mockImplementationOnce(() => Promise.resolve(axiosResponse));
        render(<RyhmanMuokkaus {...(MINIMAL_PROPS as RouteComponentProps<RyhmanMuokausProps>)} />);
        expect(await screen.findByText('Suominimi')).toBeInTheDocument();
    });
});
