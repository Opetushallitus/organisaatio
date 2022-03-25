import React from 'react';
import { shallow } from 'enzyme';
import '@testing-library/jest-dom/extend-expect';

import MuokkausLomake, { MuokkausLomakeProps } from './MuokkausLomake';

import RyhmatLomakeSchema from '../../../../ValidationSchemas/RyhmatLomakeSchema';
import { useAtom } from 'jotai';
import axios from 'axios';

const MINIMAL_PROPS: Partial<MuokkausLomakeProps> = {
    handlePeruuta: jest.fn(),
    handlePassivoi: jest.fn(),
    handlePoista: jest.fn(),
    handleTallenna: jest.fn(),
    ryhma: {
        nimi: {
            fi: 'Suominimi',
        },
        ryhmatyypit: ['hakukohde#1'],
        kayttoryhmat: ['testikayttoryhma#1'],
        status: 'AKTIIVINEN',
        oid: '1234',
        kayntiosoite: { osoite: 'dds' },
        kuvaus2: {
            'kieli_fi#1': 'testikuvaus',
        },
        nimet: [],
        tyypit: [],
    },
};
jest.mock('jotai');
jest.mock('axios');
let testProps = { ...MINIMAL_PROPS };

afterAll(() => {
    jest.clearAllMocks();
});

beforeEach(() => {
    testProps = { ...MINIMAL_PROPS };
    jest.resetAllMocks();
    (useAtom as jest.Mock).mockReturnValue([
        {
            translate: (a) => a,
            translateNimi: (a) => a,
            selectOptions: () => {},
            uri2SelectOption: () => ({
                arvo: '',
                disabled: false,
                label: '',
                value: '',
                versio: 0,
            }),
        },
    ]);
    (axios.get as jest.Mock).mockImplementation(async (a) => {
        if (a.startsWith) {
            if (a.startsWith('/organisaatio-service/internal/koodisto/')) return { data: [] };
        }
        return { data: {} };
    });
});

describe('MuokkausLomake', () => {
    describe('Rendering', () => {
        it('Renders without crashing', () => {
            const wrapper = shallow(<MuokkausLomake {...(testProps as MuokkausLomakeProps)} />);
            expect(wrapper).toMatchSnapshot();
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
    describe('Schema validation', () => {
        it('will NOT pass pass if no nimi is given', () => {
            const nonameData = {
                kuvaus2Fi: 'testikuvaus',
                ryhmatyypit: ['testi'],
                kayttoryhmat: ['testi'],
            };
            expect(RyhmatLomakeSchema.validate(nonameData)).toHaveProperty('error');
        });

        it('will NOT pass pass if empty kuvaus is given', () => {
            const nokuvausData = {
                nimiFi: 'nimi',
                kuvaus2Fi: '',
                ryhmatyypit: ['testi'],
                kayttoryhmat: ['testi'],
            };
            expect(RyhmatLomakeSchema.validate(nokuvausData)).toHaveProperty('error');
        });

        it('will NOT pass pass if no ryhmatyypit is given', () => {
            const noryhmatyypitData = {
                nimiFi: 'nimi',
                kuvaus2Fi: 'kuvaus',
                ryhmatyypit: [],
                kayttoryhmat: ['testi'],
            };
            expect(RyhmatLomakeSchema.validate(noryhmatyypitData)).toHaveProperty('error');
        });

        it('will NOT pass pass if no kayttoryhmat is given', () => {
            const noryhmatyypitData = {
                nimiFi: 'nimi',
                kuvaus2Fi: 'kuvaus',
                ryhmatyypit: ['testi'],
                kayttoryhmat: [],
            };
            expect(RyhmatLomakeSchema.validate(noryhmatyypitData)).toHaveProperty('error');
        });

        it('will pass if one nimi and one kuvaus is filled and ryhmatyypit and kayttoryhmat are not empty', () => {
            const validData = {
                nimiFi: 'suominimi',
                kuvaus2Fi: 'testikuvaus',
                ryhmatyypit: ['testi'],
                kayttoryhmat: ['testi'],
            };
            expect(RyhmatLomakeSchema.validate(validData).error).toBeUndefined();
        });
    });
});
