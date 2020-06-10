package fi.vm.sade.organisaatio.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QYhteystietoArvo is a Querydsl query type for YhteystietoArvo
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QYhteystietoArvo extends EntityPathBase<YhteystietoArvo> {

    private static final long serialVersionUID = -2110304678L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QYhteystietoArvo yhteystietoArvo = new QYhteystietoArvo("yhteystietoArvo");

    public final QOrganisaatioBaseEntity _super = new QOrganisaatioBaseEntity(this);

    public final StringPath arvoText = createString("arvoText");

    public final QYhteystieto arvoYhteystieto;

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final QYhteystietoElementti kentta;

    public final StringPath kieli = createString("kieli");

    public final QOrganisaatio organisaatio;

    //inherited
    public final NumberPath<Long> version = _super.version;

    public final StringPath yhteystietoArvoOid = createString("yhteystietoArvoOid");

    public QYhteystietoArvo(String variable) {
        this(YhteystietoArvo.class, forVariable(variable), INITS);
    }

    public QYhteystietoArvo(Path<? extends YhteystietoArvo> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QYhteystietoArvo(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QYhteystietoArvo(PathMetadata metadata, PathInits inits) {
        this(YhteystietoArvo.class, metadata, inits);
    }

    public QYhteystietoArvo(Class<? extends YhteystietoArvo> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.arvoYhteystieto = inits.isInitialized("arvoYhteystieto") ? new QYhteystieto(forProperty("arvoYhteystieto"), inits.get("arvoYhteystieto")) : null;
        this.kentta = inits.isInitialized("kentta") ? new QYhteystietoElementti(forProperty("kentta"), inits.get("kentta")) : null;
        this.organisaatio = inits.isInitialized("organisaatio") ? new QOrganisaatio(forProperty("organisaatio"), inits.get("organisaatio")) : null;
    }

}

