/* SPDX-License-Identifier: Apache-2.0 */

package marquez.jdbi;

import javax.sql.DataSource;
import marquez.db.DbContainer;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.postgresql.ds.PGSimpleDataSource;

public class MarquezJdbiExternalPostgresExtension extends JdbiExternalPostgresExtension {

  private static final DbContainer POSTGRES = DbContainer.create("marquez");

  static {
    POSTGRES.start();
  }

  private final String hostname;
  private final Integer port;
  private final String username;
  private final String password;
  private final String database;

  MarquezJdbiExternalPostgresExtension() {
    super();
    hostname = POSTGRES.getHost();
    port = POSTGRES.getPort();
    username = POSTGRES.getUsername();
    password = POSTGRES.getPassword();
    database = POSTGRES.getDatabaseName();
    plugins.add(new SqlObjectPlugin());
    plugins.add(new PostgresPlugin());
    migration = Migration.before().withPath("marquez/db/migration");
  }

  protected DataSource createDataSource() {
    final PGSimpleDataSource datasource = new PGSimpleDataSource();
    datasource.setServerName(hostname);
    datasource.setPortNumber(port);
    datasource.setUser(username);
    datasource.setPassword(password);
    datasource.setDatabaseName(database);
    datasource.setApplicationName("Marquez Unit Tests");
    return datasource;
  }
}
