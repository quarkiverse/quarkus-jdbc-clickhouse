package io.quarkiverse.jdbc.clickhouse.deployment;

import static io.quarkiverse.jdbc.clickhouse.deployment.JdbcClickhouseProcessor.DATA_SOURCE_NAME;
import static io.quarkiverse.jdbc.clickhouse.deployment.JdbcClickhouseProcessor.DRIVER_NAME;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;

/**
 * Registers the {@code org.sqlite.JDBC} so that it can be loaded
 * by reflection, as commonly expected.
 *
 * @author Alexey Sharandin <sanders@yandex.ru>
 */

@SuppressWarnings("unused")
public class ClickHouseJDBCReflections {
    private static final String[] LZ4_DYNAMIC_CLASSES = {
            "com.clickhouse.client.internal.net.jpountz.lz4.LZ4JNICompressor",
            "com.clickhouse.client.internal.net.jpountz.lz4.LZ4HCJNICompressor",
            "com.clickhouse.client.internal.net.jpountz.lz4.LZ4JNIFastDecompressor",
            "com.clickhouse.client.internal.net.jpountz.lz4.LZ4JNISafeDecompressor",
            "com.clickhouse.client.internal.net.jpountz.lz4.LZ4JavaSafeCompressor",
            "com.clickhouse.client.internal.net.jpountz.lz4.LZ4HCJavaSafeCompressor",
            "com.clickhouse.client.internal.net.jpountz.lz4.LZ4JavaSafeFastDecompressor",
            "com.clickhouse.client.internal.net.jpountz.lz4.LZ4JavaSafeSafeDecompressor",
            "com.clickhouse.client.internal.net.jpountz.lz4.LZ4JavaUnsafeCompressor",
            "com.clickhouse.client.internal.net.jpountz.lz4.LZ4HCJavaUnsafeCompressor",
            "com.clickhouse.client.internal.net.jpountz.lz4.LZ4JavaUnsafeFastDecompressor",
            "com.clickhouse.client.internal.net.jpountz.lz4.LZ4JavaUnsafeSafeDecompressor"
    };

    @BuildStep
    void build(BuildProducer<ReflectiveClassBuildItem> reflectiveClass) {
        //Not strictly necessary when using Agroal, as it also registers
        //any JDBC driver being configured explicitly through its configuration.
        //We register it for the sake of people not using Agroal.
        reflectiveClass.produce(ReflectiveClassBuildItem.builder(DATA_SOURCE_NAME, DRIVER_NAME).constructors().build());
        // ClickHouse's shaded LZ4 factory constructs implementation class names dynamically and
        // loads them via ClassLoader at runtime, so native-image cannot discover them statically.
        reflectiveClass.produce(ReflectiveClassBuildItem.builder(LZ4_DYNAMIC_CLASSES)
                .constructors()
                .methods()
                .fields()
                .build());
    }
}
