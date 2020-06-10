package fi.vm.sade.organisaatio.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOrganisaatioSahkoposti is a Querydsl query type for OrganisaatioSahkoposti
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QOrganisaatioSahkoposti extends EntityPathBase<OrganisaatioSahkoposti> {

    private static final long serialVersionUID = 1377387787L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOrganisaatioSahkoposti organisaatioSahkoposti = new QOrganisaatioSahkoposti("organisaatioSahkoposti");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final DateTimePath<java.util.Date> aikaleima = createDateTime("aikaleima", java.util.Date.class);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final QOrganisaatio organisaatio;

    public final EnumPath<OrganisaatioSahkoposti.Tyyppi> tyyppi = createEnum("tyyppi", OrganisaatioSahkoposti.Tyyppi.class);

    //inherited
    public final NumberPath<Long> version = _super.version;

    public final StringPath viestintapalveluId = createString("viestintapalveluId");

    public QOrganisaatioSahkoposti(String variable) {
        this(OrganisaatioSahkoposti.class, forVariable(variable), INITS);
    }

    public QOrganisaatioSahkoposti(Path<? extends OrganisaatioSahkoposti> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOrganisaatioSahkoposti(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOrganisaatioSahkoposti(PathMetadata metadata, PathInits inits) {
        this(OrganisaatioSahkoposti.class, metadata, inits);
    }

    public QOrganisaatioSahkoposti(Class<? extends OrganisaatioSahkoposti> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.organisaatio = inits.isInitialized("organisaatio") ? new QOrganisaatio(forProperty("organisaatio"), inits.get("organisaatio")) : null;
    }

}

