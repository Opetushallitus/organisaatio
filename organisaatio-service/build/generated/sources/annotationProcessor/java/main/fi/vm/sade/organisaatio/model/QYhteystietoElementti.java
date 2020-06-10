package fi.vm.sade.organisaatio.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QYhteystietoElementti is a Querydsl query type for YhteystietoElementti
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QYhteystietoElementti extends EntityPathBase<YhteystietoElementti> {

    private static final long serialVersionUID = 1553445057L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QYhteystietoElementti yhteystietoElementti = new QYhteystietoElementti("yhteystietoElementti");

    public final QOrganisaatioBaseEntity _super = new QOrganisaatioBaseEntity(this);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final BooleanPath kaytossa = createBoolean("kaytossa");

    public final StringPath nimi = createString("nimi");

    public final StringPath nimiEn = createString("nimiEn");

    public final StringPath nimiSv = createString("nimiSv");

    public final StringPath oid = createString("oid");

    public final BooleanPath pakollinen = createBoolean("pakollinen");

    public final StringPath tyyppi = createString("tyyppi");

    //inherited
    public final NumberPath<Long> version = _super.version;

    public final QYhteystietojenTyyppi yhteystietojenTyyppi;

    public QYhteystietoElementti(String variable) {
        this(YhteystietoElementti.class, forVariable(variable), INITS);
    }

    public QYhteystietoElementti(Path<? extends YhteystietoElementti> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QYhteystietoElementti(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QYhteystietoElementti(PathMetadata metadata, PathInits inits) {
        this(YhteystietoElementti.class, metadata, inits);
    }

    public QYhteystietoElementti(Class<? extends YhteystietoElementti> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.yhteystietojenTyyppi = inits.isInitialized("yhteystietojenTyyppi") ? new QYhteystietojenTyyppi(forProperty("yhteystietojenTyyppi"), inits.get("yhteystietojenTyyppi")) : null;
    }

}

