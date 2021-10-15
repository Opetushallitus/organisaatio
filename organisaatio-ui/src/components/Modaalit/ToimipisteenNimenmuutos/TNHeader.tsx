import React, { useContext } from 'react';
import { LanguageContext } from '../../../contexts/contexts';

export default function TNHeader() {
    const { i18n } = useContext(LanguageContext);
<<<<<<< HEAD
    return <span>{i18n.translate('TOIMIPISTEEN_NIMENMUUTOS_TITLE')}</span>;
=======
    return <>{i18n.translate('TOIMIPISTEEN_NIMENMUUTOS_TITLE')}</>;
>>>>>>> 2d1984e1 (implement ytj for uusilomake)
}
