import { Filters, SearchFilters } from '../types/types';
import * as React from 'react';

export class SearchFiltersImpl implements SearchFilters {
    filters: Filters;
    constructor(isOPHVirkailija = false) {
        this.filters = {
            isOPHVirkailija: isOPHVirkailija,
            naytaPassivoidut: false,
            omatOrganisaatiotSelected: false,
            searchString: '',
        };
    }

    setFilters(filters: Filters): void {
        this.filters = filters;
    }
}

type SearchFiltersContextType = {
    searchFilters: SearchFilters;
};
export const SearchFilterContext = React.createContext<SearchFiltersContextType>({
    searchFilters: new SearchFiltersImpl(),
});
