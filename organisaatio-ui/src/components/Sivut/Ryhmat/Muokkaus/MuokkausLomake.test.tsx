import React from 'react';
import { shallow } from 'enzyme';
import '@testing-library/jest-dom/extend-expect';

import MuokkausLomake, { InputRivi, MuokkausLomakeProps } from './MuokkausLomake';

const MINIMAL_PROPS: Partial<MuokkausLomakeProps> = {
    nimiBinds: [
        {
            localizationKey: 'SUOMEKSI',
            name: 'nimiFi',
            value: '1',
            onChange: jest.fn(),
        },
        {
            localizationKey: 'RUOTSIKSI',
            name: 'nimiSv',
            value: '2',
            onChange: jest.fn(),
        },
        {
            localizationKey: 'ENGLANNIKSI',
            name: 'nimiEn',
            value: '3',
            onChange: jest.fn(),
        },
    ],
    kuvausBinds: [
        {
            localizationKey: 'SUOMEKSI',
            name: 'kuvaus2Fi',
            value: '4',
            onChange: jest.fn(),
        },
        {
            localizationKey: 'RUOTSIKSI',
            name: 'kuvaus2Sv',
            value: '5',
            onChange: jest.fn(),
        },
        {
            localizationKey: 'ENGLANNIKSI',
            name: 'kuvaus2En',
            value: '6',
            onChange: jest.fn(),
        },
    ],
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
        const { nimiBinds = [] } = testProps;
        nimiBinds.forEach((bind) => {
            const wrapper = shallow(<InputRivi bind={bind} />);
            const field = wrapper.find({ name: bind.name });
            field.simulate('change', { target: { name: bind.name, value: 'testiNimi' } });
            expect(bind.onChange).toHaveBeenCalledTimes(1);
            expect((bind.onChange as jest.Mock).mock.calls).toMatchSnapshot();
        });
    });

    describe('Update kuvaus2 in all languages', () => {
        const { kuvausBinds = [] } = testProps;
        kuvausBinds.forEach((bind) => {
            const wrapper = shallow(<InputRivi bind={bind} />);
            const field = wrapper.find({ name: bind.name });
            field.simulate('change', { target: { name: bind.name, value: 'testiKuvaus2' } });
            expect(bind.onChange).toHaveBeenCalledTimes(1);
            expect((bind.onChange as jest.Mock).mock.calls).toMatchSnapshot();
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
