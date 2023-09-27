import { CASMe, ConfigurableButton, ConfigurableLomake, Language, OrganisaatioNimiJaOid } from '../types/types';
import { ROOT_OID } from './constants';

const ORGANISAATIO_CRUD = 'APP_ORGANISAATIOHALLINTA_CRUD';
const OPH_CRUD = `${ORGANISAATIO_CRUD}_${ROOT_OID}`;
const getRoleItems = <A>(myRole: string, role: string, items: A[]): A[] => {
    return myRole === role ? items : [];
};
const getOidsFromRoles = (roles: string[]): string[] => {
    return roles
        .filter((a) => a.startsWith(`${ORGANISAATIO_CRUD}_`))
        .map((a) => a.substr(ORGANISAATIO_CRUD.length + 1));
};
export const organisationAllowedInRoles = (
    oid: string,
    organisaatioNimiPolku: OrganisaatioNimiJaOid[],
    roles: string[]
): boolean => {
    const oidsFromRoles = getOidsFromRoles(roles);
    if (oidsFromRoles.includes(oid)) {
        return true;
    }
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
    allowedButtons: { button: ConfigurableButton; fromLevel: number }[];
    allowedLomakes: { lomake: ConfigurableLomake; fromLevel: number }[];
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
                ...getRoleItems<{ button: ConfigurableButton; fromLevel: number }>(c, ORGANISAATIO_CRUD, [
                    { button: 'LOMAKE_LISAA_UUSI_TOIMIJA', fromLevel: 1 },
                    { button: 'BUTTON_TALLENNA', fromLevel: 0 },
                ]),
            ],
            [] as ConfigurableButton[]
        );
        this.allowedLomakes = casMe.roles.reduce(
            (p, c) => [
                ...p,
                ...getRoleItems<{ lomake: ConfigurableLomake; fromLevel: number }>(c, ORGANISAATIO_CRUD, [
                    { lomake: 'LOMAKE_KOSKI_POSTI', fromLevel: 1 },
                    { lomake: 'LOMAKE_YHTEYSTIEDOT', fromLevel: 1 },
                    { lomake: 'LOMAKE_KRIISI_VIESTINTA', fromLevel: 0 },
                ]),
            ],
            []
        );
    }
    canHaveButton = (button: ConfigurableButton, oid: string, organisaatioNimiPolku: OrganisaatioNimiJaOid[]) => {
        if (this.roles.includes(OPH_CRUD)) return true;
        if (organisaatioNimiPolku.length > 2 && organisationAllowedInRoles(oid, organisaatioNimiPolku, this.roles))
            return true;
        return (
            organisationAllowedInRoles(oid, organisaatioNimiPolku, this.roles) &&
            this.allowedButtons.reduce(
                (p, c) => p || (c.button === button && organisaatioNimiPolku.length > c.fromLevel),
                false
            )
        );
    };
    canEditLomake = (lomake: ConfigurableLomake, oid: string, organisaatioNimiPolku: OrganisaatioNimiJaOid[]) => {
        if (this.roles.includes(OPH_CRUD)) return true;
        return (
            organisationAllowedInRoles(oid, organisaatioNimiPolku, this.roles) &&
            this.allowedLomakes.reduce(
                (p, c) => p || (c.lomake === lomake && organisaatioNimiPolku.length > c.fromLevel),
                false
            )
        );
    };
    canEditIfParent = (oid: string, organisaatioNimiPolku: OrganisaatioNimiJaOid[]) => {
        if (this.roles.includes(OPH_CRUD)) return true;
        if (organisaatioNimiPolku.length > 2 && organisationAllowedInRoles(oid, organisaatioNimiPolku, this.roles))
            return true;
        return false;
    };
    getCRUDOids = () => {
        return getOidsFromRoles(this.roles);
    };
    isOphUser = () => {
        return this.roles.includes(OPH_CRUD);
    };
}
