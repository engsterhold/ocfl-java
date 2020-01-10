package edu.wisc.library.ocfl.core.lock;

import edu.wisc.library.ocfl.api.exception.LockException;
import edu.wisc.library.ocfl.api.util.Enforce;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class PostgresObjectLock implements ObjectLock {

    private static final Logger LOG = LoggerFactory.getLogger(PostgresObjectLock.class);

    private static final String OBJECT_LOCK_FAIL = "55P03";

    private DataSource dataSource;
    private long waitMillis;

    public PostgresObjectLock(DataSource dataSource, long waitTime, TimeUnit timeUnit) {
        this.dataSource = Enforce.notNull(dataSource, "dataSource cannot be null");
        Enforce.expressionTrue(waitTime > -1, waitTime, "waitTime cannot be negative");
        Enforce.notNull(timeUnit, "timeUnit cannot be null");
        this.waitMillis = timeUnit.toMillis(waitTime);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doInWriteLock(String objectId, Runnable doInLock) {
        doInWriteLock(objectId, () -> {
            doInLock.run();
            return null;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T doInWriteLock(String objectId, Callable<T> doInLock) {
        try (var connection = dataSource.getConnection()) {
            createLockRow(objectId, connection);
            connection.setAutoCommit(false);
            setLockWaitTimeout(connection);

            try (var statement = acquireLock(connection)) {
                statement.setString(1, objectId);

                try (var resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return doInLock.call();
                    } else {
                        throw failedToAcquireLock(objectId);
                    }
                } catch (SQLException e) {
                    if (OBJECT_LOCK_FAIL.equals(e.getSQLState())) {
                        throw failedToAcquireLock(objectId);
                    }
                    throw e;
                }
            } finally {
                safeCleanup(connection);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void createLockRow(String objectId, Connection connection) throws SQLException {
        try (var statement = connection.prepareStatement("INSERT INTO ocfl_object_lock" +
                " (object_id) VALUES (?)" +
                " ON CONFLICT (object_id) DO NOTHING")) {
            statement.setString(1, objectId);
            statement.executeUpdate();
        }
    }

    private void setLockWaitTimeout(Connection connection) throws SQLException {
        try (var statement = connection.prepareStatement(String.format("SET LOCAL lock_timeout = %s", waitMillis))) {
            statement.executeUpdate();
        }
    }

    private LockException failedToAcquireLock(String objectId) {
        return new LockException("Failed to acquire lock for object " + objectId);
    }

    private PreparedStatement acquireLock(Connection connection) throws SQLException {
        return connection.prepareStatement("SELECT object_id FROM ocfl_object_lock WHERE object_id = ? FOR UPDATE");
    }

    private void safeCleanup(Connection connection) {
        try {
            connection.commit();
        } catch (Exception e) {
            LOG.warn("Failed to commit", e);
        }

        try {
            connection.setAutoCommit(true);
        } catch (Exception e) {
            LOG.warn("Failed to enable autocommit", e);
        }
    }

}
