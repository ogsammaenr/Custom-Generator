package xanth.ogsammaenr.customGenerator.storage;

import java.sql.Connection;
import java.sql.SQLException;

public interface DatabaseConnector {
    /**
     * Connect To Database
     */
    void connect();

    /**
     * @return Connection
     * @throws SQLException
     */
    Connection getConnection() throws SQLException;

    /**
     * Create Tables If Not Exist
     */
    void initializeTables();

    void close();
}
