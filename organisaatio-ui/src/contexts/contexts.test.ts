import assert from 'assert/strict';
import { describe, it } from 'node:test';

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
        assert.strictEqual(nimi, koodit[0].nimi.fi);
    });

    it('Finds uri using a arvo', () => {
        const uri = impl.arvo2Uri(koodit[0].arvo);
        assert.strictEqual(uri, koodit[0].uri);
    });

    it('Returns empty string if uri is not found by arvo', () => {
        const uri = impl.arvo2Uri('2');
        assert.strictEqual(uri, '');
    });

    it('Returns empty nimi when uri doesnt exist', () => {
        const nimi = impl.uri2Nimi('eioo_1#1');
        assert.strictEqual(nimi, '');
    });

    it('Returns all koodis', () => {
        const koodit = impl.koodit();
        assert.strictEqual(koodit.length, 1);
        assert.strictEqual(koodit[0].uri, 'koodi_1#1');
    });

    it('Returns all selectOptions', () => {
        const options = impl.selectOptions();
        assert.strictEqual(options.length, 1);
        assert.strictEqual(options[0].label, 'Koodi');
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
        assert.strictEqual(options.length, 2);
        assert.strictEqual(options[0].label, 'Koodi');
        assert.strictEqual(options[1].label, 'Koodi10');
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
        assert.strictEqual(i18n.translate('BUTTON_JATKA'), 'BUTTON_JATKA_FI');
        assert.strictEqual(i18n.translateWithLang('BUTTON_JATKA', 'sv'), 'BUTTON_JATKA_SV');
        assert.strictEqual(i18n.translateWithLang('BUTTON_JATKA', 'en'), 'BUTTON_JATKA_EN');
    });
    it('translateNimi translates undefined to empty string', () => {
        const i18n = new I18nImpl(lokalisointi, 'fi');
        assert.strictEqual(i18n.translateNimi(undefined), '');
    });

    it('defaults to key', () => {
        const i18n = new I18nImpl(lokalisointi, 'fi');
        const trans = i18n.translate('NOT_FOUND');
        assert.strictEqual(trans, 'NOT_FOUND');
    });
    it('enriches message', () => {
        const i18n = new I18nImpl(lokalisointi, 'fi');
        const trans = i18n.enrichMessage('ENRICH', [
            { key: 'key1', value: 'val1' },
            { key: 'key2', value: 'val2' },
            { key: 'key3', value: 'val3' },
        ]);
        assert.strictEqual(trans, 'val3 val1 val2');
    });

    it('defaults to key when lokalisointi undefined', () => {
        const i18n = new I18nImpl({} as Lokalisointi, 'fi');
        const trans = i18n.translate('NOT_FOUND');
        assert.strictEqual(trans, 'NOT_FOUND');
    });
});

describe('CASMeImpl', () => {
    it('cheks roles and organisation for button when organisations present', () => {
        const roles = ['APP_ORGANISAATIOHALLINTA_CRUD_111', 'APP_ORGANISAATIOHALLINTA_CRUD_666'];
        assert.strictEqual(organisationCrudAllowedInRoles('333', [{ oid: '111', nimi: { fi: '111' } }], roles), true);
        assert.strictEqual(organisationCrudAllowedInRoles('333', [{ oid: '666', nimi: { fi: '666' } }], roles), true);
        assert.strictEqual(organisationCrudAllowedInRoles('666', [{ oid: '999', nimi: { fi: '999' } }], roles), true);
        assert.strictEqual(organisationCrudAllowedInRoles('333', [{ oid: '999', nimi: { fi: '999' } }], roles), false);
    });
    it('cheks roles and organisation for button when no organisation', () => {
        const roles = ['FOO', 'APP_ORGANISAATIOHALLINTA_CRUD'];
        assert.strictEqual(organisationCrudAllowedInRoles('111', [{ oid: '999', nimi: { fi: '999' } }], roles), false);
    });
});
