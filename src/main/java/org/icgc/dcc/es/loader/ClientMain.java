package org.icgc.dcc.es.loader;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.System.err;
import lombok.val;
import org.icgc.dcc.es.loader.core.Indexer;

import java.io.File;

public class ClientMain {

  public static void main(String[] args) {
    val options = new ClientOptions();
    val cli = new JCommander(options);

    try {
      cli.parse(args);
      val esUri = options.esUri;

      val inputDirectory = new File(options.inputDir);
      val configDirectory = new File(options.configDir);
      checkState(inputDirectory.isDirectory() && inputDirectory.canRead(), "Failed to read from %s", inputDirectory);
      checkState(configDirectory.isDirectory() && configDirectory.canRead(), "Failed to read from %s", configDirectory);

      val indexer = new Indexer(esUri, options.indexNamePrefix, inputDirectory, configDirectory, options.concurrency,
          options.skipIndexInit);
      indexer.indexDocuments();
    } catch (ParameterException e) {
      err.println(e.toString());
      usage(cli);
      System.exit(1);
    }
  }

  private static void usage(JCommander cli) {
    val message = new StringBuilder();
    cli.usage(message);
    err.println(message.toString());
  }

}
