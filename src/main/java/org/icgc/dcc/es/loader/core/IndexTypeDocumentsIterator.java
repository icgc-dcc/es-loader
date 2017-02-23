package org.icgc.dcc.es.loader.core;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.dcc.common.es.impl.IndexDocumentType;
import org.icgc.dcc.dcc.common.es.model.IndexDocument;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

@Slf4j
public class IndexTypeDocumentsIterator extends AbstractIndexDocumentIterator {

  private final IndexDocumentType indexType;
  private final Iterator<Path> filesIterator;

  @SneakyThrows
  public IndexTypeDocumentsIterator(File indexTypeDir) {
    log.debug("Creating IndexDocumentsIterator for index type directory '{}'", indexTypeDir);
    val indexDirPath = Paths.get(indexTypeDir.getAbsolutePath());
    val dirStream = Files.walk(indexDirPath).filter(Files::isRegularFile);
    val indexTypeName = indexTypeDir.getName();
    log.debug("Resolved index type name to '{}'", indexTypeName);

    this.indexType = () -> indexTypeName;
    this.filesIterator = dirStream.iterator();
  }

  @Override
  public boolean hasNext() {
    return filesIterator.hasNext();
  }

  @Override
  @SneakyThrows
  public IndexDocument next() {
    val docPath = filesIterator.next();
    val docFile = docPath.toFile();

    log.debug("Reading {}", docFile);
    val source = (ObjectNode) DEFAULT_MAPPER.readTree(docFile);
    val fileName = docFile.getName();

    return createIndexDocument(fileName, indexType, source);
  }

}
