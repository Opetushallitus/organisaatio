import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';

import { Hakutulos, HakutulosRow, KayttajaHakutulosRow, useHakutulos } from './OsoitteetApi';
import { OrganisaatioHakutulosTable } from './OrganisaatioHakutulosTable';
import { KayttajaHakutulosTable } from './KayttajaHakutulosTable';
import { LinklikeButton } from './LinklikeButton';
import { API_CONTEXT } from '../../../contexts/constants';
import { GenericOsoitepalveluError } from './GenericOsoitepalveluError';

import styles from './HakutulosView.module.css';
import searchStyles from './SearchView.module.css';

type HakutulosViewProps = {
    result?: Hakutulos;
    selection: Set<string>;
    setSelection: (s: Set<string>) => void;
    hakutulosCache?: Hakutulos;
};

export function HakutulosView({ selection, setSelection, hakutulosCache }: HakutulosViewProps) {
    const navigate = useNavigate();
    const location = useLocation();
    const { hakutulosId } = useParams<{ hakutulosId: string }>();
    const fetchedHakutulos = useHakutulos(hakutulosId);
    const [hakutulos, setHakutulos] = useState(hakutulosCache?.id === hakutulosId ? hakutulosCache : undefined);
    const [allOids, setAllOids] = useState(
        hakutulosCache?.rows.map((r: HakutulosRow | KayttajaHakutulosRow) => r.oid).join(',')
    );
    const selectionString = hakutulos?.rows.length === selection.size ? allOids : Array.from(selection).join(',');

    useEffect(() => {
        if (hakutulosCache?.id !== hakutulosId && fetchedHakutulos?.state === 'OK') {
            setHakutulos(fetchedHakutulos.value);
            const oids = fetchedHakutulos?.value.rows.map((r: HakutulosRow | KayttajaHakutulosRow) => r.oid);
            setSelection(new Set(oids));
            setAllOids(oids.join(','));
        }
    }, [fetchedHakutulos]);

    function navigateBackToSearch() {
        if (location.key) {
            navigate(-1);
        } else {
            navigate('/osoitteet');
        }
    }

    function onWriteMail() {
        navigate(`/osoitteet/hakutulos/${hakutulosId}/viesti`);
    }

    if (fetchedHakutulos?.state === 'ERROR') {
        return <GenericOsoitepalveluError />;
    }

    if (!hakutulos || fetchedHakutulos?.state === 'LOADING') {
        return (
            <div className={searchStyles.LoadingOverlay}>
                <Spin />
            </div>
        );
    }

    return (
        <div className={styles.HakutulosView}>
            <div className={styles.TitleRow}>
                <div className={styles.Title}>
                    <h1 className={styles.TitleText}>Hakutulokset</h1>
                    <span className={styles.TitleAlt}>( {hakutulos.rows.length} )</span>
                </div>
                <LinklikeButton onClick={navigateBackToSearch}>
                    <IconArrowBack /> Muokkaa hakua
                </LinklikeButton>
            </div>
            <div className={styles.SininenPalkki}>{selection.size} hakutulosta valittu</div>
            {hakutulos.type === 'organisaatio' ? (
                <OrganisaatioHakutulosTable rows={hakutulos.rows} selection={selection} setSelection={setSelection} />
            ) : (
                <KayttajaHakutulosTable rows={hakutulos.rows} selection={selection} setSelection={setSelection} />
            )}
            <div className={styles.ButtonRow}>
                <Button onClick={onWriteMail} disabled={!selection.size}>
                    Kirjoita sähköpostiviesti
                </Button>
                <form action={`${API_CONTEXT}/osoitteet/hae/xls`} method="POST">
                    <input type="hidden" name="resultId" value={hakutulos.id} />
                    {selectionString && <input type="hidden" name="selectedOids" value={selectionString} />}
                    <Button variant="outlined" type="submit" disabled={!selection.size}>
                        Lataa Excel
                    </Button>
                </form>
            </div>
        </div>
    );
}

export function IconArrowBack() {
    return (
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path
                d="M3.5625 10.9375L11.3125 18.6875L10 20L0 10L10 0L11.3125 1.3125L3.5625 9.0625H20V10.9375H3.5625Z"
                fill="#0A789C"
            />
        </svg>
    );
}
