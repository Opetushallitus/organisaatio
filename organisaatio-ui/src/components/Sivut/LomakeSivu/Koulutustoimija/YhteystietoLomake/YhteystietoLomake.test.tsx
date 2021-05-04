import React from 'react';
import { shallow } from 'enzyme';

import YhteystietoLomake, { Props, getYhteystieto, getOsoite } from './YhteystietoLomake';
import type { YhteystiedotOsoite } from '../../../../../types/types';

describe('YhteystietoLomake', () => {
    const kieli = 'test';

    describe('Rendering', () => {
        const MINIMAL_PROPS: Props = {
            yhteystiedot: [],
            handleOnChange: jest.fn,
        };

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
});
