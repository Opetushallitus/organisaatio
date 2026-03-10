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

    // eslint-disable-next-line @typescript-eslint/consistent-type-definitions
    export interface TableOptions<D extends object>
        extends
            UseExpandedOptions<D>,
            UseFiltersOptions<D>,
            UseGlobalFiltersOptions<D>,
            UseGroupByOptions<D>,
            UsePaginationOptions<D>,
            UseResizeColumnsOptions<D>,
            UseRowSelectOptions<D>,
            UseRowStateOptions<D>,
            UseSortByOptions<D> {}

    // eslint-disable-next-line @typescript-eslint/consistent-type-definitions
    export interface TableInstance<D extends object>
        extends
            UseColumnOrderInstanceProps<D>,
            UseExpandedInstanceProps<D>,
            UseFiltersInstanceProps<D>,
            UseGlobalFiltersInstanceProps<D>,
            UseGroupByInstanceProps<D>,
            UsePaginationInstanceProps<D>,
            UseRowSelectInstanceProps<D>,
            UseRowStateInstanceProps<D>,
            UseSortByInstanceProps<D> {}

    // eslint-disable-next-line @typescript-eslint/consistent-type-definitions
    export interface TableState<D extends object>
        extends
            UseColumnOrderState<D>,
            UseExpandedState<D>,
            UseFiltersState<D>,
            UseGlobalFiltersState<D>,
            UseGroupByState<D>,
            UsePaginationState<D>,
            UseResizeColumnsState<D>,
            UseRowSelectState<D>,
            UseRowStateState<D>,
            UseSortByState<D> {}

    // eslint-disable-next-line @typescript-eslint/consistent-type-definitions
    export interface Row<D extends object> extends UseTableRowProps<D>, UseExpandedRowProps<D>, UseGroupByRowProps<D> {}
}
