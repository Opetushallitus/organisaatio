package fi.vm.sade.organisaatio.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOrganisaatio is a Querydsl query type for Organisaatio
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QOrganisaatio extends EntityPathBase<Organisaatio> {

    private static final long serialVersionUID = -658568928L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOrganisaatio organisaatio = new QOrganisaatio("organisaatio");

    public final QOrganisaatioBaseEntity _super = new QOrganisaatioBaseEntity(this);

    public final DatePath<java.util.Date> alkuPvm = createDate("alkuPvm", java.util.Date.class);

    public final SetPath<OrganisaatioSuhde, QOrganisaatioSuhde> childSuhteet = this.<OrganisaatioSuhde, QOrganisaatioSuhde>createSet("childSuhteet", OrganisaatioSuhde.class, QOrganisaatioSuhde.class, PathInits.DIRECT2);

    public final StringPath domainNimi = createString("domainNimi");

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final SetPath<String, StringPath> kayttoryhmat = this.<String, StringPath>createSet("kayttoryhmat", String.class, StringPath.class, PathInits.DIRECT2);

    public final SetPath<String, StringPath> kielet = this.<String, StringPath>createSet("kielet", String.class, StringPath.class, PathInits.DIRECT2);

    public final StringPath kotipaikka = createString("kotipaikka");

    public final QMonikielinenTeksti kuvaus2;

    public final DatePath<java.util.Date> lakkautusPvm = createDate("lakkautusPvm", java.util.Date.class);

    public final StringPath maa = createString("maa");

    public final QOrganisaatioMetaData metadata;

    public final SetPath<String, StringPath> muutKotipaikatUris = this.<String, StringPath>createSet("muutKotipaikatUris", String.class, StringPath.class, PathInits.DIRECT2);

    public final SetPath<String, StringPath> muutOppilaitosTyyppiUris = this.<String, StringPath>createSet("muutOppilaitosTyyppiUris", String.class, StringPath.class, PathInits.DIRECT2);

    public final ListPath<OrganisaatioNimi, QOrganisaatioNimi> nimet = this.<OrganisaatioNimi, QOrganisaatioNimi>createList("nimet", OrganisaatioNimi.class, QOrganisaatioNimi.class, PathInits.DIRECT2);

    public final QMonikielinenTeksti nimi;

    public final StringPath nimihaku = createString("nimihaku");

    public final StringPath oid = createString("oid");

    public final StringPath opetuspisteenJarjNro = createString("opetuspisteenJarjNro");

    public final StringPath oppilaitosKoodi = createString("oppilaitosKoodi");

    public final StringPath oppilaitosTyyppi = createString("oppilaitosTyyppi");

    public final SetPath<OrganisaatioLisatietotyyppi, QOrganisaatioLisatietotyyppi> organisaatioLisatietotyypit = this.<OrganisaatioLisatietotyyppi, QOrganisaatioLisatietotyyppi>createSet("organisaatioLisatietotyypit", OrganisaatioLisatietotyyppi.class, QOrganisaatioLisatietotyyppi.class, PathInits.DIRECT2);

    public final BooleanPath organisaatioPoistettu = createBoolean("organisaatioPoistettu");

    public final StringPath paivittaja = createString("paivittaja");

    public final DateTimePath<java.util.Date> paivitysPvm = createDateTime("paivitysPvm", java.util.Date.class);

    public final StringPath parentIdPath = createString("parentIdPath");

    public final StringPath parentOidPath = createString("parentOidPath");

    public final ListPath<OrganisaatioSuhde, QOrganisaatioSuhde> parentSuhteet = this.<OrganisaatioSuhde, QOrganisaatioSuhde>createList("parentSuhteet", OrganisaatioSuhde.class, QOrganisaatioSuhde.class, PathInits.DIRECT2);

    public final BooleanPath piilotettu = createBoolean("piilotettu");

    public final SetPath<String, StringPath> ryhmatyypit = this.<String, StringPath>createSet("ryhmatyypit", String.class, StringPath.class, PathInits.DIRECT2);

    public final DateTimePath<java.util.Date> tarkastusPvm = createDateTime("tarkastusPvm", java.util.Date.class);

    public final StringPath toimipisteKoodi = createString("toimipisteKoodi");

    public final DateTimePath<java.util.Date> tuontiPvm = createDateTime("tuontiPvm", java.util.Date.class);

    public final SetPath<String, StringPath> tyypit = this.<String, StringPath>createSet("tyypit", String.class, StringPath.class, PathInits.DIRECT2);

    public final QVarhaiskasvatuksenToimipaikkaTiedot varhaiskasvatuksenToimipaikkaTiedot;

    //inherited
    public final NumberPath<Long> version = _super.version;

    public final StringPath virastoTunnus = createString("virastoTunnus");

    public final SetPath<String, StringPath> vuosiluokat = this.<String, StringPath>createSet("vuosiluokat", String.class, StringPath.class, PathInits.DIRECT2);

    public final StringPath yhteishaunKoulukoodi = createString("yhteishaunKoulukoodi");

    public final SetPath<Yhteystieto, QYhteystieto> yhteystiedot = this.<Yhteystieto, QYhteystieto>createSet("yhteystiedot", Yhteystieto.class, QYhteystieto.class, PathInits.DIRECT2);

    public final SetPath<YhteystietoArvo, QYhteystietoArvo> yhteystietoArvos = this.<YhteystietoArvo, QYhteystietoArvo>createSet("yhteystietoArvos", YhteystietoArvo.class, QYhteystietoArvo.class, PathInits.DIRECT2);

    public final StringPath yritysmuoto = createString("yritysmuoto");

    public final StringPath ytjKieli = createString("ytjKieli");

    public final DatePath<java.util.Date> ytjPaivitysPvm = createDate("ytjPaivitysPvm", java.util.Date.class);

    public final StringPath ytunnus = createString("ytunnus");

    public QOrganisaatio(String variable) {
        this(Organisaatio.class, forVariable(variable), INITS);
    }

    public QOrganisaatio(Path<? extends Organisaatio> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOrganisaatio(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOrganisaatio(PathMetadata metadata, PathInits inits) {
        this(Organisaatio.class, metadata, inits);
    }

    public QOrganisaatio(Class<? extends Organisaatio> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.kuvaus2 = inits.isInitialized("kuvaus2") ? new QMonikielinenTeksti(forProperty("kuvaus2")) : null;
        this.metadata = inits.isInitialized("metadata") ? new QOrganisaatioMetaData(forProperty("metadata"), inits.get("metadata")) : null;
        this.nimi = inits.isInitialized("nimi") ? new QMonikielinenTeksti(forProperty("nimi")) : null;
        this.varhaiskasvatuksenToimipaikkaTiedot = inits.isInitialized("varhaiskasvatuksenToimipaikkaTiedot") ? new QVarhaiskasvatuksenToimipaikkaTiedot(forProperty("varhaiskasvatuksenToimipaikkaTiedot")) : null;
    }

}

