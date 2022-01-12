import { Filters, LocalFilters, SearchFilters } from '../types/types';
import * as React from 'react';

export class SearchFiltersImpl implements SearchFilters {
    filters: Filters;
    localFilters: LocalFilters;
    constructor(isOPHVirkailija = false) {
        this.filters = {
            isOPHVirkailija: isOPHVirkailija,
            naytaPassivoidut: false,
            omatOrganisaatiotSelected: true,
            searchString: '',
        };
        this.localFilters = {
            searchString: '',
            omatOrganisaatiotSelected: true,
        };
    }

    setFilters(filters: Filters): void {
        this.filters = filters;
    }

    setLocalFilters(localFilters: LocalFilters): void {
        this.localFilters = localFilters;
    }
}

type SearchFiltersContextType = {
    searchFilters: SearchFilters;
};
export const SearchFilterContext = React.createContext<SearchFiltersContextType>({
    searchFilters: new SearchFiltersImpl(),
});
