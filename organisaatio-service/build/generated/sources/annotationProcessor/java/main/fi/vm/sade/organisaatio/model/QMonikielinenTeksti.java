package fi.vm.sade.organisaatio.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QMonikielinenTeksti is a Querydsl query type for MonikielinenTeksti
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QMonikielinenTeksti extends EntityPathBase<MonikielinenTeksti> {

    private static final long serialVersionUID = 2129967199L;

    public static final QMonikielinenTeksti monikielinenTeksti = new QMonikielinenTeksti("monikielinenTeksti");

    public final QBaseEntity _super = new QBaseEntity(this);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final MapPath<String, String, StringPath> values = this.<String, String, StringPath>createMap("values", String.class, String.class, StringPath.class);

    //inherited
    public final NumberPath<Long> version = _super.version;

    public QMonikielinenTeksti(String variable) {
        super(MonikielinenTeksti.class, forVariable(variable));
    }

    public QMonikielinenTeksti(Path<? extends MonikielinenTeksti> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMonikielinenTeksti(PathMetadata metadata) {
        super(MonikielinenTeksti.class, metadata);
    }

}

