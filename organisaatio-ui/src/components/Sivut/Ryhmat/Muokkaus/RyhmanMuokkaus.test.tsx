import React from 'react';
import { shallow } from 'enzyme';
import '@testing-library/jest-dom/extend-expect';

import RyhmanMuokkaus, { RyhmanMuokausProps } from './RyhmanMuokkaus';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';
import { RouteComponentProps } from 'react-router-dom';

const MINIMAL_PROPS: RouteComponentProps<RyhmanMuokausProps> = {
    match: {
        params: {
            oid: '1234',
        },
        isExact: true,
        path: '/',
        url: '',
    },
    history: {
        push: jest.fn(),
        length: 1,
        action: 'POP',
        location: {
            pathname: '',
            search: '',
            state: '',
            hash: '',
        },
        replace: jest.fn(),
        go: jest.fn(),
        goBack: jest.fn(),
        goForward: jest.fn(),
        block: jest.fn(),
        listen: jest.fn(),
        createHref: jest.fn(),
    },
    location: {
        pathname: '',
        search: '',
        state: '',
        hash: '',
    },
};

afterAll(() => {
    jest.clearAllMocks();
});

describe('RyhmanMuokkaus', () => {
    let wrapper;

    beforeEach(() => {
        wrapper = shallow(<RyhmanMuokkaus {...MINIMAL_PROPS} />);
        jest.resetAllMocks();
    });

    describe('Rendering', () => {
        it('Renders without crashing', () => {
            expect(wrapper).toMatchSnapshot();
        });
        it('Renders spinner when there is no data', () => {
            expect(wrapper.contains(<Spin />)).toBe(true);
        });
    });
});
