package org.icgc.dcc.es.loader.service;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

@Ignore
@Slf4j
public class SettingsServiceTest {

  private final String path = "";

  @Test
  public void testIndexMap() {
    val service = new SettingsService(new File(path));
    val map = service.readSettings();
    map.entrySet().forEach(e -> log.info("Settings file read: {}", e.getKey()));
  }

}