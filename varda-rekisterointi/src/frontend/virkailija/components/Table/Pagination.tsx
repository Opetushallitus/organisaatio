import React, { useContext } from 'react';

import { LanguageContext } from '../../../contexts';

import styles from './Pagination.module.css';

type PaginationProps = {
    pageIndex: number;
    setPageIndex: (n: number) => void;
    pageOptions: number[];
    pageSize: number;
    setPageSize: (n: number) => void;
};

export const Pagination = ({ pageIndex, setPageIndex, pageOptions, pageSize, setPageSize }: PaginationProps) => {
    const { i18n } = useContext(LanguageContext);
    const lastPageIndex = pageOptions[pageOptions.length - 1] ?? 1;
    return (
        <div className={styles.container}>
            <button
                className={styles.page}
                onClick={() => pageIndex > 0 && setPageIndex(pageIndex - 1)}
                disabled={pageIndex === 0}
            >
                &lt;
            </button>
            {pageIndex >= 4 && (
                <>
                    <button className={styles.page} onClick={() => setPageIndex(0)}>
                        1
                    </button>
                    <button className={styles.page} disabled={true}>
                        ...
                    </button>
                </>
            )}
            {pageOptions.slice(...(pageIndex > 3 ? [pageIndex - 2, pageIndex + 3] : [0, 5])).map((i) => (
                <button
                    key={`page-${i}`}
                    className={i === pageIndex ? styles.currentPage : styles.page}
                    onClick={() => setPageIndex(i)}
                >
                    {i + 1}
                </button>
            ))}
            {pageIndex < lastPageIndex - 3 && (
                <>
                    <button className={styles.page} disabled={true}>
                        ...
                    </button>
                    <button className={styles.page} onClick={() => setPageIndex(lastPageIndex)}>
                        {lastPageIndex + 1}
                    </button>
                </>
            )}
            <button
                className={styles.page}
                onClick={() => pageIndex < lastPageIndex && setPageIndex(pageIndex + 1)}
                disabled={pageIndex >= lastPageIndex}
            >
                &gt;
            </button>
            <div className={styles.pageSizeContainer}>
                {i18n.translate('NAYTA_SIVULLA')}:{' '}
                <select
                    className={styles.pageSizeSelect}
                    defaultValue={pageSize}
                    onChange={(e) => setPageSize(Number.parseInt(e.target.value))}
                >
                    <option value="20">20</option>
                    <option value="50">50</option>
                    <option value="100">100</option>
                </select>
            </div>
        </div>
    );
};
