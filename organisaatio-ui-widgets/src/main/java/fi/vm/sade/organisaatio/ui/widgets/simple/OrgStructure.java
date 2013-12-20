package fi.vm.sade.organisaatio.ui.widgets.simple;

import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

public class OrgStructure {

    private String parentoid;
    private String oid;
    private String nameFi;
    private String nameSv;
    private String nameEn;
    private Boolean poistettu;
    private Date lakkautusPvm;
    private OrgStructure parent;
    private Set<OrgStructure> child = new TreeSet<OrgStructure>(new OrgStructureComparator());


    public Set<OrgStructure> getChild() {
        return child;
    }

    public OrgStructure getParent() {
        return parent;
    }

    public void setParent(OrgStructure parent) {
        this.parent = parent;
    }

    public String getNameFi() {
        return nameFi;
    }

    public void setNameFi(String nameFi) {
        this.nameFi = nameFi;
    }

    public String getNameSv() {
        return nameSv;
    }

    public void setNameSv(String nameSv) {
        this.nameSv = nameSv;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getParentoid() {
        return parentoid;
    }

    public void setParentOid(String parentoid) {
        this.parentoid = parentoid;
    }

    public void addChild(OrgStructure c) {
        this.child.add(c);
    }

    public Date getLakkautusPvm() {
        return lakkautusPvm;
    }

    public void setLakkautusPvm(Date lakkautusPvm) {
        this.lakkautusPvm = lakkautusPvm;
    }

    public Boolean getPoistettu() {
        return poistettu;
    }

    public void setPoistettu(Boolean poistettu) {
        this.poistettu = poistettu;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OrgStructure)) {
            return false;
        }

        OrgStructure that = (OrgStructure) o;

        if (!oid.equals(that.oid)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return oid.hashCode();
    }
}
