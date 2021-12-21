import { CASMe, ConfigurableButton, Language, OrganisaatioNimiJaOid } from '../types/types';
import * as React from 'react';

type Role = 'APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.10.00000000001' | 'APP_ORGANISAATIOHALLINTA_CRUD';
function getRoleItems<A>(myRole: Role, role: Role, items: A[]): A[] {
    return myRole === role ? items : [];
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
                ...getRoleItems<ConfigurableButton>(c, 'APP_ORGANISAATIOHALLINTA_CRUD', ['LOMAKE_LISAA_UUSI_TOIMIJA']),
            ],
            [] as ConfigurableButton[]
        );
    }
    canHaveButton = (button: ConfigurableButton, organisaatioNimiPolku: OrganisaatioNimiJaOid[]) => {
        if (this.roles.includes('APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.10.00000000001')) return true;
        if (this.roles.includes('APP_ORGANISAATIOHALLINTA_CRUD') && organisaatioNimiPolku.length > 2) return true;
        return this.allowedButtons.includes(button) && organisaatioNimiPolku.length > 1;
    };
    canEditIfParent = (organisaatioNimiPolku: OrganisaatioNimiJaOid[]) => {
        if (this.roles.includes('APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.10.00000000001')) return true;
        if (this.roles.includes('APP_ORGANISAATIOHALLINTA_CRUD') && organisaatioNimiPolku.length > 2) return true;
        return false;
    };
}
type CASMeContextType = {
    me: CASMe;
};

export const CasMeContext = React.createContext<CASMeContextType>({
    me: new CASMeImpl({ firstName: '', groups: [], lang: 'fi' as Language, lastName: '', oid: '', roles: [], uid: '' }),
});
