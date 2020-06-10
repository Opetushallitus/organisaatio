package fi.vm.sade.organisaatio.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QYtjVirhe is a Querydsl query type for YtjVirhe
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QYtjVirhe extends EntityPathBase<YtjVirhe> {

    private static final long serialVersionUID = 1785603374L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QYtjVirhe ytjVirhe = new QYtjVirhe("ytjVirhe");

    public final QBaseEntity _super = new QBaseEntity(this);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final StringPath oid = createString("oid");

    public final StringPath orgNimi = createString("orgNimi");

    //inherited
    public final NumberPath<Long> version = _super.version;

    public final EnumPath<YtjVirhe.YTJVirheKohde> virhekohde = createEnum("virhekohde", YtjVirhe.YTJVirheKohde.class);

    public final StringPath virheviesti = createString("virheviesti");

    public final QYtjPaivitysLoki ytjPaivitysLoki;

    public QYtjVirhe(String variable) {
        this(YtjVirhe.class, forVariable(variable), INITS);
    }

    public QYtjVirhe(Path<? extends YtjVirhe> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QYtjVirhe(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QYtjVirhe(PathMetadata metadata, PathInits inits) {
        this(YtjVirhe.class, metadata, inits);
    }

    public QYtjVirhe(Class<? extends YtjVirhe> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.ytjPaivitysLoki = inits.isInitialized("ytjPaivitysLoki") ? new QYtjPaivitysLoki(forProperty("ytjPaivitysLoki")) : null;
    }

}

