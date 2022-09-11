package com.devit.mscore.workflow.flowable.datasource;

import com.devit.mscore.Configuration;
import com.devit.mscore.WorkflowDataSource;
import com.devit.mscore.exception.ConfigException;

import java.util.Arrays;

import javax.sql.DataSource;

import org.postgresql.ds.PGSimpleDataSource;

public class PgDataSource implements WorkflowDataSource<DataSource> {

  private static final String DB_HOST = "platform.postgres.host";

  private static final String DB_PORT = "platform.postgres.port";

  private static final String DB_USERNAME = "platform.postgres.username";

  private static final String DB_PASSWORD = "platform.postgres.password";

  private static final String DB_NAME = "services.%s.db.name";

  private static final String DB_SCHEMA = "services.%s.db.schema";

  private static final String DEFAULT_PORT = "5432";

  private Configuration configuration;

  public PgDataSource(Configuration configuration) {
    this.configuration = configuration;
  }

  @Override
  public DataSource get() throws ConfigException {
    var serviceName = this.configuration.getServiceName();
    var serverNames = this.configuration.getConfig(DB_HOST)
        .orElseThrow(() -> new ConfigException("No psql host provided")).split(",");
    var portNumbers = getPorts();
    var user = this.configuration.getConfig(DB_USERNAME)
        .orElseThrow(() -> new ConfigException("No psql user provided"));
    var password = this.configuration.getConfig(DB_PASSWORD)
        .orElseThrow(() -> new ConfigException("No psql password provided"));
    var schemaName = this.configuration.getConfig(String.format(DB_SCHEMA, serviceName))
        .orElseThrow(() -> new ConfigException("No psql schema configured"));
    var databaseName = this.configuration.getConfig(String.format(DB_NAME, serviceName))
        .orElseThrow(() -> new ConfigException("No psql database configured"));

    return getDataSource(serverNames, portNumbers, user, password, schemaName, databaseName);
  }

  private DataSource getDataSource(String[] serverNames, int[] portNumbers, String user, String password,
      String schemaName, String databaseName) {
    var pgDataSource = new PGSimpleDataSource();
    pgDataSource.setServerNames(serverNames);
    pgDataSource.setPortNumbers(portNumbers);
    pgDataSource.setUser(user);
    pgDataSource.setPassword(password);
    pgDataSource.setCurrentSchema(schemaName);
    pgDataSource.setDatabaseName(databaseName);
    return pgDataSource;
  }

  private int[] getPorts() throws ConfigException {
    var ports = this.configuration.getConfig(DB_PORT).orElse(DEFAULT_PORT).split(",");
    return Arrays.stream(ports).mapToInt(Integer::parseInt).toArray();
  }

  @Override
  public Type getType() {
    return Type.SQL;
  }
}
