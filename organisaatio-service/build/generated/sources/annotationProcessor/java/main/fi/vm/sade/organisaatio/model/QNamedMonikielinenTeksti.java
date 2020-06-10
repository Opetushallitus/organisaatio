package fi.vm.sade.organisaatio.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QNamedMonikielinenTeksti is a Querydsl query type for NamedMonikielinenTeksti
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QNamedMonikielinenTeksti extends EntityPathBase<NamedMonikielinenTeksti> {

    private static final long serialVersionUID = -1374516202L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QNamedMonikielinenTeksti namedMonikielinenTeksti = new QNamedMonikielinenTeksti("namedMonikielinenTeksti");

    public final QBaseEntity _super = new QBaseEntity(this);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final StringPath key = createString("key");

    public final QMonikielinenTeksti name;

    public final QMonikielinenTeksti value;

    //inherited
    public final NumberPath<Long> version = _super.version;

    public QNamedMonikielinenTeksti(String variable) {
        this(NamedMonikielinenTeksti.class, forVariable(variable), INITS);
    }

    public QNamedMonikielinenTeksti(Path<? extends NamedMonikielinenTeksti> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QNamedMonikielinenTeksti(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QNamedMonikielinenTeksti(PathMetadata metadata, PathInits inits) {
        this(NamedMonikielinenTeksti.class, metadata, inits);
    }

    public QNamedMonikielinenTeksti(Class<? extends NamedMonikielinenTeksti> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.name = inits.isInitialized("name") ? new QMonikielinenTeksti(forProperty("name")) : null;
        this.value = inits.isInitialized("value") ? new QMonikielinenTeksti(forProperty("value")) : null;
    }

}

