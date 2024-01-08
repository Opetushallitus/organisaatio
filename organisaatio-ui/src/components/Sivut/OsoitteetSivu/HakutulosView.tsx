import { HaeRequest, Hakutulos } from './OsoitteetApi';
import styles from './HakutulosView.module.css';
import { useHistory } from 'react-router-dom';
import { HakutulosTable } from './HakutulosTable';
import React from 'react';
import { LinklikeButton } from './LinklikeButton';
import { API_CONTEXT } from '../../../contexts/constants';
import Button from '@opetushallitus/virkailija-ui-components/Button';

type HakutulosViewProps = {
    muotoilematonViestiEnabled: boolean;
    request?: HaeRequest;
    results?: Hakutulos[];
};

export function HakutulosView({ muotoilematonViestiEnabled, request, results }: HakutulosViewProps) {
    const history = useHistory();

    function navigateBackToSearch() {
        history.goBack();
    }

    if (typeof results === 'undefined') {
        navigateBackToSearch();
        return null;
    }

    return (
        <div className={styles.HakutulosView}>
            <div className={styles.TitleRow}>
                <div className={styles.Title}>
                    <h1 className={styles.TitleText}>Hakutulokset</h1>
                    <span className={styles.TitleAlt}>( {results.length} )</span>
                </div>
                <LinklikeButton onClick={navigateBackToSearch}>
                    <IconArrowBack /> Muokkaa hakua
                </LinklikeButton>
            </div>
            <div className={styles.SininenPalkki}>{results.length} hakutulosta valittu</div>
            <HakutulosTable results={results} />
            <div className={styles.ButtonRow}>
                {muotoilematonViestiEnabled && <Button>Kirjoita sähköpostiviesti</Button>}
                <form action={`${API_CONTEXT}/osoitteet/hae/xls`} method={'POST'}>
                    <input type={'hidden'} name={'request'} value={JSON.stringify(request)} />
                    <Button variant={muotoilematonViestiEnabled ? 'outlined' : 'contained'} type={'submit'}>
                        Lataa Excel
                    </Button>
                </form>
            </div>
        </div>
    );
}

function IconArrowBack() {
    return (
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path
                d="M3.5625 10.9375L11.3125 18.6875L10 20L0 10L10 0L11.3125 1.3125L3.5625 9.0625H20V10.9375H3.5625Z"
                fill="#0A789C"
            />
        </svg>
    );
}
