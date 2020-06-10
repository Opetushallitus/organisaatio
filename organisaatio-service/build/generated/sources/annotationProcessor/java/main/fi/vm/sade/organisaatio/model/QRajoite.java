package fi.vm.sade.organisaatio.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRajoite is a Querydsl query type for Rajoite
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QRajoite extends EntityPathBase<Rajoite> {

    private static final long serialVersionUID = -879146747L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRajoite rajoite = new QRajoite("rajoite");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final StringPath arvo = createString("arvo");

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final QLisatietotyyppi lisatietotyyppi;

    //inherited
    public final NumberPath<Long> version = _super.version;

    public QRajoite(String variable) {
        this(Rajoite.class, forVariable(variable), INITS);
    }

    public QRajoite(Path<? extends Rajoite> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRajoite(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRajoite(PathMetadata metadata, PathInits inits) {
        this(Rajoite.class, metadata, inits);
    }

    public QRajoite(Class<? extends Rajoite> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.lisatietotyyppi = inits.isInitialized("lisatietotyyppi") ? new QLisatietotyyppi(forProperty("lisatietotyyppi")) : null;
    }

}

