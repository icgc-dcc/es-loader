package org.icgc.dcc.es.loader.core;

import lombok.val;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

import java.io.File;

public class DocumentSourceTest {

  private static final String INDEX_TYPE_NAME = "case-ssm-child";
  private static final String INDEX_NAME = "gdc-r1-gene";
  private static final File INPUT_DIR = new File("src/test/resources/fixtures/input");

  DocumentSource documentSource = new DocumentSource(INPUT_DIR);

  @Test
  public void testGetIndexNames() throws Exception {
    assertThat(documentSource.getIndexNames(null)).containsOnly(INDEX_NAME);
    assertThat(documentSource.getIndexNames("gdc-")).containsOnly(INDEX_NAME);
    assertThat(documentSource.getIndexNames("boo")).isEmpty();
  }

  @Test
  public void testGetIndexDocumentsIterator() throws Exception {
    val iterator = documentSource.getIndexDocumentsIterator(INDEX_NAME, INDEX_TYPE_NAME);
    assertThat(iterator.hasNext()).isTrue();
    assertThat(iterator.next().getId()).isEqualTo("ID1");
    assertThat(iterator.hasNext()).isFalse();
  }

  @Test
  public void testGetIndexTypes() throws Exception {
    assertThat(documentSource.getIndexTypes(INDEX_NAME)).containsOnly(INDEX_TYPE_NAME, "gene");
  }

}