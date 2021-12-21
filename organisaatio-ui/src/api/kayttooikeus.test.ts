import { mapApiToUI } from './kayttooikeus';
import { CASMeImpl } from '../contexts/CasMeContext';
import { Language } from '../types/types';

describe('kayttooikeus', () => {
    describe('mapApiToUI', () => {
        it('can handle empty input', () => {
            expect(
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
                )
            ).toEqual(
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
            expect(
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
                )
            ).toEqual(
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
