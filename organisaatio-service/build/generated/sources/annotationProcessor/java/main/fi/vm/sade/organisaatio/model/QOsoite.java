package fi.vm.sade.organisaatio.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOsoite is a Querydsl query type for Osoite
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QOsoite extends EntityPathBase<Osoite> {

    private static final long serialVersionUID = -374574800L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOsoite osoite1 = new QOsoite("osoite1");

    public final QYhteystieto _super;

    public final StringPath coordinateType = createString("coordinateType");

    public final StringPath extraRivi = createString("extraRivi");

    //inherited
    public final NumberPath<Long> id;

    //inherited
    public final StringPath kieli;

    public final NumberPath<Double> lat = createNumber("lat", Double.class);

    public final NumberPath<Double> lng = createNumber("lng", Double.class);

    public final StringPath maa = createString("maa");

    // inherited
    public final QOrganisaatio organisaatio;

    public final StringPath osavaltio = createString("osavaltio");

    public final StringPath osoite = createString("osoite");

    public final StringPath osoiteTyyppi = createString("osoiteTyyppi");

    public final StringPath postinumero = createString("postinumero");

    public final StringPath postitoimipaikka = createString("postitoimipaikka");

    //inherited
    public final NumberPath<Long> version;

    //inherited
    public final StringPath yhteystietoOid;

    public final DatePath<java.util.Date> ytjPaivitysPvm = createDate("ytjPaivitysPvm", java.util.Date.class);

    public QOsoite(String variable) {
        this(Osoite.class, forVariable(variable), INITS);
    }

    public QOsoite(Path<? extends Osoite> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOsoite(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOsoite(PathMetadata metadata, PathInits inits) {
        this(Osoite.class, metadata, inits);
    }

    public QOsoite(Class<? extends Osoite> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this._super = new QYhteystieto(type, metadata, inits);
        this.id = _super.id;
        this.kieli = _super.kieli;
        this.organisaatio = _super.organisaatio;
        this.version = _super.version;
        this.yhteystietoOid = _super.yhteystietoOid;
    }

}

