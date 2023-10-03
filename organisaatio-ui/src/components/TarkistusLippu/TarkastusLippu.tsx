import moment, { Moment } from 'moment';
import React from 'react';
import { LocalDate } from '../../types/types';
import isNumber from '@opetushallitus/virkailija-ui-components/utils/isNumber';
import IconWrapper from '../IconWapper/IconWrapper';
import { getUiDateStr } from '../../tools/mappers';
import { useAtom } from 'jotai';
import { LomakeButton } from '../Sivut/LomakeSivu/LomakeFields/LomakeFields';
import { languageAtom } from '../../api/lokalisaatio';
import { ORGANIAATIOTYYPPI_VARHAISKASVATUKSEN_TOIMIPAIKKA } from '../../api/koodisto';

const inputToDate = (input?: number | LocalDate): Moment | undefined => {
    if (!input) return undefined;
    return isNumber(input) ? moment.unix(input / 1000) : moment(input, 'DD.MM.yyyy');
};
export const hasWarning = ({
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
type TarkastusLippuProps = {
    tarkastusPvm?: number;
    alkuPvm: number | LocalDate;
    lakkautusPvm?: number | LocalDate;
    organisaatioTyypit: string[];
};
const TarkastusLippu: React.FC<TarkastusLippuProps> = ({
    tarkastusPvm,
    alkuPvm,
    lakkautusPvm,
    organisaatioTyypit = [],
}) => {
    const [i18n] = useAtom(languageAtom);
    if (organisaatioTyypit.length === 1 && organisaatioTyypit[0] === ORGANIAATIOTYYPPI_VARHAISKASVATUKSEN_TOIMIPAIKKA) {
        return <></>;
    }
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
            />
        </div>
    );
};
type TarkastusLippuButtonProps = TarkastusLippuProps & {
    isDirty: boolean;
    onClick: () => void;
};
const TarkastusLippuButton: React.FC<TarkastusLippuButtonProps> = ({
    tarkastusPvm,
    alkuPvm,
    lakkautusPvm,
    organisaatioTyypit = [],
    isDirty,
    onClick,
}) => {
    if (organisaatioTyypit.length === 1 && organisaatioTyypit[0] === ORGANIAATIOTYYPPI_VARHAISKASVATUKSEN_TOIMIPAIKKA) {
        return <></>;
    }
    return (
        <LomakeButton
            disabled={isDirty}
            label={'LOMAKE_MERKITSE_TARKISTUS'}
            icon={() => (
                <TarkastusLippu
                    tarkastusPvm={tarkastusPvm}
                    alkuPvm={alkuPvm}
                    lakkautusPvm={lakkautusPvm}
                    organisaatioTyypit={organisaatioTyypit}
                />
            )}
            onClick={onClick}
        />
    );
};

export { TarkastusLippu, TarkastusLippuButton };
