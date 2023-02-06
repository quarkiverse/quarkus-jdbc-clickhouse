/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package io.quarkiverse.quarkus.jdbc.clickhouse.it;

import java.sql.*;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import com.clickhouse.jdbc.internal.ClickHouseConnectionImpl;

import io.agroal.api.AgroalDataSource;
import io.agroal.pool.wrapper.ConnectionWrapper;
import io.quarkus.runtime.StartupEvent;

@Path("/jdbc-clickhouse")
@ApplicationScoped
public class JdbcClickhouseResource {
    @Inject
    AgroalDataSource ds;

    void onStart(@Observes StartupEvent event) throws Exception {
        //com.clickhouse.client.ClickHouseClientBuilder
        String agent = System.getProperty("agent");
        System.out.println("agent = " + agent);
        if ("true".equals(agent)) {
            Thread.sleep(2000);
            testAgoral();
        }
    }

    @GET
    @Path("agoral")
    public String testAgoral() throws SQLException {
        String result;
        try (Connection connection = ds.getConnection()) {
            if (connection instanceof ConnectionWrapper) {
                unwrap((ConnectionWrapper) connection);
            }
            System.out.println("connection = " + connection.getClass().getName() + ":" + connection);
            DatabaseMetaData metaData = connection.getMetaData();

            System.out.println("conn conn=" + metaData.getConnection());
            System.out.println("conn url=" + metaData.getURL());
            result = test(connection);
        }
        return result;
    }

    private void unwrap(ConnectionWrapper connection) throws SQLException {
        ClickHouseConnectionImpl unwrap = connection.unwrap(ClickHouseConnectionImpl.class);
        String collect = unwrap.getClientInfo().entrySet().stream()
                .map(e -> e.getKey() + ":" + e.getValue())
                .collect(Collectors.joining(", "));
        System.out.println("collect = " + collect);
    }

    private String test(Connection connection) throws SQLException {
        StringBuilder result = new StringBuilder();
        try (Statement statement = connection.createStatement()) {
            statement.setQueryTimeout(30); // set timeout to 30 sec.

            statement.executeUpdate("drop table if exists xperson");
            statement.executeUpdate("create table xperson (id Int64, name String) ENGINE = Memory");
            statement.executeUpdate("insert into xperson values(1, 'leo')");
            statement.executeUpdate("insert into xperson values(2, 'yui')");
            try (ResultSet rs = statement.executeQuery("select * from xperson where id = 1")) {
                while (rs.next()) {
                    result.append(rs.getInt("id")).append("/").append(rs.getString("name")).append("/");
                }
            }
        }
        return result.toString();
    }
}
