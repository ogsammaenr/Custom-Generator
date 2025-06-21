package xanth.ogsammaenr.customGenerator.storage;

import java.sql.Connection;
import java.sql.SQLException;

public interface DatabaseConnector {

    void connect();

    Connection getConnection() throws SQLException;

    void initializeTables();

    void close();
}
