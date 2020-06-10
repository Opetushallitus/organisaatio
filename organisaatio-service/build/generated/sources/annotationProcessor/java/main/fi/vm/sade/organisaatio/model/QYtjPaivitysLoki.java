package fi.vm.sade.organisaatio.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QYtjPaivitysLoki is a Querydsl query type for YtjPaivitysLoki
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QYtjPaivitysLoki extends EntityPathBase<YtjPaivitysLoki> {

    private static final long serialVersionUID = -261577198L;

    public static final QYtjPaivitysLoki ytjPaivitysLoki = new QYtjPaivitysLoki("ytjPaivitysLoki");

    public final QBaseEntity _super = new QBaseEntity(this);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final NumberPath<Long> paivitetytLkm = createNumber("paivitetytLkm", Long.class);

    public final DateTimePath<java.util.Date> paivitysaika = createDateTime("paivitysaika", java.util.Date.class);

    public final EnumPath<YtjPaivitysLoki.YTJPaivitysStatus> paivitysTila = createEnum("paivitysTila", YtjPaivitysLoki.YTJPaivitysStatus.class);

    public final StringPath paivitysTilaSelite = createString("paivitysTilaSelite");

    //inherited
    public final NumberPath<Long> version = _super.version;

    public final ListPath<YtjVirhe, QYtjVirhe> ytjVirheet = this.<YtjVirhe, QYtjVirhe>createList("ytjVirheet", YtjVirhe.class, QYtjVirhe.class, PathInits.DIRECT2);

    public QYtjPaivitysLoki(String variable) {
        super(YtjPaivitysLoki.class, forVariable(variable));
    }

    public QYtjPaivitysLoki(Path<? extends YtjPaivitysLoki> path) {
        super(path.getType(), path.getMetadata());
    }

    public QYtjPaivitysLoki(PathMetadata metadata) {
        super(YtjPaivitysLoki.class, metadata);
    }

}

