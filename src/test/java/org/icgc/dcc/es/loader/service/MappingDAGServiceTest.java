package org.icgc.dcc.es.loader.service;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import static org.assertj.core.api.Assertions.assertThat;
import org.icgc.dcc.es.loader.model.MappingDAG.TypeMapping;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

@Ignore
@Slf4j
public class MappingDAGServiceTest {

  private final String path = "";

  @Test
  public void testDAG() {
    val service = new MappingDAGService(new File(path));
    val graph = service.readMappings();
  }

  @Test
  public void testRecursiveConsumer() {
    val service = new MappingDAGService(new File(path));
    val graph = service.readMappings();

    val caseMapping = graph.getRoot("case");
    caseMapping.applyRecursive((TypeMapping m) -> log.info(m.getTypeName()));
  }

  @Test
  public void testRecursivePredicate() {
    val service = new MappingDAGService(new File(path));
    val graph = service.readMappings();

    val caseMapping = graph.getRoot("case");
    assertThat(caseMapping.applyRecursive((TypeMapping m) -> {
      log.info(m.getTypeName());
      return m.getTypeName().equals("file_child");
    }));
  }

}