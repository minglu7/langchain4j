package dev.langchain4j.store.embedding.pgvector;

import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JSONBMetadataHandlerTest {

    @Test
    void createSimpleMetadataIndexes() throws SQLException {
        Statement statement = mock(Statement.class);
        List<String> sqlStatementQueries = new ArrayList<>();
        when(statement.executeUpdate(anyString()))
                .thenAnswer(AdditionalAnswers.answerVoid(q -> sqlStatementQueries.add((String)q)));

        MetadataStorageConfig metadataStorageConfig = DefaultMetadataStorageConfig.builder()
                .storageMode(MetadataStorageMode.COMBINED_JSONB)
                .columnDefinitions(Collections.singletonList("metadata JSONB"))
                .indexes(Collections.singletonList("metadata"))
                .indexType("GIN")
                .build();
        JSONBMetadataHandler jsonbMetadataHandler = new JSONBMetadataHandler(metadataStorageConfig);
        jsonbMetadataHandler.createMetadataIndexes(statement, "embeddings");

        assertThat(sqlStatementQueries).hasSize(1);
        assertThat(sqlStatementQueries.get(0)).isEqualTo("create index if not exists embeddings_metadata on embeddings " +
                "USING GIN (metadata)");
    }

    @Test
    void createSimpleMetadataIndexes_jsonb_path_ops() throws SQLException {
        Statement statement = mock(Statement.class);
        List<String> sqlStatementQueries = new ArrayList<>();
        when(statement.executeUpdate(anyString()))
                .thenAnswer(AdditionalAnswers.answerVoid(q -> sqlStatementQueries.add((String)q)));

        MetadataStorageConfig metadataStorageConfig = DefaultMetadataStorageConfig.builder()
                .storageMode(MetadataStorageMode.COMBINED_JSONB)
                .columnDefinitions(Collections.singletonList("metadata JSONB"))
                .indexes(Collections.singletonList("metadata jsonb_path_ops"))
                .indexType("GIN")
                .build();
        JSONBMetadataHandler jsonbMetadataHandler = new JSONBMetadataHandler(metadataStorageConfig);
        jsonbMetadataHandler.createMetadataIndexes(statement, "embeddings");

        assertThat(sqlStatementQueries).hasSize(1);
        assertThat(sqlStatementQueries.get(0)).isEqualTo("create index if not exists embeddings_metadata_jsonb_path_ops on embeddings " +
                "USING GIN (metadata jsonb_path_ops)");
    }

    @Test
    void createJSONNodeMetadataIndexes() throws SQLException {
        Statement statement = mock(Statement.class);
        List<String> sqlStatementQueries = new ArrayList<>();
        when(statement.executeUpdate(anyString()))
                .thenAnswer(AdditionalAnswers.answerVoid(q -> sqlStatementQueries.add((String)q)));

        MetadataStorageConfig metadataStorageConfig = DefaultMetadataStorageConfig.builder()
                .storageMode(MetadataStorageMode.COMBINED_JSONB)
                .columnDefinitions(Collections.singletonList("metadata JSONB"))
                .indexes(Arrays.asList("(metadata->key1)", "(metadata->key2)"))
                .indexType("GIN")
                .build();
        JSONBMetadataHandler jsonbMetadataHandler = new JSONBMetadataHandler(metadataStorageConfig);
        jsonbMetadataHandler.createMetadataIndexes(statement, "embeddings");

        assertThat(sqlStatementQueries).hasSize(2);
        assertThat(sqlStatementQueries.get(0)).isEqualTo("create index if not exists embeddings_metadata_key1 on embeddings " +
                "USING GIN ((metadata->key1))");
        assertThat(sqlStatementQueries.get(1)).isEqualTo("create index if not exists embeddings_metadata_key2 on embeddings " +
                "USING GIN ((metadata->key2))");
    }
}