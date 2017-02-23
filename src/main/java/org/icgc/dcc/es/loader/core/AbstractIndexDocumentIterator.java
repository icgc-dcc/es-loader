package org.icgc.dcc.es.loader.core;

import static com.fasterxml.jackson.core.JsonParser.Feature.AUTO_CLOSE_SOURCE;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.val;
import org.icgc.dcc.dcc.common.es.impl.IndexDocumentType;
import org.icgc.dcc.dcc.common.es.model.IndexDocument;

public abstract class AbstractIndexDocumentIterator implements IndexDocumentsIterator {

  protected static final String ID_SEPARATOR = ".";
  protected static final ObjectMapper DEFAULT_MAPPER = new ObjectMapper();
  protected static final ObjectMapper NO_CLOSE_MAPPER = new ObjectMapper().configure(AUTO_CLOSE_SOURCE, false);

  protected IndexDocument createIndexDocument(String fileName, IndexDocumentType indexType, ObjectNode source) {
    return fileName.contains(ID_SEPARATOR) ?
        createParentChildDocument(fileName, indexType, source) :
        new IndexDocument(fileName, source, indexType);
  }

  private static IndexDocument createParentChildDocument(String fileName, IndexDocumentType indexType, ObjectNode source) {
    val separatorPosition = fileName.indexOf(ID_SEPARATOR);
    val parentId = fileName.substring(0, separatorPosition);
    val childId = fileName.substring(separatorPosition + 1);

    return new IndexDocument(childId, source, indexType, parentId);
  }

}