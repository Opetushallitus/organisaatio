import React from 'react';
import { shallow } from 'enzyme';
import '@testing-library/jest-dom/extend-expect';

import MuokkausLomake, { MuokkausLomakeProps } from './MuokkausLomake';

const MINIMAL_PROPS: Partial<MuokkausLomakeProps> = {
    nimiFiBind: {
        name: 'nimiFi',
        value: '1',
        onChange: jest.fn(),
    },
    nimiSvBind: {
        name: 'nimiSv',
        value: '2',
        onChange: jest.fn(),
    },
    nimiEnBind: {
        name: 'nimiEn',
        value: '3',
        onChange: jest.fn(),
    },
    kuvaus2FiBind: {
        name: 'kuvaus2Fi',
        value: '4',
        onChange: jest.fn(),
    },
    kuvaus2SvBind: {
        name: 'kuvaus2Sv',
        value: '5',
        onChange: jest.fn(),
    },
    kuvaus2EnBind: {
        name: 'kuvaus2En',
        value: '6',
        onChange: jest.fn(),
    },
    handleRyhmaSelectOnChange: jest.fn(),
    handlePeruuta: jest.fn(),
    handlePassivoi: jest.fn(),
    handlePoista: jest.fn(),
    handleTallenna: jest.fn(),
};

let testProps = { ...MINIMAL_PROPS };

afterAll(() => {
    jest.clearAllMocks();
});

beforeEach(() => {
    testProps = { ...MINIMAL_PROPS };
    jest.resetAllMocks();
});

describe('MuokkausLomake', () => {
    describe('Rendering', () => {
        it('Renders without crashing', () => {
            const wrapper = shallow(<MuokkausLomake {...testProps as MuokkausLomakeProps} />);
            expect(wrapper).toMatchSnapshot();
        });
    });
    describe('Update nimi in all languages', () => {
        test.each([['nimiFi'], ['nimiSv'], ['nimiEn']])('%s', (name) => {
            const wrapper = shallow(<MuokkausLomake {...testProps as MuokkausLomakeProps} />);
            const field = wrapper.find({ name });
            field.simulate('change', { target: { name, value: 'testiNimi' } });
            expect(testProps[`${name}Bind`].onChange).toHaveBeenCalledTimes(1);
            expect((testProps[`${name}Bind`].onChange as jest.Mock).mock.calls).toMatchSnapshot();
        });
    });

    describe('Update kuvaus2 in all languages', () => {
        test.each([['kuvaus2Fi'], ['kuvaus2Sv'], ['kuvaus2En']])('%s', (name) => {
            const wrapper = shallow(<MuokkausLomake {...testProps as MuokkausLomakeProps} />);
            const field = wrapper.find({ name });
            field.simulate('change', { target: { name, value: 'testiKuvaus2' } });
            expect(testProps[`${name}Bind`].onChange).toHaveBeenCalledTimes(1);
            expect((testProps[`${name}Bind`].onChange as jest.Mock).mock.calls).toMatchSnapshot();
        });
    });

    describe('Buttons', () => {
        describe('Passivoi button', () => {
            it('clicks', () => {
                const wrapper = shallow(<MuokkausLomake {...testProps as MuokkausLomakeProps} />);
                const button = wrapper.find({ name: 'passivoibutton' });
                button.simulate('click');
                expect(testProps.handlePassivoi).toHaveBeenCalledTimes(1);
                expect((testProps.handlePassivoi as jest.Mock).mock.calls).toMatchSnapshot();
            });
        });
        describe('Poista button', () => {
            it('clicks', () => {
                const wrapper = shallow(<MuokkausLomake {...testProps as MuokkausLomakeProps} />);
                const button = wrapper.find({ name: 'poistabutton' });
                button.simulate('click');
                expect(testProps.handlePoista).toHaveBeenCalledTimes(1);
                expect((testProps.handlePoista as jest.Mock).mock.calls).toMatchSnapshot();
            });
        });

        describe('Tallenna button', () => {
            it('clicks', () => {
                const wrapper = shallow(<MuokkausLomake {...testProps as MuokkausLomakeProps} />);
                const button = wrapper.find({ name: 'tallennabutton' });
                button.simulate('click');
                expect(testProps.handleTallenna).toHaveBeenCalledTimes(1);
                expect((testProps.handleTallenna as jest.Mock).mock.calls).toMatchSnapshot();
            });
        });

        describe('Peruuta button', () => {
            it('clicks', () => {
                const wrapper = shallow(<MuokkausLomake {...testProps as MuokkausLomakeProps} />);
                const button = wrapper.find({ name: 'peruutabutton' });
                button.simulate('click');
                expect(testProps.handlePeruuta).toHaveBeenCalledTimes(1);
                expect((testProps.handlePeruuta as jest.Mock).mock.calls).toMatchSnapshot();
            });
        });
    });
});
