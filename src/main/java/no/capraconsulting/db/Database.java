package no.capraconsulting.db;

import no.capraconsulting.config.PropertiesHelper;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class Database {

    public static Database INSTANCE = new Database();
    private static Logger LOG = LoggerFactory.getLogger(Database.class);
    private DataSource dataSource;
    
    /**
     * This constructor initiates the single INSTANCE of the Database
     * while Hikari manages the connection to the DB, and pooling/caching
     * of the connections, for faster access times
     */
    private Database() {
        HikariConfig config = new HikariConfig();

        Properties properties = PropertiesHelper.getProperties();
        String name = getProperty(properties, PropertiesHelper.DB_CONTAINER_NAME);
        String port = getProperty(properties, PropertiesHelper.DB_PORT); 
        String database = getProperty(properties, PropertiesHelper.DB_NAME); 
        String user = getProperty(properties, PropertiesHelper.DB_USER);
        String password = getProperty(properties, PropertiesHelper.DB_PASSWORD);

        String url = String.format("jdbc:sqlserver://%s:%s;databaseName=%s", name, port, database);        
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        this.dataSource = new HikariDataSource(config);  
    }

    /**
     * Get a cached ResultSet from the database
     *
     * @param query  SQL SELECT query
     * @param values Values to insert into the parameters of the PreparedStatement
     * @return Cached ResultSet of the query
     */
    public CachedRowSet selectQuery(String query, Object... values) throws SQLException {
        ResultSet result = null;
        try (
                Connection connection = this.getConnection();
                PreparedStatement statement = connection.prepareStatement(query);
        ) {
            for (int i = 0; i < values.length; i++) {
                statement.setObject(i + 1, values[i]);
            }

            result = statement.executeQuery();
            RowSetFactory factory = RowSetProvider.newFactory();
            CachedRowSet crs = factory.createCachedRowSet();
            crs.populate(result);
            return crs;
        } finally { 
            if (result != null) {
                try {
                    result.close();
                } catch (SQLException e) {
                    LOG.warn("Could not close SQL ResultSet");
                    LOG.warn(e.getMessage());
                }
                result = null;
            }
        }
    }

    /**
     * Manipulate the database
     *
     * @param sql SQL query to run
     * @param returnGeneratedKey If true, this method will return the generated primary
     * key after the insertion of the new row (for SQL INSERTs)
     * @param values Values to insert into the parameters of the PreparedStatement
     * @return The auto-generated id of the new row, -1 if no key returned
     */
    public int manipulateQuery(String query, boolean returnGeneratedKey, Object... values) throws SQLException {
        try (
                Connection connection = this.getConnection();
                PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        ) {
            for (int i = 0; i < values.length; i++) {
                statement.setObject(i + 1, values[i]);
            }

            int affected = statement.executeUpdate();
            LOG.debug("Query: {" + query + "} completed with " + affected + " rows affected");

            if (returnGeneratedKey) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        return id;
                    } else {
                        throw new SQLException("Insertion failed, no ID obtained.");
                    }
                }
            }
        }
        return -1;
    }

    // Getters for database objects
    public Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }

    public DataSource getDataSource() {
        return this.dataSource;
    }
    
    // Helpers
    private String getProperty(Properties properties, String property) {
        return PropertiesHelper.getStringProperty(properties, property, null);
    }
}
