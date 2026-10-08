package com.utp.hospital.dao;

import com.utp.hospital.conexion.Conexion;
import com.utp.hospital.model.Personal;
import jakarta.enterprise.context.ApplicationScoped;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class PersonalDAO {

    private static final String COLUMNAS = "SELECT id_personal, dni, nombre, cargo, id_turno FROM personal";

    public Personal buscarPorDni(String dni) throws SQLException {
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(COLUMNAS + " WHERE dni = ?")) {
            ps.setString(1, dni);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public List<Personal> listarActivos() throws SQLException {
        List<Personal> lista = new ArrayList<>();
        try (Connection con = Conexion.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(COLUMNAS + " WHERE activo = TRUE ORDER BY nombre")) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    private Personal mapear(ResultSet rs) throws SQLException {
        Personal p = new Personal();
        p.setIdPersonal(rs.getInt("id_personal"));
        p.setDni(rs.getString("dni"));
        p.setNombre(rs.getString("nombre"));
        p.setCargo(rs.getString("cargo"));
        int turno = rs.getInt("id_turno");
        p.setIdTurno(rs.wasNull() ? null : turno);
        return p;
    }
}
