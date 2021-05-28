import React from 'react';
import { shallow } from 'enzyme';

import RyhmanMuokkaus, { RyhmanMuokausProps } from './RyhmanMuokkaus';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';
import { RouteComponentProps } from 'react-router-dom';
import { PartialDeep } from 'type-fest';

jest.spyOn(React, 'useEffect').mockImplementation((f) => f());

const MINIMAL_PROPS: PartialDeep<RouteComponentProps<RyhmanMuokausProps>> = {
    match: {
        params: {
            oid: '1234',
        },
    },
};

describe('RyhmanMuokkaus', () => {
    const wrapper = shallow(<RyhmanMuokkaus {...(MINIMAL_PROPS as RouteComponentProps<RyhmanMuokausProps>)} />);

    describe('Rendering', () => {
        it('Renders without crashing', () => {
            expect(wrapper).toMatchSnapshot();
        });
        it('Renders spinner when there is no data', () => {
            expect(wrapper.contains(<Spin />)).toBe(true);
        });
    });
});
