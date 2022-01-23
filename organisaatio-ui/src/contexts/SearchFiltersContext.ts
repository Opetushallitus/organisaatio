import { Filters, LocalFiltersAtom } from '../types/types';
import { atomWithStorage } from 'jotai/utils';

export const localFiltersAtom = atomWithStorage<LocalFiltersAtom>('localFilters', {
    searchString: '',
    omatOrganisaatiotSelected: true,
});

export const remoteFiltersAtom = atomWithStorage<Filters>('remoteFilters', {
    searchString: '',
    naytaPassivoidut: false,
});
