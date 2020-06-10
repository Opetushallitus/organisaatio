package fi.vm.sade.organisaatio.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPuhelinnumero is a Querydsl query type for Puhelinnumero
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QPuhelinnumero extends EntityPathBase<Puhelinnumero> {

    private static final long serialVersionUID = -1862132790L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPuhelinnumero puhelinnumero1 = new QPuhelinnumero("puhelinnumero1");

    public final QYhteystieto _super;

    //inherited
    public final NumberPath<Long> id;

    //inherited
    public final StringPath kieli;

    // inherited
    public final QOrganisaatio organisaatio;

    public final StringPath puhelinnumero = createString("puhelinnumero");

    public final StringPath tyyppi = createString("tyyppi");

    //inherited
    public final NumberPath<Long> version;

    //inherited
    public final StringPath yhteystietoOid;

    public QPuhelinnumero(String variable) {
        this(Puhelinnumero.class, forVariable(variable), INITS);
    }

    public QPuhelinnumero(Path<? extends Puhelinnumero> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPuhelinnumero(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPuhelinnumero(PathMetadata metadata, PathInits inits) {
        this(Puhelinnumero.class, metadata, inits);
    }

    public QPuhelinnumero(Class<? extends Puhelinnumero> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this._super = new QYhteystieto(type, metadata, inits);
        this.id = _super.id;
        this.kieli = _super.kieli;
        this.organisaatio = _super.organisaatio;
        this.version = _super.version;
        this.yhteystietoOid = _super.yhteystietoOid;
    }

}

