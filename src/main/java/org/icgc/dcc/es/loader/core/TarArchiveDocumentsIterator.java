package org.icgc.dcc.es.loader.core;

import com.fasterxml.jackson.databind.node.ObjectNode;
import static com.google.common.base.Preconditions.checkState;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.icgc.dcc.dcc.common.es.impl.IndexDocumentType;
import org.icgc.dcc.dcc.common.es.model.IndexDocument;
import static org.icgc.dcc.es.loader.util.TarArchiveNames.getDocumentId;
import static org.icgc.dcc.es.loader.util.TarArchiveNames.getDocumentType;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

@Slf4j
public class TarArchiveDocumentsIterator extends AbstractIndexDocumentIterator implements Closeable {

  private final TarArchiveInputStream tarInputStream;

  private IndexDocument nextDocument;

  public TarArchiveDocumentsIterator(@NonNull File archive) {
    val absolutePath = archive.getAbsolutePath();
    checkState(archive.canRead(), "Failed to read %s", absolutePath);
    log.info("Creating tar archive documents iterator for file '{}'", absolutePath);

    this.tarInputStream = readArchiveStream(archive);
  }

  @Override
  public boolean hasNext() {
    if (nextDocument == null) {
      nextDocument = readNextDocument();
    }

    return nextDocument != null;
  }

  @Override
  public IndexDocument next() {
    // Read next document if it was not read.
    hasNext();
    val next = nextDocument;
    nextDocument = null;

    return next;
  }

  @Override
  public void close() throws IOException {
    if (tarInputStream != null) {
      tarInputStream.close();
    }
  }

  @SneakyThrows
  private IndexDocument readNextDocument() {
    val entry = readNextTarEntry();
    if (entry == null) {
      return null;
    }

    val entryName = entry.getName();
    val fileName = getDocumentId(entryName);
    val source = (ObjectNode) NO_CLOSE_MAPPER.readTree(tarInputStream);
    final IndexDocumentType indexType = () -> getDocumentType(entryName);

    return createIndexDocument(fileName, indexType, source);
  }

  @SneakyThrows
  private TarArchiveEntry readNextTarEntry() {
    TarArchiveEntry entry;
    while ((entry = tarInputStream.getNextTarEntry()) != null && entry.isDirectory()) {
      // read until get a non-directory entry
    }

    return entry;
  }

  @SneakyThrows
  private static TarArchiveInputStream readArchiveStream(File archive) {
    val inputStream = new BufferedInputStream(new GZIPInputStream(new FileInputStream(archive)));

    return new TarArchiveInputStream(inputStream);
  }

}
