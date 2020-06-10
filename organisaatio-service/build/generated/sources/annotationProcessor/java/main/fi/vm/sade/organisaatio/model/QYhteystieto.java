package fi.vm.sade.organisaatio.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QYhteystieto is a Querydsl query type for Yhteystieto
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QYhteystieto extends EntityPathBase<Yhteystieto> {

    private static final long serialVersionUID = 636112784L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QYhteystieto yhteystieto = new QYhteystieto("yhteystieto");

    public final QOrganisaatioBaseEntity _super = new QOrganisaatioBaseEntity(this);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final StringPath kieli = createString("kieli");

    public final QOrganisaatio organisaatio;

    //inherited
    public final NumberPath<Long> version = _super.version;

    public final StringPath yhteystietoOid = createString("yhteystietoOid");

    public QYhteystieto(String variable) {
        this(Yhteystieto.class, forVariable(variable), INITS);
    }

    public QYhteystieto(Path<? extends Yhteystieto> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QYhteystieto(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QYhteystieto(PathMetadata metadata, PathInits inits) {
        this(Yhteystieto.class, metadata, inits);
    }

    public QYhteystieto(Class<? extends Yhteystieto> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.organisaatio = inits.isInitialized("organisaatio") ? new QOrganisaatio(forProperty("organisaatio"), inits.get("organisaatio")) : null;
    }

}

