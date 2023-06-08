/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api;

import static marquez.common.models.CommonModelGenerator.newNamespaceName;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.openlineage.client.OpenLineage;
import io.openlineage.client.OpenLineageClient;
import io.openlineage.client.transports.HttpTransport;
import java.net.URI;
import java.net.URL;
import marquez.MarquezApp;
import marquez.MarquezConfig;
import marquez.client.MarquezClient;
import marquez.client.Utils;
import marquez.db.DbContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Base class for {@code resource} integration tests. */
@Testcontainers
abstract class BaseResourceIntegrationTest {
  static final String ERROR_NAMESPACE_NOT_FOUND = "Namespace '%s' not found.";
  static final String ERROR_JOB_VERSION_NOT_FOUND = "Job version '%s' not found.";
  static final String ERROR_JOB_NOT_FOUND = "Job '%s' not found.";
  static final String ERROR_FAIL_IF_NOT_IN = "Expected '%s' in '%s'.";

  /* The OL producer. */
  static final URI OL_PRODUCER =
      URI.create(
          "https://github.com/MarquezProject/marquez/tree/main/api/src/test/java/marquez/api/models/ActiveRun.java");

  /* Load configuration for testing. */
  static final String CONFIG_FILE = "config.test.yml";
  static final String CONFIG_FILE_PATH = ResourceHelpers.resourceFilePath(CONFIG_FILE);

  @Container static final DbContainer MARQUEZ_DB = DbContainer.create("marquez");

  static String NAMESPACE_NAME;
  static DropwizardAppExtension<MarquezConfig> MARQUEZ_APP;

  static OpenLineage OL;
  static OpenLineageClient OL_CLIENT;
  static MarquezClient MARQUEZ_CLIENT;

  @BeforeAll
  public static void setUp() throws Exception {
    // (1) Use a randomly generated namespace.
    NAMESPACE_NAME = newNamespaceName().getValue();

    // (2) Configure Marquez application using test configuration and database.
    MARQUEZ_APP =
        new DropwizardAppExtension<>(
            MarquezApp.class,
            CONFIG_FILE_PATH,
            ConfigOverride.config("db.url", MARQUEZ_DB.getJdbcUrl()),
            ConfigOverride.config("db.user", MARQUEZ_DB.getUsername()),
            ConfigOverride.config("db.password", MARQUEZ_DB.getPassword()));
    MARQUEZ_APP.before();

    final URL url = Utils.toUrl("http://localhost:" + MARQUEZ_APP.getLocalPort());

    // (3) Configure clients.
    OL = new OpenLineage(OL_PRODUCER);
    OL_CLIENT =
        OpenLineageClient.builder()
            .transport(HttpTransport.builder().uri(url.toURI()).build())
            .build();
    MARQUEZ_CLIENT = MarquezClient.builder().baseUrl(url).build();
  }

  @AfterAll
  public static void cleanUp() {
    MARQUEZ_APP.after();
  }
}
