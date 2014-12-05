package fi.vm.sade.organisaatio.dao.impl;

import fi.vm.sade.generic.dao.AbstractJpaDAOImpl;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.OrganisaatioSuhde;
import fi.vm.sade.organisaatio.model.QOrganisaatio;
import fi.vm.sade.organisaatio.model.QOrganisaatioSuhde;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;
import fi.vm.sade.organisaatio.dao.OrganisaatioSuhdeDAO;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author mlyly
 */
@Repository
public class OrganisaatioSuhdeDAOImpl extends AbstractJpaDAOImpl<OrganisaatioSuhde, Long> implements OrganisaatioSuhdeDAO {

    private static final Logger LOG = LoggerFactory.getLogger(OrganisaatioSuhdeDAOImpl.class);

    @Autowired(required = true)
    OrganisaatioDAOImpl organisaatioDAO;

    // USE CASES:
    //
    // TIME 1. New child, B for A
    // data:
    // A B 1 - *
    //
    // TIME 2. New child, C for A
    // data:
    // A B 1 -
    // A C 2 - *
    //
    // TIME 3. New child, E for D
    // data:
    // A B 1 -
    // A C 2 -
    // D E 3 - *
    //
    // TIME 4. Remove child E
    // data:
    // A B 1 -
    // A C 2 -
    // D E 3 4 *
    //
    // TIME 5. Move B to be child of C  (remove child B, add child B to C)
    // data:
    // A B 1 5 *
    // A C 2 -
    // D E 3 4
    // C B 5 - *
    //

    /**
     * @param childId
     * @param atTime  null == now
     * @return parent relation at given time - there can be only one.
     */
    @Override
    public OrganisaatioSuhde findParentTo(Long childId, Date atTime) {
        if (atTime == null) {
            atTime = new Date();
        }
        if (childId == null) {
            throw new IllegalArgumentException("childId cannot be null");
        }

        LOG.info("findParentTo({}, {})", childId, atTime);

        QOrganisaatioSuhde qSuhde = QOrganisaatioSuhde.organisaatioSuhde;
        QOrganisaatio qOrganisaatio = QOrganisaatio.organisaatio;
        BooleanExpression expression = qSuhde.alkuPvm.eq(atTime).or(qSuhde.alkuPvm.before(atTime)).and(qSuhde.child.id.eq(childId));
        List<OrganisaatioSuhde> suhteet = new JPAQuery(getEntityManager()).from(qSuhde)
                .join(qSuhde.parent, qOrganisaatio).fetch()
                .where(expression.and(qOrganisaatio.organisaatioPoistettu.isFalse()))
                .orderBy(qSuhde.alkuPvm.desc())
                .list(qSuhde);
        
        if (suhteet != null && !suhteet.isEmpty()) {
            return suhteet.get(0);
        }
        return null;
    }

    /**
     * @param parentId
     * @param atTime   null == now
     * @return list of child relations at given time
     */
    @Override
    public List<OrganisaatioSuhde> findChildrenTo(Long parentId, Date atTime) {
        if (parentId == null) {
            throw new IllegalArgumentException("parentId == null");
        }
        if (atTime == null) {
            atTime = new Date();
        }
        
        LOG.info("findChildrenTo({}, {})", parentId, atTime);
        
        QOrganisaatio qOrganisaatio = QOrganisaatio.organisaatio;
        QOrganisaatioSuhde qSuhde = QOrganisaatioSuhde.organisaatioSuhde;
        
        BooleanExpression expression = (qSuhde.alkuPvm.eq(atTime).or(qSuhde.alkuPvm.before(atTime))).and(qSuhde.parent.id.eq(parentId));
                
        
        List<OrganisaatioSuhde> suhteet = new JPAQuery(getEntityManager()).from(qSuhde)
                .join(qSuhde.child, qOrganisaatio).fetch()
                .where(expression.and(qOrganisaatio.organisaatioPoistettu.isFalse()))
                .orderBy(qSuhde.alkuPvm.desc())
                .list(qSuhde);
        
        List<Long> foundChildIds = new ArrayList<Long>();
        List<OrganisaatioSuhde> result = new ArrayList<OrganisaatioSuhde>();
        for (OrganisaatioSuhde curSuhde : suhteet) {
            Organisaatio parent = findParentTo(curSuhde.getChild().getId(), atTime).getParent();
            if (!(foundChildIds.contains(curSuhde.getChild().getId()))
                    && parent.getId() == parentId) {
                foundChildIds.add(curSuhde.getChild().getId());
                result.add(curSuhde);
            }
        }
        
        return result;
    }

