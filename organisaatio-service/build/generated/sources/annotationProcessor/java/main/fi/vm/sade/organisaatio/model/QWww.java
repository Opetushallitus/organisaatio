package fi.vm.sade.organisaatio.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QWww is a Querydsl query type for Www
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QWww extends EntityPathBase<Www> {

    private static final long serialVersionUID = -1905787466L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QWww www = new QWww("www");

    public final QYhteystieto _super;

    //inherited
    public final NumberPath<Long> id;

    //inherited
    public final StringPath kieli;

    // inherited
    public final QOrganisaatio organisaatio;

    //inherited
    public final NumberPath<Long> version;

    public final StringPath wwwOsoite = createString("wwwOsoite");

    //inherited
    public final StringPath yhteystietoOid;

    public QWww(String variable) {
        this(Www.class, forVariable(variable), INITS);
    }

    public QWww(Path<? extends Www> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QWww(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QWww(PathMetadata metadata, PathInits inits) {
        this(Www.class, metadata, inits);
    }

    public QWww(Class<? extends Www> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this._super = new QYhteystieto(type, metadata, inits);
        this.id = _super.id;
        this.kieli = _super.kieli;
        this.organisaatio = _super.organisaatio;
        this.version = _super.version;
        this.yhteystietoOid = _super.yhteystietoOid;
    }

}

