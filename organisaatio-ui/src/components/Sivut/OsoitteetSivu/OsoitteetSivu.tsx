import React from 'react';
import PohjaSivu from '../PohjaSivu/PohjaSivu';
import { PaaOsio } from '../LomakeSivu/LomakeFields/LomakeFields';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';

const OsoitteetSivu = () => {
    return (
        <PohjaSivu>
            <PaaOsio>
                <Spin />
            </PaaOsio>
        </PohjaSivu>
    );
};

export default OsoitteetSivu;
