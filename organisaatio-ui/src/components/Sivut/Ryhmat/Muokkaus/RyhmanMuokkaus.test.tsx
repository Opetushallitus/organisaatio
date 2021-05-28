import React from 'react';
import { shallow } from 'enzyme';
import '@testing-library/jest-dom/extend-expect';
import { History } from 'history';

import RyhmanMuokkaus, { RyhmanMuokausProps } from './RyhmanMuokkaus';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';
import { RouteComponentProps, match } from 'react-router-dom';

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

afterAll(() => {
    jest.clearAllMocks();
});

describe('RyhmanMuokkaus', () => {
    let wrapper;

    beforeEach(() => {
        wrapper = shallow(<RyhmanMuokkaus {...MINIMAL_PROPS as RouteComponentProps<RyhmanMuokausProps>} />);
        jest.resetAllMocks();
    });

    describe('Rendering', () => {
        it('Renders spinner when there is no data', () => {
            expect(wrapper.contains(<Spin />)).toBe(true);
            expect(wrapper).toMatchSnapshot();
        });
    });
});
