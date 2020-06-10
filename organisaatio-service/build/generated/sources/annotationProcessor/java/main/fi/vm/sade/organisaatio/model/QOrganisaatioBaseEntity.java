package fi.vm.sade.organisaatio.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QOrganisaatioBaseEntity is a Querydsl query type for OrganisaatioBaseEntity
 */
@Generated("com.querydsl.codegen.SupertypeSerializer")
public class QOrganisaatioBaseEntity extends EntityPathBase<OrganisaatioBaseEntity> {

    private static final long serialVersionUID = 180007764L;

    public static final QOrganisaatioBaseEntity organisaatioBaseEntity = new QOrganisaatioBaseEntity("organisaatioBaseEntity");

    public final QBaseEntity _super = new QBaseEntity(this);

    //inherited
    public final NumberPath<Long> id = _super.id;

    //inherited
    public final NumberPath<Long> version = _super.version;

    public QOrganisaatioBaseEntity(String variable) {
        super(OrganisaatioBaseEntity.class, forVariable(variable));
    }

    public QOrganisaatioBaseEntity(Path<? extends OrganisaatioBaseEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QOrganisaatioBaseEntity(PathMetadata metadata) {
        super(OrganisaatioBaseEntity.class, metadata);
    }

}

