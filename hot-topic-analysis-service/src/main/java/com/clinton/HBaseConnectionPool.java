package com.clinton;

import cn.danielw.fop.ObjectFactory;
import cn.danielw.fop.ObjectPool;
import cn.danielw.fop.PoolConfig;
import cn.danielw.fop.Poolable;
import lombok.SneakyThrows;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;

public class HBaseConnectionPool {

    private static final String HBASE_CONFIG_FILE = "/app/hbase-site.xml";

    private static final ObjectPool<Connection> pool;
    private static final Configuration config = getConfiguration();

    private HBaseConnectionPool(){}

    static {
        PoolConfig poolConfig = new PoolConfig();
        poolConfig.setPartitionSize(5);
        poolConfig.setMaxSize(10);
        poolConfig.setMinSize(5);
        poolConfig.setMaxIdleMilliseconds(120);

        ObjectFactory<Connection> factory = new ObjectFactory<Connection>() {
            @SneakyThrows
            @Override
            public Connection create() {
                return ConnectionFactory.createConnection(config);
            }

            @SneakyThrows
            @Override
            public void destroy(Connection connection) {
                connection.close();
            }

            @Override
            public boolean validate(Connection connection) {
                return !connection.isClosed();
            }
        };

        pool = new ObjectPool<>(poolConfig, factory);
    }

    public static Poolable<Connection> getConnection() {
        return pool.borrowObject();
    }

    @SneakyThrows
    public static void shutdown() {
        pool.shutdown();
    }

    private static Configuration getConfiguration() {
        Configuration hbaseConfig = HBaseConfiguration.create();
        hbaseConfig.addResource(new Path(HBASE_CONFIG_FILE));
        return hbaseConfig;
    }
}
