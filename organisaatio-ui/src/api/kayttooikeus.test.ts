import assert from 'assert/strict';
import { describe, it } from 'node:test';

import { mapApiToUI } from './kayttooikeus';
import { CASMeImpl } from '../contexts/CasMeContext';
import { Language } from '../types/types';

describe('kayttooikeus', () => {
    describe('mapApiToUI', () => {
        it('can handle empty input', () => {
            assert.strictEqual(
                JSON.stringify(
                    mapApiToUI({
                        uid: '',
                        oid: '',
                        firstName: '',
                        lastName: '',
                        groups: [],
                        roles: '',
                        lang: 'fi',
                    })
                ),
                JSON.stringify(
                    new CASMeImpl({
                        uid: '',
                        oid: '',
                        firstName: '',
                        lastName: '',
                        groups: [],
                        roles: [],
                        lang: 'fi' as Language,
                    })
                )
            );
        });
        it('can handle roles array', () => {
            assert.strictEqual(
                JSON.stringify(
                    mapApiToUI({
                        uid: '',
                        oid: '',
                        firstName: '',
                        lastName: '',
                        groups: [],
                        roles: '["one","two"]',
                        lang: 'fi',
                    })
                ),
                JSON.stringify(
                    new CASMeImpl({
                        uid: '',
                        oid: '',
                        firstName: '',
                        lastName: '',
                        groups: [],
                        roles: ['one', 'two'],
                        lang: 'fi',
                    })
                )
            );
        });
    });
});
