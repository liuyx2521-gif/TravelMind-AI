import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RunSql {
    public static void main(String[] args) throws Exception {
        String url = System.getenv().getOrDefault("MYSQL_URL",
                "jdbc:mysql://localhost:3306/?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false");
        String user = System.getenv().getOrDefault("MYSQL_USER", "root");
        String password = System.getenv().getOrDefault("MYSQL_PASSWORD", "root");
        String sql = Files.readString(Path.of(args.length == 0 ? "deploy/mysql/init.sql" : args[0]));

        try (var conn = DriverManager.getConnection(url, user, password);
             var statement = conn.createStatement()) {
            for (String command : splitSql(sql)) {
                if (!command.isBlank()) {
                    statement.execute(command);
                }
            }
            verify(statement);
        }
    }

    private static List<String> splitSql(String sql) {
        List<String> commands = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;

        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if (c == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote;
            } else if (c == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
            }

            if (c == ';' && !inSingleQuote && !inDoubleQuote) {
                commands.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        if (!current.isEmpty()) {
            commands.add(current.toString().trim());
        }
        return commands;
    }

    private static void verify(Statement statement) throws Exception {
        try (var rs = statement.executeQuery("""
                SELECT table_name
                FROM information_schema.tables
                WHERE table_schema = 'travelmind_ai'
                ORDER BY table_name
                """)) {
            while (rs.next()) {
                System.out.println(rs.getString(1));
            }
        }
    }
}
