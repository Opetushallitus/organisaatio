import React from 'react';
import DatePickerInput from '@opetushallitus/virkailija-ui-components/DatePickerInput';

import { BodyKehys, BodyKentta } from '../ModalFields/ModalFields';
import { LocalDate } from '../../../types/types';

type TLProps = {
    date?: LocalDate;
    setDate: (d: Date) => void;
};

export default function TLBody({ date, setDate }: TLProps) {
    return (
        <BodyKehys>
            <BodyKentta label={'TOIMIPISTEEN_LAKKAUTUS_PVM'}>
                <DatePickerInput value={date} onChange={setDate} />
            </BodyKentta>
        </BodyKehys>
    );
}
