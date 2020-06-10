package fi.vm.sade.organisaatio.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QVarhaiskasvatuksenToimipaikkaTiedot is a Querydsl query type for VarhaiskasvatuksenToimipaikkaTiedot
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QVarhaiskasvatuksenToimipaikkaTiedot extends EntityPathBase<VarhaiskasvatuksenToimipaikkaTiedot> {

    private static final long serialVersionUID = -1696046501L;

    public static final QVarhaiskasvatuksenToimipaikkaTiedot varhaiskasvatuksenToimipaikkaTiedot = new QVarhaiskasvatuksenToimipaikkaTiedot("varhaiskasvatuksenToimipaikkaTiedot");

    public final QBaseEntity _super = new QBaseEntity(this);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final StringPath kasvatusopillinenJarjestelma = createString("kasvatusopillinenJarjestelma");

    public final NumberPath<Long> paikkojenLukumaara = createNumber("paikkojenLukumaara", Long.class);

    public final StringPath toimintamuoto = createString("toimintamuoto");

    public final SetPath<String, StringPath> varhaiskasvatuksenJarjestamismuodot = this.<String, StringPath>createSet("varhaiskasvatuksenJarjestamismuodot", String.class, StringPath.class, PathInits.DIRECT2);

    public final SetPath<VarhaiskasvatuksenKielipainotus, QVarhaiskasvatuksenKielipainotus> varhaiskasvatuksenKielipainotukset = this.<VarhaiskasvatuksenKielipainotus, QVarhaiskasvatuksenKielipainotus>createSet("varhaiskasvatuksenKielipainotukset", VarhaiskasvatuksenKielipainotus.class, QVarhaiskasvatuksenKielipainotus.class, PathInits.DIRECT2);

    public final SetPath<VarhaiskasvatuksenToiminnallinenpainotus, QVarhaiskasvatuksenToiminnallinenpainotus> varhaiskasvatuksenToiminnallinenpainotukset = this.<VarhaiskasvatuksenToiminnallinenpainotus, QVarhaiskasvatuksenToiminnallinenpainotus>createSet("varhaiskasvatuksenToiminnallinenpainotukset", VarhaiskasvatuksenToiminnallinenpainotus.class, QVarhaiskasvatuksenToiminnallinenpainotus.class, PathInits.DIRECT2);

    //inherited
    public final NumberPath<Long> version = _super.version;

    public QVarhaiskasvatuksenToimipaikkaTiedot(String variable) {
        super(VarhaiskasvatuksenToimipaikkaTiedot.class, forVariable(variable));
    }

    public QVarhaiskasvatuksenToimipaikkaTiedot(Path<? extends VarhaiskasvatuksenToimipaikkaTiedot> path) {
        super(path.getType(), path.getMetadata());
    }

    public QVarhaiskasvatuksenToimipaikkaTiedot(PathMetadata metadata) {
        super(VarhaiskasvatuksenToimipaikkaTiedot.class, metadata);
    }

}