    /**
     * If child has a "current" parent, this actually "moves" child under another parent.
     *
     * @param parentId
     * @param childId
     * @param startingFrom null == now
     * @return created relation
     */
    @Override
    public OrganisaatioSuhde addChild(Long parentId, Long childId, Date startingFrom, String opetuspisteenJarjNro) {
        LOG.info("addChild({}, {}, {})", new Object[]{parentId, childId, startingFrom});

        if (parentId == null || childId == null) {
            throw new IllegalArgumentException();
        }
        if (startingFrom == null) {
            startingFrom = new Date();
        }

        //
        // Create the new relation
        //
        Organisaatio parent = organisaatioDAO.read(parentId);
        Organisaatio child = organisaatioDAO.read(childId);

        OrganisaatioSuhde childRelation = new OrganisaatioSuhde();
        childRelation.setAlkuPvm(startingFrom);
        childRelation.setLoppuPvm(null);
        childRelation.setChild(child);
        childRelation.setParent(parent);
        childRelation.setOpetuspisteenJarjNro(opetuspisteenJarjNro);

        childRelation = insert(childRelation);

        logRelation("  Created new child relation: ", childRelation);

        return childRelation;
    }

    /**
     * Updates existing parent-child relation for give parent-child.
     * If parent is null ANY valid relation for child will be dated to be ended.
     *
     * @param parentId
     * @param childId
     * @param removalDate
     */
    @Override
    public void removeChild(Long parentId, Long childId, Date removalDate) {
        LOG.info("removeChild(pId={}, cId={}, t={})", new Object[]{parentId, childId, removalDate});

        if (removalDate == null) {
            removalDate = new Date();
        }

        // Get possible existing parent relation
        OrganisaatioSuhde parentRelation = findParentTo(childId, removalDate);

        if (parentRelation != null) {
            this.remove(parentRelation);
        }
    }

    @Override
    public List<OrganisaatioSuhde> findForDay(Date day) {
        if (day == null) {
            return new ArrayList<>();
        }

        Calendar from = Calendar.getInstance();
        from.setTime(day);
        Calendar to = Calendar.getInstance();
        to.setTime(day);

        zeroTime(from);
        zeroTime(to);

        to.add(Calendar.DAY_OF_MONTH, 1);
        to.add(Calendar.SECOND, -1);

        QOrganisaatioSuhde organisaatioSuhde = QOrganisaatioSuhde.organisaatioSuhde;
        JPAQuery query = new JPAQuery(getEntityManager())
                .from(organisaatioSuhde)
                .where(organisaatioSuhde.alkuPvm.between(from.getTime(), to.getTime()))
                .orderBy(organisaatioSuhde.alkuPvm.asc());
        return query.list(organisaatioSuhde);
    }

    private void zeroTime(Calendar from) {
        from.set(Calendar.HOUR_OF_DAY, 0);
        from.set(Calendar.MINUTE, 0);
        from.set(Calendar.SECOND, 0);
        from.set(Calendar.MILLISECOND, 0);
    }

    private void logRelation(String message, OrganisaatioSuhde relation) {
        if (relation == null) {
            LOG.info("  {} - NULL", message);
        } else {
            LOG.info("  {} --> pId={}, cId={}, aPvm={}, lPvm={}",
                    new Object[]{message, relation.getParent().getId(), relation.getChild().getId(),
                            relation.getAlkuPvm(), relation.getLoppuPvm()});
        }
    }
    


}
