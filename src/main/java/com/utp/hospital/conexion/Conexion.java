package com.utp.hospital.conexion;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


public final class Conexion {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = Conexion.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                PROPS.load(in);
            }
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private Conexion() {
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("No se encontró el driver de MySQL", e);
        }
        String url = PROPS.getProperty("db.url",
                "jdbc:mysql://localhost:3307/hospital_asistencia?useSSL=false&allowPublicKeyRetrieval=true"
                + "&serverTimezone=America/Lima&characterEncoding=UTF-8");
        String user = PROPS.getProperty("db.user", "root");
        String password = PROPS.getProperty("db.password", "");
        return DriverManager.getConnection(url, user, password);
    }
}
