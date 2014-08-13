/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sqoop.connector.jdbc;

import org.apache.log4j.Logger;
import org.apache.sqoop.connector.jdbc.configuration.ConnectionConfiguration;
import org.apache.sqoop.connector.jdbc.configuration.ToJobConfiguration;
import org.apache.sqoop.job.etl.Destroyer;
import org.apache.sqoop.job.etl.DestroyerContext;

public class GenericJdbcToDestroyer extends Destroyer<ConnectionConfiguration, ToJobConfiguration> {

  private static final Logger LOG = Logger.getLogger(GenericJdbcToDestroyer.class);

  @Override
  public void destroy(DestroyerContext context, ConnectionConfiguration connection, ToJobConfiguration job) {
    LOG.info("Running generic JDBC connector destroyer");

    final String tableName = job.table.tableName;
    final String stageTableName = job.table.stageTableName;
    final boolean stageEnabled = stageTableName != null &&
      stageTableName.length() > 0;
    if(stageEnabled) {
      moveDataToDestinationTable(connection,
        context.isSuccess(), stageTableName, tableName);
    }
  }

  private void moveDataToDestinationTable(ConnectionConfiguration connectorConf,
    boolean success, String stageTableName, String tableName) {
    GenericJdbcExecutor executor =
      new GenericJdbcExecutor(connectorConf.connection.jdbcDriver,
        connectorConf.connection.connectionString,
        connectorConf.connection.username,
        connectorConf.connection.password);

    if(success) {
      LOG.info("Job completed, transferring data from stage table to " +
        "destination table.");
      executor.migrateData(stageTableName, tableName);
    } else {
      LOG.warn("Job failed, clearing stage table.");
      executor.deleteTableData(stageTableName);
    }
  }

}