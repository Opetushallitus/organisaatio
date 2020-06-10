package fi.vm.sade.organisaatio.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QLisatietotyyppi is a Querydsl query type for Lisatietotyyppi
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QLisatietotyyppi extends EntityPathBase<Lisatietotyyppi> {

    private static final long serialVersionUID = 731923028L;

    public static final QLisatietotyyppi lisatietotyyppi = new QLisatietotyyppi("lisatietotyyppi");

    public final QBaseEntity _super = new QBaseEntity(this);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final StringPath nimi = createString("nimi");

    public final SetPath<OrganisaatioLisatietotyyppi, QOrganisaatioLisatietotyyppi> organisaatioLisatietotyyppis = this.<OrganisaatioLisatietotyyppi, QOrganisaatioLisatietotyyppi>createSet("organisaatioLisatietotyyppis", OrganisaatioLisatietotyyppi.class, QOrganisaatioLisatietotyyppi.class, PathInits.DIRECT2);

    public final SetPath<Rajoite, QRajoite> rajoitteet = this.<Rajoite, QRajoite>createSet("rajoitteet", Rajoite.class, QRajoite.class, PathInits.DIRECT2);

    //inherited
    public final NumberPath<Long> version = _super.version;

    public QLisatietotyyppi(String variable) {
        super(Lisatietotyyppi.class, forVariable(variable));
    }

    public QLisatietotyyppi(Path<? extends Lisatietotyyppi> path) {
        super(path.getType(), path.getMetadata());
    }

    public QLisatietotyyppi(PathMetadata metadata) {
        super(Lisatietotyyppi.class, metadata);
    }

}

