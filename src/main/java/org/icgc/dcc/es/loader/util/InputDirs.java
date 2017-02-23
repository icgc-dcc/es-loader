package org.icgc.dcc.es.loader.util;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PRIVATE;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.File;
import java.util.Arrays;

@Slf4j
@NoArgsConstructor(access = PRIVATE)
public final class InputDirs {

  private static final String TAR_GZ_SUFFIX = ".*\\.tar\\.gz";

  public static Iterable<String> getIndexNames(File inputDir, String indexNamePrefix) {
    File[] files = isNullOrEmpty(indexNamePrefix) ?
        inputDir.listFiles() :
        inputDir.listFiles(pathname -> pathname.getName().startsWith(indexNamePrefix));

    return Arrays.stream(files)
        .filter(File::isDirectory)
        .map(File::getName)
        .collect(toList());
  }

  public static Iterable<String> getArchiveFileNames(File inputDir, String indexNamePrefix) {
    File[] files = getArchiveFiles(inputDir, indexNamePrefix);

    return Arrays.stream(files)
        .filter(File::isFile)
        .map(File::getName)
        .collect(toList());
  }

  public static Iterable<String> getIndexNamesFromArchiveFiles(File inputDir, String indexNamePrefix) {
    File[] files = getArchiveFiles(inputDir, indexNamePrefix);
    log.info("Resolved archive files from input dir '{}' and prefix '{}': {}", inputDir, indexNamePrefix, files);

    return Arrays.stream(files)
        .filter(File::isFile)
        .map(File::getName)
        .map(InputDirs::removeArchiveExtention)
        .collect(toList());
  }

  private static String removeArchiveExtention(String archiveName) {
    val extentionStart = archiveName.indexOf(".");

    return archiveName.substring(0, extentionStart);
  }

  private static File[] getArchiveFiles(File inputDir, String indexNamePrefix) {
    val nameRegex = isNullOrEmpty(indexNamePrefix) ? TAR_GZ_SUFFIX : indexNamePrefix + TAR_GZ_SUFFIX;

    return inputDir.listFiles(pathname -> pathname.getName().matches(nameRegex));
  }

}
