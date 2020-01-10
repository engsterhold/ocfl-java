package edu.wisc.library.ocfl.core.db;

import edu.wisc.library.ocfl.api.util.Enforce;

import javax.sql.DataSource;
import java.util.concurrent.TimeUnit;

/**
 * Constructs {@link ObjectDetailsDatabase} instances
 */
public class ObjectDetailsDatabaseBuilder {

    private boolean storeInventory;
    private long waitTime;
    private TimeUnit timeUnit;

    public ObjectDetailsDatabaseBuilder() {
        storeInventory = true;
        waitTime = 10;
        timeUnit = TimeUnit.SECONDS;
    }

    /**
     * If serialized inventories should be stored in the database. Default: true.
     *
     * @param storeInventory true if serialized inventories should be stored in the database.
     * @return builder
     */
    public ObjectDetailsDatabaseBuilder storeInventory(boolean storeInventory) {
        this.storeInventory = storeInventory;
        return this;
    }

    /**
     * Used to override the amount of time the client will wait to obtain a lock. Default: 10 seconds.
     *
     * @param waitTime wait time
     * @param timeUnit unit of time
     * @return builder
     */
    public ObjectDetailsDatabaseBuilder waitTime(long waitTime, TimeUnit timeUnit) {
        this.waitTime = Enforce.expressionTrue(waitTime > 0, waitTime, "waitTime must be greater than 0");
        this.timeUnit = Enforce.notNull(timeUnit, "timeUnit cannot be null");
        return this;
    }

    /**
     * Constructs a new {@link ObjectDetailsDatabase} instance using the given dataSource. If the database does not
     * already contain an object details table, it attempts to create one.
     *
     * @param dataSource the connection to the database
     * @return ObjectDetailsDatabase
     */
    public ObjectDetailsDatabase build(DataSource dataSource) {
        Enforce.notNull(dataSource, "dataSource cannot be null");

        var dbType = DbType.fromDataSource(dataSource);
        ObjectDetailsDatabase database;

        switch (dbType) {
            case POSTGRES:
                database = new PostgresObjectDetailsDatabase(dataSource, storeInventory, waitTime, timeUnit);
                break;
            case H2:
                database = new H2ObjectDetailsDatabase(dataSource, storeInventory, waitTime, timeUnit);
                break;
            default:
                throw new IllegalStateException(String.format("Database type %s is not mapped to an ObjectDetailsDatabase implementation.", dbType));
        }

        new TableCreator(dbType, dataSource).createObjectDetailsTable();

        return database;
    }

}
