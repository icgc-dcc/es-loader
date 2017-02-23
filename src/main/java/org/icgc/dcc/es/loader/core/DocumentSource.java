package org.icgc.dcc.es.loader.core;

import static com.google.common.base.Preconditions.checkState;
import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.es.loader.util.InputDirs;

import java.io.File;

@Slf4j
@RequiredArgsConstructor
public class DocumentSource {

  @NonNull
  private final File inputDir;

  public IndexDocumentsIterator getIndexDocumentsIterator(@NonNull String indexName, @NonNull String indexType) {
    log.debug("Getting IndexDocumentsIterator for index '{}' and type '{}'", indexName, indexType);
    val indexDir = getIndexDir(indexName);
    val indexTypeDir = getTypeDir(indexDir, indexType);

    return new IndexTypeDocumentsIterator(indexTypeDir);
  }

  public TarArchiveDocumentsIterator getTarArchiveIndexDocumentsIterator(@NonNull String archiveName) {
    val archiveFile = new File(inputDir, archiveName);

    return new TarArchiveDocumentsIterator(archiveFile);
  }

  public Iterable<String> getIndexNames(String indexNamePrefix) {
    return InputDirs.getIndexNames(inputDir, indexNamePrefix);
  }

  public Iterable<String> getArchiveFileNames(String indexNamePrefix) {
    return InputDirs.getArchiveFileNames(inputDir, indexNamePrefix);
  }

  public Iterable<String> getIndexTypes(@NonNull String indexName) {
    val indexDir = getIndexDir(indexName);

    return ImmutableList.copyOf(indexDir.list());
  }

  private File getIndexDir(String indexName) {
    val indexDir = new File(inputDir, indexName);
    checkState(indexDir.exists(), "%s doesn't exist", indexName);

    return indexDir;
  }

  private static File getTypeDir(File indexDir, String indexType) {
    val indexTypeDir = new File(indexDir, indexType);
    checkState(indexTypeDir.exists(), "%s doesn't exist", indexTypeDir);

    return indexTypeDir;
  }

}
