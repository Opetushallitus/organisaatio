import React from 'react';
import { shallow } from 'enzyme';
import '@testing-library/jest-dom/extend-expect';

import Ryhmat from './Ryhmat';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';

afterAll(() => {
    jest.clearAllMocks();
});

describe('Ryhmat', () => {
    let wrapper;

    beforeEach(() => {
        jest.resetAllMocks();
        wrapper = shallow(<Ryhmat />);
    });

    describe('Rendering', () => {
        it('Renders spinner when there is no data', () => {
            expect(wrapper.contains(<Spin />)).toBe(true);
            expect(wrapper).toMatchSnapshot();
        });
    });
});
