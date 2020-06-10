package fi.vm.sade.organisaatio.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBinaryData is a Querydsl query type for BinaryData
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QBinaryData extends EntityPathBase<BinaryData> {

    private static final long serialVersionUID = 721864268L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBinaryData binaryData = new QBinaryData("binaryData");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final ArrayPath<byte[], Byte> data = createArray("data", byte[].class);

    public final StringPath filename = createString("filename");

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final StringPath mimeType = createString("mimeType");

    public final QMonikielinenTeksti name;

    //inherited
    public final NumberPath<Long> version = _super.version;

    public QBinaryData(String variable) {
        this(BinaryData.class, forVariable(variable), INITS);
    }

    public QBinaryData(Path<? extends BinaryData> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBinaryData(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBinaryData(PathMetadata metadata, PathInits inits) {
        this(BinaryData.class, metadata, inits);
    }

    public QBinaryData(Class<? extends BinaryData> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.name = inits.isInitialized("name") ? new QMonikielinenTeksti(forProperty("name")) : null;
    }

}

