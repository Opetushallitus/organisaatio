package fi.vm.sade.organisaatio.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOrganisaatioLisatietotyyppi is a Querydsl query type for OrganisaatioLisatietotyyppi
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QOrganisaatioLisatietotyyppi extends EntityPathBase<OrganisaatioLisatietotyyppi> {

    private static final long serialVersionUID = 1545638773L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOrganisaatioLisatietotyyppi organisaatioLisatietotyyppi = new QOrganisaatioLisatietotyyppi("organisaatioLisatietotyyppi");

    public final QBaseEntity _super = new QBaseEntity(this);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final QLisatietotyyppi lisatietotyyppi;

    public final QOrganisaatio organisaatio;

    //inherited
    public final NumberPath<Long> version = _super.version;

    public QOrganisaatioLisatietotyyppi(String variable) {
        this(OrganisaatioLisatietotyyppi.class, forVariable(variable), INITS);
    }

    public QOrganisaatioLisatietotyyppi(Path<? extends OrganisaatioLisatietotyyppi> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOrganisaatioLisatietotyyppi(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOrganisaatioLisatietotyyppi(PathMetadata metadata, PathInits inits) {
        this(OrganisaatioLisatietotyyppi.class, metadata, inits);
    }

    public QOrganisaatioLisatietotyyppi(Class<? extends OrganisaatioLisatietotyyppi> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.lisatietotyyppi = inits.isInitialized("lisatietotyyppi") ? new QLisatietotyyppi(forProperty("lisatietotyyppi")) : null;
        this.organisaatio = inits.isInitialized("organisaatio") ? new QOrganisaatio(forProperty("organisaatio"), inits.get("organisaatio")) : null;
    }

}

