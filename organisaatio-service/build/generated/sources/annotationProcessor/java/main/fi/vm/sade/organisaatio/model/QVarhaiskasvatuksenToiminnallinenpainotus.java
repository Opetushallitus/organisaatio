package fi.vm.sade.organisaatio.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QVarhaiskasvatuksenToiminnallinenpainotus is a Querydsl query type for VarhaiskasvatuksenToiminnallinenpainotus
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QVarhaiskasvatuksenToiminnallinenpainotus extends EntityPathBase<VarhaiskasvatuksenToiminnallinenpainotus> {

    private static final long serialVersionUID = 1348810959L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QVarhaiskasvatuksenToiminnallinenpainotus varhaiskasvatuksenToiminnallinenpainotus = new QVarhaiskasvatuksenToiminnallinenpainotus("varhaiskasvatuksenToiminnallinenpainotus");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final DateTimePath<java.util.Date> alkupvm = createDateTime("alkupvm", java.util.Date.class);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final DateTimePath<java.util.Date> loppupvm = createDateTime("loppupvm", java.util.Date.class);

    public final StringPath toiminnallinenpainotus = createString("toiminnallinenpainotus");

    public final QVarhaiskasvatuksenToimipaikkaTiedot varhaiskasvatuksenToimipaikkaTiedot;

    //inherited
    public final NumberPath<Long> version = _super.version;

    public QVarhaiskasvatuksenToiminnallinenpainotus(String variable) {
        this(VarhaiskasvatuksenToiminnallinenpainotus.class, forVariable(variable), INITS);
    }

    public QVarhaiskasvatuksenToiminnallinenpainotus(Path<? extends VarhaiskasvatuksenToiminnallinenpainotus> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QVarhaiskasvatuksenToiminnallinenpainotus(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QVarhaiskasvatuksenToiminnallinenpainotus(PathMetadata metadata, PathInits inits) {
        this(VarhaiskasvatuksenToiminnallinenpainotus.class, metadata, inits);
    }

    public QVarhaiskasvatuksenToiminnallinenpainotus(Class<? extends VarhaiskasvatuksenToiminnallinenpainotus> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.varhaiskasvatuksenToimipaikkaTiedot = inits.isInitialized("varhaiskasvatuksenToimipaikkaTiedot") ? new QVarhaiskasvatuksenToimipaikkaTiedot(forProperty("varhaiskasvatuksenToimipaikkaTiedot")) : null;
    }

}

