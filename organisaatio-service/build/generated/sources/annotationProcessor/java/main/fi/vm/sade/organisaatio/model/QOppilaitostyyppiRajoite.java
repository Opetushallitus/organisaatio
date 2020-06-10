package fi.vm.sade.organisaatio.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOppilaitostyyppiRajoite is a Querydsl query type for OppilaitostyyppiRajoite
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QOppilaitostyyppiRajoite extends EntityPathBase<OppilaitostyyppiRajoite> {

    private static final long serialVersionUID = 589260626L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOppilaitostyyppiRajoite oppilaitostyyppiRajoite = new QOppilaitostyyppiRajoite("oppilaitostyyppiRajoite");

    public final QRajoite _super;

    //inherited
    public final StringPath arvo;

    //inherited
    public final NumberPath<Long> id;

    // inherited
    public final QLisatietotyyppi lisatietotyyppi;

    //inherited
    public final NumberPath<Long> version;

    public QOppilaitostyyppiRajoite(String variable) {
        this(OppilaitostyyppiRajoite.class, forVariable(variable), INITS);
    }

    public QOppilaitostyyppiRajoite(Path<? extends OppilaitostyyppiRajoite> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOppilaitostyyppiRajoite(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOppilaitostyyppiRajoite(PathMetadata metadata, PathInits inits) {
        this(OppilaitostyyppiRajoite.class, metadata, inits);
    }

    public QOppilaitostyyppiRajoite(Class<? extends OppilaitostyyppiRajoite> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this._super = new QRajoite(type, metadata, inits);
        this.arvo = _super.arvo;
        this.id = _super.id;
        this.lisatietotyyppi = _super.lisatietotyyppi;
        this.version = _super.version;
    }

}

