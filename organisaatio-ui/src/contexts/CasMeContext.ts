import { Language } from '../types/types';
import * as React from 'react';
import { CASMe, ConfigurableButton } from '../types/apiTypes';
type Role = 'APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.10.00000000001' | 'APP_ORGANISAATIOHALLINTA_CRUD';
function getRoleButtons(myRole: Role, role: Role, buttons: ConfigurableButton[]): ConfigurableButton[] {
    return myRole === role ? buttons : [];
}

export class CASMeImpl implements CASMe {
    firstName: string;
    groups: string[];
    lang: Language;
    lastName: string;
    oid: string;
    roles: Role[];
    uid: string;
    allowedButtons: string[];
    constructor(casMe) {
        this.firstName = casMe.firstName;
        this.groups = casMe.groups;
        this.lang = casMe.lang;
        this.lastName = casMe.lastName;
        this.oid = casMe.oid;
        this.roles = casMe.roles;
        this.uid = casMe.uid;
        this.allowedButtons = casMe.roles.reduce(
            (p, c) => [
                ...p,
                ...getRoleButtons(c, 'APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.10.00000000001', [
                    'LOMAKE_YHDISTA_ORGANISAATIO',
                    'LOMAKE_SIIRRA_ORGANISAATIO',
                    'LOMAKE_LISAA_UUSI_TOIMIJA',
                    'TAULUKKO_LISAA_UUSI_TOIMIJA',
                ]),
                ...getRoleButtons(c, 'APP_ORGANISAATIOHALLINTA_CRUD', ['BUTTON_TALLENNA']),
            ],
            [] as string[]
        );
    }
    canHaveButton = (button: ConfigurableButton) => this.allowedButtons.includes(button);
}
type CASMeContextType = {
    me: CASMe;
};

export const CasMeContext = React.createContext<CASMeContextType>({
    me: new CASMeImpl({ firstName: '', groups: [], lang: 'fi' as Language, lastName: '', oid: '', roles: [], uid: '' }),
});
