import React from 'react';
import { shallow } from 'enzyme';

import YhteystietoLomake, { Props } from './YhteystietoLomake';

const MINIMAL_PROPS: Props = {
    yhteystiedot: [],
    handleOnChange: jest.fn,
};

describe('YhteystietoLomake', () => {
    it('Renders without crashing', () => {
        const element = shallow(<YhteystietoLomake {...MINIMAL_PROPS} />);
        expect(element).toMatchSnapshot();
    });
});
