package fi.vm.sade.organisaatio.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOrganisaatiotyyppiRajoite is a Querydsl query type for OrganisaatiotyyppiRajoite
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QOrganisaatiotyyppiRajoite extends EntityPathBase<OrganisaatiotyyppiRajoite> {

    private static final long serialVersionUID = -1432636879L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOrganisaatiotyyppiRajoite organisaatiotyyppiRajoite = new QOrganisaatiotyyppiRajoite("organisaatiotyyppiRajoite");

    public final QRajoite _super;

    //inherited
    public final StringPath arvo;

    //inherited
    public final NumberPath<Long> id;

    // inherited
    public final QLisatietotyyppi lisatietotyyppi;

    //inherited
    public final NumberPath<Long> version;

    public QOrganisaatiotyyppiRajoite(String variable) {
        this(OrganisaatiotyyppiRajoite.class, forVariable(variable), INITS);
    }

    public QOrganisaatiotyyppiRajoite(Path<? extends OrganisaatiotyyppiRajoite> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOrganisaatiotyyppiRajoite(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOrganisaatiotyyppiRajoite(PathMetadata metadata, PathInits inits) {
        this(OrganisaatiotyyppiRajoite.class, metadata, inits);
    }

    public QOrganisaatiotyyppiRajoite(Class<? extends OrganisaatiotyyppiRajoite> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this._super = new QRajoite(type, metadata, inits);
        this.arvo = _super.arvo;
        this.id = _super.id;
        this.lisatietotyyppi = _super.lisatietotyyppi;
        this.version = _super.version;
    }

}

