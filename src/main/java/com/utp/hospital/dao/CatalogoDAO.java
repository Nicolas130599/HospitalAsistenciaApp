package com.utp.hospital.dao;

import com.utp.hospital.conexion.Conexion;
import com.utp.hospital.model.TipoPermiso;
import com.utp.hospital.model.Turno;
import jakarta.enterprise.context.ApplicationScoped;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Listas de apoyo para los formularios y filtros: turnos, tipos de permiso y cargos. */
@ApplicationScoped
public class CatalogoDAO {

    public List<Turno> listarTurnos() throws SQLException {
        List<Turno> lista = new ArrayList<>();
        // MIN(id_turno) agrupa por nombre, así un script ejecutado dos veces no duplica opciones.
        String sql = "SELECT MIN(id_turno) AS id_turno, nombre, MIN(hora_inicio) AS hora_inicio, "
                + "MIN(hora_fin) AS hora_fin FROM turno GROUP BY nombre ORDER BY MIN(id_turno)";
        try (Connection con = Conexion.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Turno(rs.getInt("id_turno"), rs.getString("nombre"),
                        rs.getTime("hora_inicio").toLocalTime(), rs.getTime("hora_fin").toLocalTime()));
            }
        }
        return lista;
    }

    public List<TipoPermiso> listarTiposPermiso() throws SQLException {
        List<TipoPermiso> lista = new ArrayList<>();
        String sql = "SELECT MIN(id_tipo) AS id_tipo, nombre FROM tipo_permiso GROUP BY nombre ORDER BY MIN(id_tipo)";
        try (Connection con = Conexion.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new TipoPermiso(rs.getInt("id_tipo"), rs.getString("nombre")));
            }
        }
        return lista;
    }

    /** Cargos que existen realmente en la tabla personal (para el filtro del listado). */
    public List<String> listarCargos() throws SQLException {
        List<String> lista = new ArrayList<>();
        try (Connection con = Conexion.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT DISTINCT cargo FROM personal ORDER BY cargo")) {
            while (rs.next()) {
                lista.add(rs.getString(1));
            }
        }
        return lista;
    }
}
