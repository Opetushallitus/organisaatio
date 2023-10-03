import {
    UseColumnOrderInstanceProps,
    UseExpandedInstanceProps,
    UseExpandedOptions,
    UseFiltersInstanceProps,
    UseFiltersOptions,
    UseGlobalFiltersInstanceProps,
    UseGlobalFiltersOptions,
    UseGroupByInstanceProps,
    UseGroupByOptions,
    UsePaginationInstanceProps,
    UsePaginationOptions,
    UseResizeColumnsOptions,
    UseRowSelectInstanceProps,
    UseRowSelectOptions,
    UseRowStateInstanceProps,
    UseRowStateOptions,
    UseSortByInstanceProps,
    UseSortByOptions,
} from 'react-table';

declare module 'react-table' {
    // take this file as-is, or comment out the sections that don't apply to your plugin configuration

    export interface TableOptions<D extends Record<string, unknown>>
        extends UseExpandedOptions<D>,
            UseFiltersOptions<D>,
            UseGlobalFiltersOptions<D>,
            UseGroupByOptions<D>,
            UsePaginationOptions<D>,
            UseResizeColumnsOptions<D>,
            UseRowSelectOptions<D>,
            UseRowStateOptions<D>,
            UseSortByOptions<D> {}

    export interface TableInstance<D extends Record<string, unknown> = Record<string, unknown>>
        extends UseColumnOrderInstanceProps<D>,
            UseExpandedInstanceProps<D>,
            UseFiltersInstanceProps<D>,
            UseGlobalFiltersInstanceProps<D>,
            UseGroupByInstanceProps<D>,
            UsePaginationInstanceProps<D>,
            UseRowSelectInstanceProps<D>,
            UseRowStateInstanceProps<D>,
            UseSortByInstanceProps<D> {}

    export interface TableState<D extends Record<string, unknown> = Record<string, unknown>>
        extends UseColumnOrderState<D>,
            UseExpandedState<D>,
            UseFiltersState<D>,
            UseGlobalFiltersState<D>,
            UseGroupByState<D>,
            UsePaginationState<D>,
            UseResizeColumnsState<D>,
            UseRowSelectState<D>,
            UseRowStateState<D>,
            UseSortByState<D> {}

    export interface Row<D extends Record<string, unknown> = Record<string, unknown>>
        extends UseTableRowProps<D>,
            UseExpandedRowProps<D>,
            UseGroupByRowProps<D> {}
}
