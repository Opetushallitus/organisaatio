package fi.vm.sade.organisaatio.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOrganisaatioSuhde is a Querydsl query type for OrganisaatioSuhde
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QOrganisaatioSuhde extends EntityPathBase<OrganisaatioSuhde> {

    private static final long serialVersionUID = 1570900999L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOrganisaatioSuhde organisaatioSuhde = new QOrganisaatioSuhde("organisaatioSuhde");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final DatePath<java.util.Date> alkuPvm = createDate("alkuPvm", java.util.Date.class);

    public final QOrganisaatio child;

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final DatePath<java.util.Date> loppuPvm = createDate("loppuPvm", java.util.Date.class);

    public final StringPath opetuspisteenJarjNro = createString("opetuspisteenJarjNro");

    public final QOrganisaatio parent;

    public final EnumPath<OrganisaatioSuhde.OrganisaatioSuhdeTyyppi> suhdeTyyppi = createEnum("suhdeTyyppi", OrganisaatioSuhde.OrganisaatioSuhdeTyyppi.class);

    //inherited
    public final NumberPath<Long> version = _super.version;

    public QOrganisaatioSuhde(String variable) {
        this(OrganisaatioSuhde.class, forVariable(variable), INITS);
    }

    public QOrganisaatioSuhde(Path<? extends OrganisaatioSuhde> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOrganisaatioSuhde(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOrganisaatioSuhde(PathMetadata metadata, PathInits inits) {
        this(OrganisaatioSuhde.class, metadata, inits);
    }

    public QOrganisaatioSuhde(Class<? extends OrganisaatioSuhde> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.child = inits.isInitialized("child") ? new QOrganisaatio(forProperty("child"), inits.get("child")) : null;
        this.parent = inits.isInitialized("parent") ? new QOrganisaatio(forProperty("parent"), inits.get("parent")) : null;
    }

}

