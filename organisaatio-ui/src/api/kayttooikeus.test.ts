import { mapApiToUI } from './kayttooikeus';

describe('kayttooikeus', () => {
    describe('mapApiToUI', () => {
        it('can handle empty input', () => {
            expect(
                mapApiToUI({
                    uid: '',
                    oid: '',
                    firstName: '',
                    lastName: '',
                    groups: [],
                    roles: '',
                    lang: 'fi',
                })
            ).toEqual({
                uid: '',
                oid: '',
                firstName: '',
                lastName: '',
                groups: [],
                roles: [],
                lang: 'fi',
            });
        });
        it('can handle roles array', () => {
            expect(
                mapApiToUI({
                    uid: '',
                    oid: '',
                    firstName: '',
                    lastName: '',
                    groups: [],
                    roles: '["one","two"]',
                    lang: 'fi',
                })
            ).toEqual({
                uid: '',
                oid: '',
                firstName: '',
                lastName: '',
                groups: [],
                roles: ['one', 'two'],
                lang: 'fi',
            });
        });
    });
});
