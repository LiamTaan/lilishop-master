package tools;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Minimal JDBC runner for executing SQL patch files from UTF-8 files.
 */
public class DbPatchRunner {

    private static final DateTimeFormatter BACKUP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println("Usage: DbPatchRunner <jdbcUrl> <username> <password> [sqlFile...]");
            System.exit(1);
        }

        String jdbcUrl = args[0];
        String username = args[1];
        String password = args[2];

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
             Statement statement = connection.createStatement()) {
            statement.execute("SET NAMES utf8mb4");

            String snapshotSuffix = BACKUP_FORMATTER.format(LocalDateTime.now());
            backupTable(statement, "li_menu", snapshotSuffix);
            backupTable(statement, "li_role_menu", snapshotSuffix);

            for (int i = 3; i < args.length; i++) {
                Path sqlPath = Path.of(args[i]).toAbsolutePath();
                System.out.println("Executing SQL file: " + sqlPath);
                String sql = Files.readString(sqlPath, StandardCharsets.UTF_8);
                statement.execute(sql);
                consumeResults(statement);
            }

            printCount(statement, "li_menu", "SELECT COUNT(*) FROM li_menu");
            printCount(statement, "li_role_menu", "SELECT COUNT(*) FROM li_role_menu");
            printCount(statement, "procurement menus",
                    "SELECT COUNT(*) FROM li_menu WHERE id IN " +
                            "('3062200000000000015','3062200000000010098','3062200000000010099','3062200000000010100','3062200000000010101','3062200000000010102')");
            printCount(statement, "goods barcode column",
                    "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = database() AND table_name = 'li_goods' AND column_name = 'barcode'");
            printCount(statement, "goods sku barcode column",
                    "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = database() AND table_name = 'li_goods_sku' AND column_name = 'barcode'");
        }
    }

    private static void backupTable(Statement statement, String tableName, String suffix) throws Exception {
        String backupTableName = tableName + "__backup_" + suffix;
        String sql = "DROP TABLE IF EXISTS `" + backupTableName + "`; " +
                "CREATE TABLE `" + backupTableName + "` AS SELECT * FROM `" + tableName + "`;";
        System.out.println("Creating backup table: " + backupTableName);
        statement.execute(sql);
        consumeResults(statement);
    }

    private static void printCount(Statement statement, String label, String sql) throws Exception {
        try (ResultSet resultSet = statement.executeQuery(sql)) {
            if (resultSet.next()) {
                System.out.println(label + ": " + resultSet.getLong(1));
            }
        }
    }

    private static void consumeResults(Statement statement) throws Exception {
        while (true) {
            if (statement.getMoreResults()) {
                try (ResultSet ignored = statement.getResultSet()) {
                    // consume and close result sets
                }
                continue;
            }
            if (statement.getUpdateCount() == -1) {
                break;
            }
        }
    }
}
