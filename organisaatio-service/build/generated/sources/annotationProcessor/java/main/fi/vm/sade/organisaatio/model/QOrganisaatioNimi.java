package fi.vm.sade.organisaatio.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOrganisaatioNimi is a Querydsl query type for OrganisaatioNimi
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QOrganisaatioNimi extends EntityPathBase<OrganisaatioNimi> {

    private static final long serialVersionUID = -503675433L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOrganisaatioNimi organisaatioNimi = new QOrganisaatioNimi("organisaatioNimi");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final DatePath<java.util.Date> alkuPvm = createDate("alkuPvm", java.util.Date.class);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final QMonikielinenTeksti nimi;

    public final QOrganisaatio organisaatio;

    public final StringPath paivittaja = createString("paivittaja");

    //inherited
    public final NumberPath<Long> version = _super.version;

    public QOrganisaatioNimi(String variable) {
        this(OrganisaatioNimi.class, forVariable(variable), INITS);
    }

    public QOrganisaatioNimi(Path<? extends OrganisaatioNimi> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOrganisaatioNimi(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOrganisaatioNimi(PathMetadata metadata, PathInits inits) {
        this(OrganisaatioNimi.class, metadata, inits);
    }

    public QOrganisaatioNimi(Class<? extends OrganisaatioNimi> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.nimi = inits.isInitialized("nimi") ? new QMonikielinenTeksti(forProperty("nimi")) : null;
        this.organisaatio = inits.isInitialized("organisaatio") ? new QOrganisaatio(forProperty("organisaatio"), inits.get("organisaatio")) : null;
    }

}

