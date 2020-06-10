package fi.vm.sade.organisaatio.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QEmail is a Querydsl query type for Email
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QEmail extends EntityPathBase<Email> {

    private static final long serialVersionUID = -1822625797L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QEmail email1 = new QEmail("email1");

    public final QYhteystieto _super;

    public final StringPath email = createString("email");

    //inherited
    public final NumberPath<Long> id;

    //inherited
    public final StringPath kieli;

    // inherited
    public final QOrganisaatio organisaatio;

    //inherited
    public final NumberPath<Long> version;

    //inherited
    public final StringPath yhteystietoOid;

    public QEmail(String variable) {
        this(Email.class, forVariable(variable), INITS);
    }

    public QEmail(Path<? extends Email> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QEmail(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QEmail(PathMetadata metadata, PathInits inits) {
        this(Email.class, metadata, inits);
    }

    public QEmail(Class<? extends Email> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this._super = new QYhteystieto(type, metadata, inits);
        this.id = _super.id;
        this.kieli = _super.kieli;
        this.organisaatio = _super.organisaatio;
        this.version = _super.version;
        this.yhteystietoOid = _super.yhteystietoOid;
    }

}

