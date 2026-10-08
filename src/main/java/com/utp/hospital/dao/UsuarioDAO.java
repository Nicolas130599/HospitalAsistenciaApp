package com.utp.hospital.dao;

import com.utp.hospital.conexion.Conexion;
import com.utp.hospital.model.Usuario;
import jakarta.enterprise.context.ApplicationScoped;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HexFormat;

@ApplicationScoped
public class UsuarioDAO {

    private static final String SQL_POR_USERNAME
            = "SELECT id_usuario, username, password, rol FROM usuario WHERE username = ?";


    public Usuario autenticar(String username, String password) throws SQLException {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            return null;
        }
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_POR_USERNAME)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String guardada = rs.getString("password");
                    if (coincide(password, guardada)) {
                        return new Usuario(rs.getInt("id_usuario"), rs.getString("username"), rs.getString("rol"));
                    }
                }
            }
        }
        return null;
    }

    private boolean coincide(String ingresada, String guardada) {
        if (guardada == null) {
            return false;
        }
        return guardada.equals(ingresada) || guardada.equalsIgnoreCase(sha256(ingresada));
    }

    private String sha256(String texto) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(texto.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
