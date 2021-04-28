import React from 'react';
import { shallow } from 'enzyme';

import YhteystietoLomake, { yhteystietoProps } from './YhteystietoLomake';

const MINIMAL_PROPS: yhteystietoProps = {
    yhteystiedot: [],
    handleOnChange: jest.fn,
    postinumerot: [],
};

describe('YhteystietoLomake', () => {
    it('Renders without crashing', () => {
        const element = shallow(<YhteystietoLomake {...MINIMAL_PROPS} />);
        expect(element).toMatchSnapshot();
    });
});
