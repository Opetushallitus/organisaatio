import React from 'react';
import PohjaModaali from '../PohjaModaali/PohjaModaali';
import YTJHeader from './YTJHeader';
import YTJBody from './YTJBody';
import YTJFooter from './YTJFooter';
import { YtjOrganisaatio } from '../../../types/apiTypes';

type ModaaliProps = {
    ytunnus?: string;
    korvaaOrganisaatio: (ytiedot: YtjOrganisaatio) => void;
    suljeModaali: () => void;
};
export default function YTJModaali({ ytunnus, korvaaOrganisaatio, suljeModaali }: ModaaliProps) {
    return (
        <PohjaModaali
            header={<YTJHeader />}
            body={<YTJBody ytunnus={ytunnus} korvaaOrganisaatio={korvaaOrganisaatio} />}
            footer={
                <YTJFooter
                    peruutaCallback={() => {
                        suljeModaali();
                    }}
                />
            }
            suljeCallback={() => suljeModaali()}
        />
    );
}
