import { CASMe, ConfigurableButton, ConfigurableLomake, Language, OrganisaatioNimiJaOid } from '../types/types';
import { ROOT_OID } from './constants';

const ORGANISAATIO_CRUD = 'APP_ORGANISAATIOHALLINTA_CRUD';
const OPH_CRUD = `${ORGANISAATIO_CRUD}_${ROOT_OID}`;
const getRoleItems = <A>(myRole: string, role: string, items: A[]): A[] => {
    return myRole === role ? items : [];
};
const getCrudOidsFromRoles = (roles: string[]): string[] => {
    return roles
        .filter((a) => a.startsWith(`${ORGANISAATIO_CRUD}_`))
        .map((a) => a.substr(ORGANISAATIO_CRUD.length + 1));
};
export const organisationCrudAllowedInRoles = (
    oid: string,
    organisaatioNimiPolku: OrganisaatioNimiJaOid[],
    roles: string[]
): boolean => {
    const oidsFromRoles = getCrudOidsFromRoles(roles);
    if (oidsFromRoles.includes(oid)) {
        return true;
    }
    const isAllowed = oidsFromRoles.reduce((p, c) => {
        return p || !!organisaatioNimiPolku.find((a) => a.oid === c);
    }, false);
    return isAllowed;
};

type AllowedButton = { button: ConfigurableButton; fromLevel: number };
type AllowedLomake = { lomake: ConfigurableLomake; fromLevel: number };
type CasMeProps = {
    firstName: string;
    groups: string[];
    lang: Language;
    lastName: string;
    oid: string;
    roles: string[];
    uid: string;
};

export class CASMeImpl implements CASMe {
    firstName: string;
    groups: string[];
    lang: Language;
    lastName: string;
    oid: string;
    roles: string[];
    uid: string;
    allowedButtons: AllowedButton[];
    allowedLomakes: AllowedLomake[];
    constructor(casMe: CasMeProps) {
        this.firstName = casMe.firstName;
        this.groups = casMe.groups;
        this.lang = casMe.lang;
        this.lastName = casMe.lastName;
        this.oid = casMe.oid;
        this.roles = casMe.roles;
        this.uid = casMe.uid;
        this.allowedButtons = casMe.roles.reduce(
            (p: AllowedButton[], c: string) => [
                ...p,
                ...getRoleItems<AllowedButton>(c, ORGANISAATIO_CRUD, [
                    { button: 'LOMAKE_LISAA_UUSI_TOIMIJA', fromLevel: 1 },
                    { button: 'BUTTON_TALLENNA', fromLevel: 0 },
                ]),
            ],
            []
        );
        this.allowedLomakes = casMe.roles.reduce(
            (p: AllowedLomake[], c: string) => [
                ...p,
                ...getRoleItems<AllowedLomake>(c, ORGANISAATIO_CRUD, [
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
        if (organisaatioNimiPolku.length > 2 && organisationCrudAllowedInRoles(oid, organisaatioNimiPolku, this.roles))
            return true;
        return (
            organisationCrudAllowedInRoles(oid, organisaatioNimiPolku, this.roles) &&
            this.allowedButtons.reduce(
                (p, c) => p || (c.button === button && organisaatioNimiPolku.length > c.fromLevel),
                false
            )
        );
    };
    canEditLomake = (lomake: ConfigurableLomake, oid: string, organisaatioNimiPolku: OrganisaatioNimiJaOid[]) => {
        if (this.roles.includes(OPH_CRUD)) return true;
        return (
            organisationCrudAllowedInRoles(oid, organisaatioNimiPolku, this.roles) &&
            this.allowedLomakes.reduce(
                (p, c) => p || (c.lomake === lomake && organisaatioNimiPolku.length > c.fromLevel),
                false
            )
        );
    };
    canEditIfParent = (oid: string, organisaatioNimiPolku: OrganisaatioNimiJaOid[]) => {
        if (this.roles.includes(OPH_CRUD)) return true;
        if (organisaatioNimiPolku.length > 2 && organisationCrudAllowedInRoles(oid, organisaatioNimiPolku, this.roles))
            return true;
        return false;
    };
    getOrganisationOidsWithAnyAccess = () => {
        const oids = this.roles
            .filter(
                (r) => r.startsWith('APP_ORGANISAATIOHALLINTA_CRUD_') || r.startsWith('APP_ORGANISAATIOHALLINTA_READ_')
            )
            .map((r) => r.substring(r.lastIndexOf('_') + 1));
        return [...new Set(oids)];
    };
    isOphUser = () => {
        return this.roles.includes(OPH_CRUD);
    };
}
