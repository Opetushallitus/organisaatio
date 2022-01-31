const tyyppiFilter = (rows, id, filterValue) => {
    const filterNested = (row, id, filterValue) => {
        if (row[id]?.some((tyyppi) => filterValue.includes(tyyppi))) {
            return true;
        } else if (row.subRows.length > 0) return !!row.subRows.find((subrow) => filterNested(subrow, id, filterValue));
        return false;
    };

    if (filterValue.length === 0) return rows;
    return rows.filter((row) =>
        filterNested(
            row.original,
            id,
            filterValue.map((tyyppi) => tyyppi.value)
        )
    );
};

const containingOidsFilter = (rows, id, filterValue) => {
    if (filterValue.length === 0) return rows;
    return rows.filter((row) => {
        const rowValue = row.values[id];
        return rowValue.some((r) => filterValue.includes(r));
    });
};

export { tyyppiFilter, containingOidsFilter };
