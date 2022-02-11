import { RemoteFilters, LocalFilters } from '../types/types';
import { atomWithStorage } from 'jotai/utils';

export const localFiltersAtom = atomWithStorage<LocalFilters>('localFilters', {
    searchString: '',
    omatOrganisaatiotSelected: true,
    organisaatioTyyppi: [],
    oppilaitosTyyppi: [],
    showVakaToimijat: false,
});

export const remoteFiltersAtom = atomWithStorage<RemoteFilters>('remoteFilters', {
    searchString: '',
    naytaPassivoidut: false,
});
