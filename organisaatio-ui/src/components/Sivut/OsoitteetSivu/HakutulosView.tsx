import { Hakutulos, useHakutulos } from './OsoitteetApi';
import searchStyles from './SearchView.module.css';
import styles from './HakutulosView.module.css';
import { useHistory, useParams } from 'react-router-dom';
import { OrganisaatioHakutulosTable } from './OrganisaatioHakutulosTable';
import { KayttajaHakutulosTable } from './KayttajaHakutulosTable';
import React from 'react';
import { LinklikeButton } from './LinklikeButton';
import { API_CONTEXT } from '../../../contexts/constants';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';
import { GenericOsoitepalveluError } from './GenericOsoitepalveluError';

type HakutulosViewProps = {
    muotoilematonViestiEnabled: boolean;
    result?: Hakutulos;
};

export function HakutulosView({ muotoilematonViestiEnabled }: HakutulosViewProps) {
    const history = useHistory();
    const { hakutulosId } = useParams<{ hakutulosId: string }>();
    const hakutulos = useHakutulos(hakutulosId);

    function navigateBackToSearch() {
        history.goBack();
    }
    function onWriteMail() {
        history.push(`/osoitteet/hakutulos/${hakutulosId}/viesti`);
    }

    if (hakutulos.state === 'ERROR') {
        return <GenericOsoitepalveluError />;
    }

    if (hakutulos.state === 'LOADING') {
        return (
            <div className={searchStyles.LoadingOverlay}>
                <Spin />
            </div>
        );
    }

    const rows = hakutulos.value.rows;

    return (
        <div className={styles.HakutulosView}>
            <div className={styles.TitleRow}>
                <div className={styles.Title}>
                    <h1 className={styles.TitleText}>Hakutulokset</h1>
                    <span className={styles.TitleAlt}>( {rows.length} )</span>
                </div>
                <LinklikeButton onClick={navigateBackToSearch}>
                    <IconArrowBack /> Muokkaa hakua
                </LinklikeButton>
            </div>
            <div className={styles.SininenPalkki}>{rows.length} hakutulosta valittu</div>
            {hakutulos.value.type === 'organisaatio' ? (
                <OrganisaatioHakutulosTable rows={hakutulos.value.rows} />
            ) : (
                <KayttajaHakutulosTable rows={hakutulos.value.rows} />
            )}
            <div className={styles.ButtonRow}>
                {muotoilematonViestiEnabled && <Button onClick={onWriteMail}>Kirjoita sähköpostiviesti</Button>}
                <form action={`${API_CONTEXT}/osoitteet/hae/xls`} method={'POST'}>
                    <input type={'hidden'} name={'resultId'} value={hakutulos.value.id} />
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
