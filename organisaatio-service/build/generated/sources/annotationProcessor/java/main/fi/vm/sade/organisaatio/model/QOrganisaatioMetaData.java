package fi.vm.sade.organisaatio.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOrganisaatioMetaData is a Querydsl query type for OrganisaatioMetaData
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QOrganisaatioMetaData extends EntityPathBase<OrganisaatioMetaData> {

    private static final long serialVersionUID = -2094187377L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOrganisaatioMetaData organisaatioMetaData = new QOrganisaatioMetaData("organisaatioMetaData");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final QMonikielinenTeksti hakutoimistoEctsEmailmkt;

    public final QMonikielinenTeksti hakutoimistoEctsNimimkt;

    public final QMonikielinenTeksti hakutoimistoEctsPuhelinmkt;

    public final QMonikielinenTeksti hakutoimistoEctsTehtavanimikemkt;

    public final QMonikielinenTeksti hakutoimistoNimi;

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final StringPath koodi = createString("koodi");

    public final QBinaryData kuva;

    public final DateTimePath<java.util.Date> luontiPvm = createDateTime("luontiPvm", java.util.Date.class);

    public final DateTimePath<java.util.Date> muokkausPvm = createDateTime("muokkausPvm", java.util.Date.class);

    public final QMonikielinenTeksti nimi;

    public final QOrganisaatio organisation;

    public final SetPath<NamedMonikielinenTeksti, QNamedMonikielinenTeksti> values = this.<NamedMonikielinenTeksti, QNamedMonikielinenTeksti>createSet("values", NamedMonikielinenTeksti.class, QNamedMonikielinenTeksti.class, PathInits.DIRECT2);

    //inherited
    public final NumberPath<Long> version = _super.version;

    public final ListPath<Yhteystieto, QYhteystieto> yhteystiedot = this.<Yhteystieto, QYhteystieto>createList("yhteystiedot", Yhteystieto.class, QYhteystieto.class, PathInits.DIRECT2);

    public QOrganisaatioMetaData(String variable) {
        this(OrganisaatioMetaData.class, forVariable(variable), INITS);
    }

    public QOrganisaatioMetaData(Path<? extends OrganisaatioMetaData> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOrganisaatioMetaData(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOrganisaatioMetaData(PathMetadata metadata, PathInits inits) {
        this(OrganisaatioMetaData.class, metadata, inits);
    }

    public QOrganisaatioMetaData(Class<? extends OrganisaatioMetaData> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.hakutoimistoEctsEmailmkt = inits.isInitialized("hakutoimistoEctsEmailmkt") ? new QMonikielinenTeksti(forProperty("hakutoimistoEctsEmailmkt")) : null;
        this.hakutoimistoEctsNimimkt = inits.isInitialized("hakutoimistoEctsNimimkt") ? new QMonikielinenTeksti(forProperty("hakutoimistoEctsNimimkt")) : null;
        this.hakutoimistoEctsPuhelinmkt = inits.isInitialized("hakutoimistoEctsPuhelinmkt") ? new QMonikielinenTeksti(forProperty("hakutoimistoEctsPuhelinmkt")) : null;
        this.hakutoimistoEctsTehtavanimikemkt = inits.isInitialized("hakutoimistoEctsTehtavanimikemkt") ? new QMonikielinenTeksti(forProperty("hakutoimistoEctsTehtavanimikemkt")) : null;
        this.hakutoimistoNimi = inits.isInitialized("hakutoimistoNimi") ? new QMonikielinenTeksti(forProperty("hakutoimistoNimi")) : null;
        this.kuva = inits.isInitialized("kuva") ? new QBinaryData(forProperty("kuva"), inits.get("kuva")) : null;
        this.nimi = inits.isInitialized("nimi") ? new QMonikielinenTeksti(forProperty("nimi")) : null;
        this.organisation = inits.isInitialized("organisation") ? new QOrganisaatio(forProperty("organisation"), inits.get("organisation")) : null;
    }

}

