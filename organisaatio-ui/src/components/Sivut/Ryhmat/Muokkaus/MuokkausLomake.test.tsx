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
    ryhma: {
        nimi: {
            fi: 'Suominimi',
        },
        ryhmatyypit: [],
        kayttoryhmat: [],
        status: 'AKTIIVINEN',
        oid: '1234',
        kayntiosoite: 'dds',
    } as any,
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
            const wrapper = shallow(<MuokkausLomake {...(testProps as MuokkausLomakeProps)} />);
            expect(wrapper).toMatchSnapshot();
        });
    });
    describe('Update nimi in all languages', () => {
        test.each([['nimiFi'], ['nimiSv'], ['nimiEn']])('%s', (name) => {
            const wrapper = shallow(<MuokkausLomake {...(testProps as MuokkausLomakeProps)} />);
            const field = wrapper.find({ name });
            field.simulate('change', { target: { name, value: 'testiNimi' } });
            expect(testProps[`${name}Bind`].onChange).toHaveBeenCalledTimes(1);
            expect((testProps[`${name}Bind`].onChange as jest.Mock).mock.calls).toMatchSnapshot();
        });
    });

    describe('Update kuvaus2 in all languages', () => {
        test.each([['kuvaus2Fi'], ['kuvaus2Sv'], ['kuvaus2En']])('%s', (name) => {
            const wrapper = shallow(<MuokkausLomake {...(testProps as MuokkausLomakeProps)} />);
            const field = wrapper.find({ name });
            field.simulate('change', { target: { name, value: 'testiKuvaus2' } });
            expect(testProps[`${name}Bind`].onChange).toHaveBeenCalledTimes(1);
            expect((testProps[`${name}Bind`].onChange as jest.Mock).mock.calls).toMatchSnapshot();
        });
    });

    describe('Buttons', () => {
        describe('Passivoi button', () => {
            it('handlePassive in invoked when passivoi is clicked', () => {
                const wrapper = shallow(<MuokkausLomake {...(testProps as MuokkausLomakeProps)} />);
                const button = wrapper.find({ name: 'passivoibutton' });
                expect(testProps.handlePassivoi).toHaveBeenCalledTimes(0);
                button.simulate('click');
                expect(testProps.handlePassivoi).toHaveBeenCalledTimes(1);
            });
        });
        describe('Poista button', () => {
            it('handlePoista in invoked when poista is clicked', () => {
                const wrapper = shallow(<MuokkausLomake {...(testProps as MuokkausLomakeProps)} />);
                const button = wrapper.find({ name: 'poistabutton' });
                expect(testProps.handlePoista).toHaveBeenCalledTimes(0);
                button.simulate('click');
                expect(testProps.handlePoista).toHaveBeenCalledTimes(1);
            });
        });

        describe('Tallenna button', () => {
            it('handleTallenna in invoked when tallenna is clicked', () => {
                const wrapper = shallow(<MuokkausLomake {...(testProps as MuokkausLomakeProps)} />);
                const button = wrapper.find({ name: 'tallennabutton' });
                expect(testProps.handleTallenna).toHaveBeenCalledTimes(0);
                button.simulate('click');
                expect(testProps.handleTallenna).toHaveBeenCalledTimes(1);
            });
        });

        describe('Peruuta button', () => {
            it('handlePeruuta in invoked when peruuta is clicked', () => {
                const wrapper = shallow(<MuokkausLomake {...(testProps as MuokkausLomakeProps)} />);
                const button = wrapper.find({ name: 'peruutabutton' });
                expect(testProps.handlePeruuta).toHaveBeenCalledTimes(0);
                button.simulate('click');
                expect(testProps.handlePeruuta).toHaveBeenCalledTimes(1);
            });
        });
    });
});
