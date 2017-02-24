/*
 * Copyright (c) 2017 The Ontario Institute for Cancer Research. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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
