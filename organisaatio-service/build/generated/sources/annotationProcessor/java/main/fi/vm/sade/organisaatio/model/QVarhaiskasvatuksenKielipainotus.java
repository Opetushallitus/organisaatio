package fi.vm.sade.organisaatio.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QVarhaiskasvatuksenKielipainotus is a Querydsl query type for VarhaiskasvatuksenKielipainotus
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QVarhaiskasvatuksenKielipainotus extends EntityPathBase<VarhaiskasvatuksenKielipainotus> {

    private static final long serialVersionUID = 1929601932L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QVarhaiskasvatuksenKielipainotus varhaiskasvatuksenKielipainotus = new QVarhaiskasvatuksenKielipainotus("varhaiskasvatuksenKielipainotus");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final DateTimePath<java.util.Date> alkupvm = createDateTime("alkupvm", java.util.Date.class);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final StringPath kielipainotus = createString("kielipainotus");

    public final DateTimePath<java.util.Date> loppupvm = createDateTime("loppupvm", java.util.Date.class);

    public final QVarhaiskasvatuksenToimipaikkaTiedot varhaiskasvatuksenToimipaikkaTiedot;

    //inherited
    public final NumberPath<Long> version = _super.version;

    public QVarhaiskasvatuksenKielipainotus(String variable) {
        this(VarhaiskasvatuksenKielipainotus.class, forVariable(variable), INITS);
    }

    public QVarhaiskasvatuksenKielipainotus(Path<? extends VarhaiskasvatuksenKielipainotus> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QVarhaiskasvatuksenKielipainotus(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QVarhaiskasvatuksenKielipainotus(PathMetadata metadata, PathInits inits) {
        this(VarhaiskasvatuksenKielipainotus.class, metadata, inits);
    }

    public QVarhaiskasvatuksenKielipainotus(Class<? extends VarhaiskasvatuksenKielipainotus> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.varhaiskasvatuksenToimipaikkaTiedot = inits.isInitialized("varhaiskasvatuksenToimipaikkaTiedot") ? new QVarhaiskasvatuksenToimipaikkaTiedot(forProperty("varhaiskasvatuksenToimipaikkaTiedot")) : null;
    }

}

