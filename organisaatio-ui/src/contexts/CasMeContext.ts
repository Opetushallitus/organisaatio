import { CASMe, ConfigurableButton, Language, OrganisaatioNimiJaOid } from '../types/types';
import * as React from 'react';
import { ROOT_OID } from './constants';

const ORGANISAATIO_CRUD = 'APP_ORGANISAATIOHALLINTA_CRUD';
const OPH_CRUD = `${ORGANISAATIO_CRUD}_${ROOT_OID}`;
const getRoleItems = <A>(myRole: string, role: string, items: A[]): A[] => {
    return myRole === role ? items : [];
};
export const organisationAllowedInRoles = (
    organisaatioNimiPolku: OrganisaatioNimiJaOid[],
    roles: string[]
): boolean => {
    const oidsFromRoles = roles
        .filter((a) => a.startsWith(`${ORGANISAATIO_CRUD}_`))
        .map((a) => a.substr(ORGANISAATIO_CRUD.length + 1));
    const isAllowed = oidsFromRoles.reduce((p, c) => {
        return p || !!organisaatioNimiPolku.find((a) => a.oid === c);
    }, false);
    return isAllowed;
};

export class CASMeImpl implements CASMe {
    firstName: string;
    groups: string[];
    lang: Language;
    lastName: string;
    oid: string;
    roles: string[];
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
            (p, c) => [...p, ...getRoleItems<ConfigurableButton>(c, ORGANISAATIO_CRUD, ['LOMAKE_LISAA_UUSI_TOIMIJA'])],
            [] as ConfigurableButton[]
        );
    }
    canHaveButton = (button: ConfigurableButton, organisaatioNimiPolku: OrganisaatioNimiJaOid[]) => {
        if (this.roles.includes(OPH_CRUD)) return true;
        if (organisaatioNimiPolku.length > 2 && organisationAllowedInRoles(organisaatioNimiPolku, this.roles))
            return true;
        return (
            organisaatioNimiPolku.length > 1 &&
            organisationAllowedInRoles(organisaatioNimiPolku, this.roles) &&
            this.allowedButtons.includes(button)
        );
    };
    canEditIfParent = (organisaatioNimiPolku: OrganisaatioNimiJaOid[]) => {
        if (this.roles.includes(OPH_CRUD)) return true;
        if (organisaatioNimiPolku.length > 2 && organisationAllowedInRoles(organisaatioNimiPolku, this.roles))
            return true;
        return false;
    };
}
type CASMeContextType = {
    me: CASMe;
};

export const CasMeContext = React.createContext<CASMeContextType>({
    me: new CASMeImpl({ firstName: '', groups: [], lang: 'fi' as Language, lastName: '', oid: '', roles: [], uid: '' }),
});
