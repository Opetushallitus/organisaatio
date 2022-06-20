import * as React from 'react';
import { Nimi } from '../../types/types';
import { useAtom } from 'jotai';
import { languageAtom } from '../../api/lokalisaatio';
import { Link } from 'react-router-dom';

export const DecoratedNimi: React.FC<{ nimi: Nimi; status: string }> = ({ nimi, status }) => {
    const [i18n] = useAtom(languageAtom);
    return (
        <>
            {' '}
            {[
                i18n.translateNimi(nimi),
                ...((status !== 'AKTIIVINEN' && [' (', i18n.translate(`LABEL_${status.toUpperCase()}`), ')']) || []),
            ]}
        </>
    );
};

export const OrganisaatioLink: React.FC<{ oid: string; nimi: Nimi; status: string }> = ({ oid, nimi, status }) => {
    return (
        <Link to={`/lomake/${oid}`}>
            <DecoratedNimi nimi={nimi} status={status} />
        </Link>
    );
};
