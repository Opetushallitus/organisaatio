import { KoodistoImpl } from './KoodistoContext';
import { Koodi, Lokalisointi } from '../types/types';
import { I18nImpl } from './LanguageContext';
import { organisationCrudAllowedInRoles } from './CasMeContext';

describe('KoodistoImpl', () => {
    const koodit: Koodi[] = [
        {
            uri: 'koodi_1#1',
            nimi: {
                fi: 'Koodi',
                sv: 'Kod',
                en: 'Code',
            },
            arvo: '1',
            versio: 1,
            tila: 'LUONNOS' as const,
        },
    ];
    const impl = new KoodistoImpl({ koodisto: koodit, kieli: 'fi' });

    it('Finds name using a uri', () => {
        const nimi = impl.uri2Nimi(koodit[0].uri);
        expect(nimi).toEqual(koodit[0].nimi.fi);
    });

    it('Finds uri using a arvo', () => {
        const uri = impl.arvo2Uri(koodit[0].arvo);
        expect(uri).toEqual(koodit[0].uri);
    });

    it('Returns empty string if uri is not found by arvo', () => {
        const uri = impl.arvo2Uri('2');
        expect(uri).toEqual('');
    });

    it('Returns empty nimi when uri doesnt exist', () => {
        const nimi = impl.uri2Nimi('eioo_1#1');
        expect(nimi).toEqual('');
    });

    it('Returns all koodis', () => {
        const koodit = impl.koodit();
        expect(koodit.length).toEqual(1);
        expect(koodit[0].uri).toEqual('koodi_1#1');
    });

    it('Returns all selectOptions', () => {
        const options = impl.selectOptions();
        expect(options.length).toEqual(1);
        expect(options[0].label).toEqual('Koodi');
    });
});
describe('KoodistoImpl no version', () => {
    const koodit: Koodi[] = [
        {
            uri: 'koodi_1',
            nimi: {
                fi: 'Koodi',
                sv: 'Kod',
                en: 'Code',
            },
            arvo: '1',
            versio: 1,
            tila: 'LUONNOS' as const,
        },
        {
            uri: 'koodi_10',
            nimi: {
                fi: 'Koodi10',
                sv: 'Kod10',
                en: 'Code10',
            },
            arvo: '10',
            versio: 1,
            tila: 'LUONNOS' as const,
        },
    ];
    const impl = new KoodistoImpl({ koodisto: koodit, kieli: 'fi' });
    it('Returns all selectOptions', () => {
        const options = impl.selectOptions();
        expect(options.length).toEqual(2);
        expect(options[0].label).toEqual('Koodi');
        expect(options[1].label).toEqual('Koodi10');
    });
});
describe('I18nImpl', () => {
    const lokalisointi = {
        fi: {
            BUTTON_HAE_YTJ_TIEDOT: 'BUTTON_HAE_YTJ_TIEDOT_FI',
            BUTTON_JATKA: 'BUTTON_JATKA_FI',
            ENRICH: '{key3} {key1} {key2}',
        },
        sv: {
            BUTTON_HAE_YTJ_TIEDOT: 'BUTTON_HAE_YTJ_TIEDOT_SV',
            BUTTON_JATKA: 'BUTTON_JATKA_SV',
        },
        en: {
            BUTTON_HAE_YTJ_TIEDOT: 'BUTTON_HAE_YTJ_TIEDOT_EN',
            BUTTON_JATKA: 'BUTTON_JATKA_EN',
        },
    };

    it('translates when key found', () => {
        const i18n = new I18nImpl(lokalisointi, 'fi');
        expect(i18n.translate('BUTTON_JATKA')).toEqual('BUTTON_JATKA_FI');
        expect(i18n.translateWithLang('BUTTON_JATKA', 'sv')).toEqual('BUTTON_JATKA_SV');
        expect(i18n.translateWithLang('BUTTON_JATKA', 'en')).toEqual('BUTTON_JATKA_EN');
    });
    it('translateNimi translates undefined to empty string', () => {
        const i18n = new I18nImpl(lokalisointi, 'fi');
        expect(i18n.translateNimi(undefined)).toEqual('');
    });

    it('defaults to key', () => {
        const i18n = new I18nImpl(lokalisointi, 'fi');
        const trans = i18n.translate('NOT_FOUND');
        expect(trans).toEqual('NOT_FOUND');
    });
    it('enriches message', () => {
        const i18n = new I18nImpl(lokalisointi, 'fi');
        const trans = i18n.enrichMessage('ENRICH', [
            { key: 'key1', value: 'val1' },
            { key: 'key2', value: 'val2' },
            { key: 'key3', value: 'val3' },
        ]);
        expect(trans).toEqual('val3 val1 val2');
    });

    it('defaults to key when lokalisointi undefined', () => {
        const i18n = new I18nImpl({} as Lokalisointi, 'fi');
        const trans = i18n.translate('NOT_FOUND');
        expect(trans).toEqual('NOT_FOUND');
    });
});

describe('CASMeImpl', () => {
    it('cheks roles and organisation for button when organisations present', () => {
        const roles = ['APP_ORGANISAATIOHALLINTA_CRUD_111', 'APP_ORGANISAATIOHALLINTA_CRUD_666'];
        expect(organisationCrudAllowedInRoles('333', [{ oid: '111', nimi: { fi: '111' } }], roles)).toBeTruthy();
        expect(organisationCrudAllowedInRoles('333', [{ oid: '666', nimi: { fi: '666' } }], roles)).toBeTruthy();
        expect(organisationCrudAllowedInRoles('666', [{ oid: '999', nimi: { fi: '999' } }], roles)).toBeTruthy();
        expect(organisationCrudAllowedInRoles('333', [{ oid: '999', nimi: { fi: '999' } }], roles)).toBeFalsy();
    });
    it('cheks roles and organisation for button when no organisation', () => {
        const roles = ['FOO', 'APP_ORGANISAATIOHALLINTA_CRUD'];
        expect(organisationCrudAllowedInRoles('111', [{ oid: '999', nimi: { fi: '999' } }], roles)).toBeFalsy();
    });
});
