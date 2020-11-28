package ru.zakharova.elena.network.chat.server.auth;

import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import java.sql.*;
import java.util.concurrent.*;

public class DBAuthService implements AuthService {
    private final static Logger adminLogger = Logger.getLogger(DBAuthService.class);
    private static Connection conn;
    private static Statement stmt;
    private ExecutorService ex = Executors.newFixedThreadPool(1);

    @Override
    public void start() {
        adminLogger.debug("Auth service is running");
    }

    @Override
    public void stop() {
        adminLogger.debug("Auth service has stopped");
        ex.shutdown();
    }

    @Nullable
    @Override
    public String getNickByLoginPass(String loginEntry, String passEntry)  {
        String nick = null;
        Future<String> nickByLogin = ex.submit(new NickByLogin(loginEntry, passEntry));
        try {
            nick = nickByLogin.get();
        } catch (ExecutionException | InterruptedException e) {
            adminLogger.error("Failed attempt to authentication. Class Future is not working. " + e.toString());
        }
        return nick;
    }

    @Override
    public boolean isNickBusy(String newNick)  {
        boolean result = false;
        Future<Boolean> isNickBusy = ex.submit(new IsNickBusy(newNick));
        try {
            result = isNickBusy.get();
        } catch (ExecutionException | InterruptedException e) {
            adminLogger.error("Failed attempt to change nickname. Class Future is not working. " + e.toString());
        }
        return result;
    }

    @Override
    public void changeNick(String currentNick, String newNick)  {
        ex.submit(new ChangeNick(currentNick, newNick));
    }

    private static void connection() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        conn = DriverManager.getConnection("jdbc:sqlite:mainDB.db");
        stmt = conn.createStatement();
    }

    private static void disconnect() throws SQLException {
        conn.close();
    }


    private static class NickByLogin implements Callable<String>  {
        String loginEntry;
        String passEntry;

        NickByLogin(String loginEntry, String passEntry) {
            this.loginEntry = loginEntry;
            this.passEntry = passEntry;
        }

        @Override
        public String call() {
            String nick = null;
            try {
                connection();
                ResultSet rs = stmt.executeQuery("SELECT * FROM users " +
                        "WHERE login = '" + loginEntry + "' AND password = '" + passEntry + "' LIMIT 1");
                while (rs.next()) {
                    nick = rs.getString("nick");
                    rs.close();
                }
                disconnect();
            } catch (SQLException | ClassNotFoundException e) {
                adminLogger.error("Failed attempt to authentication. Database is not working. " + e.toString());

            }
            return nick;
        }
    };

    private static class IsNickBusy implements Callable<Boolean>  {
        String newNick;

        IsNickBusy(String newNick) {
            this.newNick = newNick;
        }

        @Override
        public Boolean call() {
            boolean result = false;
            try {
                connection();
                ResultSet rs = stmt.executeQuery("SELECT * FROM users " +
                        "WHERE nick = '" + newNick + "' LIMIT 1 ");
                while (rs.next()) {
                    result = true;
                    rs.close();
                }
                disconnect();
            } catch (SQLException | ClassNotFoundException e) {
                adminLogger.error("Failed attempt to change nickname. Database is not working. " + e.toString());
            }
            return result;
        }
    }

    private static class ChangeNick implements Runnable  {
        String currentNick;
        String newNick;

        ChangeNick(String currentNick, String newNick) {
            this.currentNick = currentNick;
            this.newNick = newNick;
        }

        @Override
        public void run() {
            try {
                connection();
                PreparedStatement pstmt = conn.prepareStatement("UPDATE users set nick = ? where nick = ?;");
                pstmt.setString(1, newNick);
                pstmt.setString(2, currentNick);
                pstmt.executeUpdate();
                disconnect();
            } catch (SQLException | ClassNotFoundException e) {
                adminLogger.error("Failed attempt to change nickname. Database is not working. " + e.toString());
            }

        }
    }
}
