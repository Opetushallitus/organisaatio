import moment, { Moment } from 'moment';
import React, { useContext } from 'react';
import { LocalDate } from '../../types/types';
import isNumber from '@opetushallitus/virkailija-ui-components/utils/isNumber';
import IconWrapper from '../IconWapper/IconWrapper';
import { getUiDateStr } from '../../tools/mappers';
import { LanguageContext } from '../../contexts/LanguageContext';

const inputToDate = (input?: number | LocalDate): Moment | undefined => {
    if (!input) return undefined;
    return isNumber(input) ? moment.unix(input / 1000) : moment(input, 'DD.MM.yyyy');
};
const hasWarning = ({
    tarkastusDate,
    alkuDate,
    lakkautusDate,
}: {
    tarkastusDate?: Moment;
    alkuDate?: Moment;
    lakkautusDate?: Moment;
}): boolean => {
    const now = moment();
    const lastYear = moment();
    lastYear.subtract(1, 'years');
    const tarkastusOk = tarkastusDate && tarkastusDate.isAfter(lastYear);
    const activeNow = alkuDate && alkuDate.isBefore(now) && (!lakkautusDate || lakkautusDate.isAfter(now));
    return !tarkastusOk && !!activeNow;
};

const TarkastusLippu = ({
    tarkastusPvm,
    alkuPvm,
    lakkautusPvm,
}: {
    tarkastusPvm?: number;
    alkuPvm: number | LocalDate;
    lakkautusPvm?: number | LocalDate;
}) => {
    const { i18n } = useContext(LanguageContext);
    const tarkastusDate = inputToDate(tarkastusPvm);
    const alkuDate = inputToDate(alkuPvm);
    const lakkautusDate = inputToDate(lakkautusPvm);
    const warning = hasWarning({ tarkastusDate, alkuDate, lakkautusDate });
    const iconColor = warning ? '#e44e4e' : '#159ecb';
    const message = tarkastusDate
        ? i18n.enrichMessage('VIIMEINEN_TARKASTUS_{tarkastusPvm}', [
              { key: 'tarkastusPvm', value: getUiDateStr(tarkastusDate?.toDate()) },
          ])
        : i18n.translate('TARKASTUS_PUUTTUU');
    return (
        <div title={message}>
            <IconWrapper
                inline={true}
                icon="el:flag-alt"
                height={'1.2rem'}
                color={iconColor}
                name={'TARKISTUS_LIPPU'}
                alt={message}
            />
        </div>
    );
};

export default TarkastusLippu;
