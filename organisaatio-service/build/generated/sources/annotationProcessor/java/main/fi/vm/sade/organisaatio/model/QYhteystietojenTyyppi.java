package fi.vm.sade.organisaatio.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QYhteystietojenTyyppi is a Querydsl query type for YhteystietojenTyyppi
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QYhteystietojenTyyppi extends EntityPathBase<YhteystietojenTyyppi> {

    private static final long serialVersionUID = -1511967176L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QYhteystietojenTyyppi yhteystietojenTyyppi = new QYhteystietojenTyyppi("yhteystietojenTyyppi");

    public final QOrganisaatioBaseEntity _super = new QOrganisaatioBaseEntity(this);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final SetPath<YhteystietoElementti, QYhteystietoElementti> lisatietos = this.<YhteystietoElementti, QYhteystietoElementti>createSet("lisatietos", YhteystietoElementti.class, QYhteystietoElementti.class, PathInits.DIRECT2);

    public final QMonikielinenTeksti nimi;

    public final StringPath oid = createString("oid");

    public final SetPath<String, StringPath> sovellettavatOppilaitostyyppis = this.<String, StringPath>createSet("sovellettavatOppilaitostyyppis", String.class, StringPath.class, PathInits.DIRECT2);

    public final SetPath<String, StringPath> sovellettavatOrganisaatioTyyppis = this.<String, StringPath>createSet("sovellettavatOrganisaatioTyyppis", String.class, StringPath.class, PathInits.DIRECT2);

    //inherited
    public final NumberPath<Long> version = _super.version;

    public QYhteystietojenTyyppi(String variable) {
        this(YhteystietojenTyyppi.class, forVariable(variable), INITS);
    }

    public QYhteystietojenTyyppi(Path<? extends YhteystietojenTyyppi> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QYhteystietojenTyyppi(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QYhteystietojenTyyppi(PathMetadata metadata, PathInits inits) {
        this(YhteystietojenTyyppi.class, metadata, inits);
    }

    public QYhteystietojenTyyppi(Class<? extends YhteystietojenTyyppi> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.nimi = inits.isInitialized("nimi") ? new QMonikielinenTeksti(forProperty("nimi")) : null;
    }

}

