import React from 'react';
import { shallow } from 'enzyme';
import '@testing-library/jest-dom/extend-expect';
import NormaaliTaulukko, { NormaaliTaulukkoProps, FiltteritProps, Hakufiltterit } from './NormaaliTaulukko';

const MINIMAL_PROPS: NormaaliTaulukkoProps = {
    ryhmatData: [],
    yhteystietoTyypitData: [],
    ryhmatColumns: [],
    yhteystietotyypitColumns: [],
    useHakuFiltteri: false,
};

const MINIMAL_FILTTERIT_PROPS: FiltteritProps = {
    setFilter: jest.fn(),
    setGlobalFilter: jest.fn(),
    globalFilter: '',
};

beforeEach(() => {
    jest.resetAllMocks();
});

afterAll(() => {
    jest.clearAllMocks();
});

describe('Normaalitaulukko', () => {
    describe('Rendering', () => {
        it('Renders without crashing', () => {
            const element = shallow(<NormaaliTaulukko {...MINIMAL_PROPS} />);
            expect(element).toMatchSnapshot();
        });
    });

    describe('Hakufiltterit', () => {
        it('Renders without crashing', () => {
            const element = shallow(<Hakufiltterit {...MINIMAL_FILTTERIT_PROPS} />);
            expect(element).toMatchSnapshot();
        });
        it('Is not rendered when useHakuFiltteri is set false', () => {
            const element = shallow(<NormaaliTaulukko {...{ ...MINIMAL_PROPS }} />);
            expect(element.find(Hakufiltterit)).toHaveLength(0);
        });
        it('Is rendered when useHakuFiltteri is set true', () => {
            const element = shallow(<NormaaliTaulukko {...{ ...MINIMAL_PROPS, useHakuFiltteri: true }} />);
            expect(element.find(Hakufiltterit)).toHaveLength(1);
        });
    });
});
