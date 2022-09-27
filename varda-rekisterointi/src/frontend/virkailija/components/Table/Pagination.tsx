import React from 'react';

import styles from './Pagination.module.css';

type PaginationProps = {
    pageIndex: number;
    setPageIndex: (n: number) => void;
    pages: number;
};

export const Pagination = ({ pageIndex, setPageIndex, pages }: PaginationProps) => {
    return (
        <div className={styles.container}>
            <button
                className={styles.page}
                onClick={() => pageIndex > 0 && setPageIndex(pageIndex - 1)}
                disabled={pageIndex === 0}
            >
                &lt;
            </button>
            {Array.from({ length: pages }, (_, i) => (
                <button className={i === pageIndex ? styles.currentPage : styles.page} onClick={() => setPageIndex(i)}>
                    {i + 1}
                </button>
            ))}
            <button
                className={styles.page}
                onClick={() => pageIndex < pages - 1 && setPageIndex(pageIndex + 1)}
                disabled={pageIndex >= pages - 1}
            >
                &gt;
            </button>
        </div>
    );
};
