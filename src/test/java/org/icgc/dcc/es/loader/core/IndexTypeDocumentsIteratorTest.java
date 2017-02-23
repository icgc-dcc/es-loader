package org.icgc.dcc.es.loader.core;

import lombok.val;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

import java.io.File;

public class IndexTypeDocumentsIteratorTest {

  private static final String INDEX_TYPE_NAME = "case-ssm-child";
  private static final File INPUT_DIR = new File("src/test/resources/fixtures/input/gdc-r1-gene/" + INDEX_TYPE_NAME);

  IndexTypeDocumentsIterator documentsIterator = new IndexTypeDocumentsIterator(INPUT_DIR);

  @Test
  public void testHasNext() throws Exception {
    assertThat(documentsIterator.hasNext()).isTrue();
    assertThat(documentsIterator.hasNext()).isTrue();
    assertThat(documentsIterator.hasNext()).isTrue();
    assertThat(documentsIterator.hasNext()).isTrue();
    documentsIterator.next();
    assertThat(documentsIterator.hasNext()).isFalse();
    assertThat(documentsIterator.hasNext()).isFalse();
    assertThat(documentsIterator.hasNext()).isFalse();
    assertThat(documentsIterator.hasNext()).isFalse();
  }

  @Test
  public void testNext() throws Exception {
    val actualDoc = documentsIterator.next();
    assertThat(actualDoc.getId()).isEqualTo("ID1");
    assertThat(actualDoc.getParentId()).isEqualTo("ENSG00000104904");
    assertThat(actualDoc.getSource().toString()).isEqualTo("{\"value\":1}");
    assertThat(actualDoc.getType().getIndexType()).isEqualTo(INDEX_TYPE_NAME);
  }

}
