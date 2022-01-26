import React from 'react';
import { shallow } from 'enzyme';
import '@testing-library/jest-dom/extend-expect';
import NormaaliTaulukko, {
    chooseTaulukkoData,
    FiltteritProps,
    Hakufiltterit,
    NormaaliTaulukkoProps,
} from './NormaaliTaulukko';
import { Ryhma } from '../../types/types';
import { Column } from 'react-table';
import { useAtom } from 'jotai';

const MINIMAL_PROPS: NormaaliTaulukkoProps = {
    ryhmatData: [],
    ryhmatColumns: [],
    useHakuFiltteri: false,
};

const MINIMAL_FILTTERIT_PROPS: FiltteritProps = {
    setFilter: jest.fn(),
    setGlobalFilter: jest.fn(),
    globalFilter: '',
};

jest.mock('jotai');
beforeEach(() => {
    jest.resetAllMocks();
    jest.spyOn(console, 'error').mockImplementation(() => {});
    (useAtom as jest.Mock).mockReturnValue([{ translate: (a) => a, translateNimi: (a) => a, koodit: () => [] }]);
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
    describe('chooseTaulukkoData', () => {
        const ryhma = {
            kayntiosoite: undefined,
            kayttoryhmat: [],
            kieletUris: [],
            kuvaus: '',
            kuvaus2: undefined,
            lakkautusPvm: '',
            lisatiedot: [],
            muutKotipaikatUris: [],
            muutOppilaitosTyyppiUris: [],
            nimet: [],
            nimi: undefined,
            oid: '',
            parentOid: '',
            parentOidPath: '',
            piilotettu: false,
            postiosoite: undefined,
            ryhmatyypit: [],
            status: '',
            toimipistekoodi: '',
            tyypit: [],
            version: 0,
            vuosiluokat: [],
            yhteystiedot: [],
            yhteystietoArvos: [],
            yritysmuoto: '',
        };

        const ryhmatColumn = {
            Header: 'nimi',
        } as Column<Ryhma>;

        test.each([
            [[ryhma], [ryhmatColumn], [], [], { data: [ryhma], columns: [ryhmatColumn] }],

            [[], [], [], [], { data: [], columns: [] }],
        ])(
            'Returns ryhmatData and ryhmatColumns or yhteystietotyypitData and yhteystietotyypitColumns based on which one has data',
            (ryhmatData, ryhmatColumns, yhteystietotyypitData, yhteystietotyypitColumns, expectedResult) => {
                const result = chooseTaulukkoData(ryhmatData, ryhmatColumns);
                expect(result).toEqual(expectedResult);
            }
        );
    });
});
