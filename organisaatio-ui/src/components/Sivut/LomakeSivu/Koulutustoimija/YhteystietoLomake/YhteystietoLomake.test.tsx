import React from 'react';
import { shallow } from 'enzyme';

import YhteystietoLomake, { Props, getYhteystieto, getOsoite } from './YhteystietoLomake';
import type { YhteystiedotOsoite } from '../../../../../types/types';

const kieli = 'kieli_fi#1';
const MINIMAL_PROPS: Props = {
    yhteystiedot: [],
    handleOnChange: jest.fn(),
};

afterAll(() => {
    jest.clearAllMocks();
});

beforeEach(() => {
    MINIMAL_PROPS.yhteystiedot = [];
    jest.resetAllMocks();
});

describe('YhteystietoLomake', () => {
    describe('Rendering', () => {
        it('Renders without crashing', () => {
            const element = shallow(<YhteystietoLomake {...MINIMAL_PROPS} />);
            expect(element).toMatchSnapshot();
        });
    });

    describe('getYhteystieto', () => {
        const yhteystieto = { kieli, www: 'foo' };

        it('Finds correct element', () => {
            expect(getYhteystieto([yhteystieto], kieli, 'www')).toEqual(yhteystieto);
        });

        it('Creates correct element on demand', () => {
            expect(getYhteystieto([], kieli, 'www')).toEqual({ ...yhteystieto, www: '' });
        });
    });

    describe('getOsoite', () => {
        const osoiteTyyppi = 'posti';

        const yhteystieto = {
            kieli,
            osoiteTyyppi,
            osoite: 'testi',
        } as YhteystiedotOsoite;

        it('Finds correct element', () => {
            expect(getOsoite([yhteystieto], kieli, osoiteTyyppi)).toEqual(yhteystieto);
        });

        it('Creates correct element on demand', () => {
            expect(getOsoite([], kieli, osoiteTyyppi)).toEqual({
                ...yhteystieto,
                osoite: '',
                postinumeroUri: '',
                postitoimipaikka: '',
            });
        });
    });

    describe('Change lang', () => {
        it('creates needed yhteystietos', () => {
            const lomake = shallow(<YhteystietoLomake {...MINIMAL_PROPS} />);
            const widget = lomake.find({ value: 'kieli_fi#1' });
            widget.simulate('change', { target: { value: 'test' } });
            expect(MINIMAL_PROPS.yhteystiedot).toMatchSnapshot();
        });
    });

    describe('Checkbox', () => {
        const MINIMAL_PROPS: Props = {
            yhteystiedot: [
                {
                    kieli,
                    osoiteTyyppi: 'posti',
                    osoite: 'testi',
                } as YhteystiedotOsoite,
            ],
            handleOnChange: jest.fn(),
        };

        it('Copies posti to kaynti when checked', () => {
            const lomake = shallow(<YhteystietoLomake {...MINIMAL_PROPS} />);
            const checkbox = () => lomake.find('#checkbox');
            expect(checkbox().props().checked).toEqual(false);
            checkbox().simulate('change', { target: { checked: true } });
            expect(checkbox().props().checked).toEqual(true);
            expect(MINIMAL_PROPS.handleOnChange).toHaveBeenCalledTimes(1);
            expect((MINIMAL_PROPS.handleOnChange as jest.Mock).mock.calls).toMatchSnapshot();
        });
    });

    describe('Update field', () => {
        test.each([['www'], ['email'], ['numero']])('%s', (osoiteTyyppi) => {
            const lomake = shallow(<YhteystietoLomake {...MINIMAL_PROPS} />);
            const field = lomake.find({ name: osoiteTyyppi });
            field.simulate('change', { target: { name: osoiteTyyppi, value: 'test' } });
            expect(MINIMAL_PROPS.handleOnChange).toHaveBeenCalledTimes(1);
            expect((MINIMAL_PROPS.handleOnChange as jest.Mock).mock.calls).toMatchSnapshot();
        });
    });

    describe('Update address', () => {
        test.each([['posti'], ['kaynti']])('%s', (osoiteTyyppi) => {
            const name = `${osoiteTyyppi}.osoite`;
            const lomake = shallow(<YhteystietoLomake {...MINIMAL_PROPS} />);
            const field = lomake.find({ name });
            field.simulate('change', { target: { name, value: 'test' } });
            expect(MINIMAL_PROPS.handleOnChange).toHaveBeenCalledTimes(1);
            expect((MINIMAL_PROPS.handleOnChange as jest.Mock).mock.calls).toMatchSnapshot();
        });
    });
});
