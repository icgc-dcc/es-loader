package org.icgc.dcc.es.loader.util;

import static lombok.AccessLevel.PRIVATE;
import lombok.NoArgsConstructor;
import lombok.val;

@NoArgsConstructor(access = PRIVATE)
public final class TarArchiveNames {

  public static String getIndexName(String entryName) {
    val extensionStart = entryName.indexOf(".");

    return entryName.substring(0, extensionStart);
  }

  public static String getDocumentType(String entryName) {
    return parseParts(entryName)[1];
  }

  public static String getDocumentId(String entryName) {
    String[] parts = parseParts(entryName);
    return parts[parts.length - 1];
  }

  private static String[] parseParts(String entryName) {
    return entryName.split("/");
  }

}
