import React from 'react';
import '@testing-library/jest-dom/extend-expect';
import { History } from 'history';

import RyhmanMuokkaus, { RyhmanMuokausProps } from './RyhmanMuokkaus';
import { RouteComponentProps, match, BrowserRouter } from 'react-router-dom';
import { render, screen } from '@testing-library/react';
import { Ryhma } from '../../../../types/types';
import axios, { AxiosResponse } from 'axios';

type DeepPartial<T> = {
    [P in keyof T]?: DeepPartial<T[P]>;
};

interface Interface {
    history: Partial<History<unknown>>;
    match: Partial<match>;
}

const MINIMAL_PROPS: DeepPartial<Interface> = {
    match: {
        params: {
            oid: '1234',
        },
    },
    history: {
        push: jest.fn(),
        location: {
            pathname: '',
        },
    },
};

const testiRyhma = {
    nimi: {
        fi: 'Suominimi',
    },
    kuvaus2: {
        'kieli_fi#1': 'testikuvaus',
    },
    ryhmatyypit: [],
    kayttoryhmat: [],
    status: 'AKTIIVINEN',
    oid: '1234',
} as Partial<Ryhma>;

jest.mock('@opetushallitus/virkailija-ui-components/Spin', () => () => <div>Spin</div>);
jest.mock('@opetushallitus/virkailija-ui-components/Button', () => () => <button key={Math.random()}>btn</button>);
jest.mock('@opetushallitus/virkailija-ui-components/Input', () => () => <input />);
jest.mock('@opetushallitus/virkailija-ui-components/Select', () => () => <select>select</select>);
jest.mock('axios');

afterAll(() => {
    jest.clearAllMocks();
});

describe('RyhmanMuokkaus', () => {
    const axiosResponse = {
        data: testiRyhma as Partial<Ryhma>,
    } as Partial<AxiosResponse>;
    beforeEach(() => {
        (axios.get as jest.Mock).mockImplementationOnce(() => Promise.resolve(axiosResponse));
    });
    it('Renders Spinner when there is no ryhma and is not new', () => {
        render(
            <BrowserRouter basename="/organisaatio">
                <RyhmanMuokkaus {...(MINIMAL_PROPS as RouteComponentProps<RyhmanMuokausProps>)} />
            </BrowserRouter>
        );
        expect(screen.getByText('Spin')).toBeInTheDocument();
    });

    it('Renders form after there is ryhma when is not new', async () => {
        render(
            <BrowserRouter basename="/organisaatio">
                <RyhmanMuokkaus {...(MINIMAL_PROPS as RouteComponentProps<RyhmanMuokausProps>)} />
            </BrowserRouter>
        );
        expect(await screen.findByText('Suominimi')).toBeInTheDocument();
    });

    it('Renders new ryhma form using empty ryhma when isNew prop is added', async () => {
        render(
            <BrowserRouter basename="/organisaatio">
                <RyhmanMuokkaus isNew {...(MINIMAL_PROPS as RouteComponentProps<RyhmanMuokausProps>)} />
            </BrowserRouter>
        );
        const alinRivi = screen.queryAllByRole('generic').find((div) => div.className === 'AlinRivi');
        const alaBanneri = screen.queryAllByRole('generic').find((div) => div.className === 'AlaBanneri');
        expect(alaBanneri).toBeInTheDocument();
        expect(alinRivi).toBeUndefined();
    });
    it('Renders new ryhma form using empty ryhma when uusi path is called', async () => {
        const UUSIPROPS: DeepPartial<Interface> = {
            match: { params: {} },
            history: { location: { pathname: '/uusi' }, push: jest.fn() },
        };
        render(
            <BrowserRouter basename="/organisaatio">
                <RyhmanMuokkaus {...(UUSIPROPS as RouteComponentProps<RyhmanMuokausProps>)} />
            </BrowserRouter>
        );
        const alinRivi = screen.queryAllByRole('generic').find((div) => div.className === 'AlinRivi');
        const alaBanneri = screen.queryAllByRole('generic').find((div) => div.className === 'AlaBanneri');
        expect(alaBanneri).toBeInTheDocument();
        expect(alinRivi).toBeUndefined();
    });
});
