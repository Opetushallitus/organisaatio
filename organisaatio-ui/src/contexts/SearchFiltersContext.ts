import { RemoteFilters, LocalFilters } from '../types/types';
import { atomWithStorage } from 'jotai/utils';

export const localFiltersAtom = atomWithStorage<LocalFilters>('localFilters', {
    searchString: '',
    omatOrganisaatiotSelected: true,
});

export const remoteFiltersAtom = atomWithStorage<RemoteFilters>('remoteFilters', {
    searchString: '',
    naytaPassivoidut: false,
});
