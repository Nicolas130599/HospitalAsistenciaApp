package com.utp.hospital.dao;

import com.utp.hospital.conexion.Conexion;
import com.utp.hospital.model.Asistencia;
import com.utp.hospital.model.FiltroAsistencia;
import com.utp.hospital.model.Indicador;
import com.utp.hospital.model.SolicitudRegistro;
import jakarta.enterprise.context.ApplicationScoped;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de asistencias: listado con filtros combinados, conteos del dashboard,
 * eliminación y registro transaccional (personal + asistencia + permiso + documento).
 */
@ApplicationScoped
public class AsistenciaDAO {

    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    /** Minutos de gracia después de la hora de inicio del turno. */
    private static final int TOLERANCIA_MIN = 10;
    /** Pasada esta ventana (8 h) ya no se considera una entrada tardía del mismo turno. */
    private static final int VENTANA_TARDANZA_MIN = 480;

    private static final String SELECT
            = "SELECT a.id_asistencia, per.id_personal, per.dni, per.nombre, per.cargo, "
            + "t.nombre AS turno, a.fecha, a.hora_entrada, a.hora_salida, a.estado, "
            + "p.id_permiso, tp.nombre AS tipo_permiso, p.dias_totales, p.fecha_inicio, p.fecha_fin, "
            + "COALESCE(p.motivo, a.observacion) AS observacion, doc.nombre_archivo AS documento ";

    private static final String FROM
            = "FROM asistencia a "
            + "JOIN personal per ON per.id_personal = a.id_personal "
            + "LEFT JOIN turno t ON t.id_turno = per.id_turno "
            + "LEFT JOIN permiso p ON p.id_asistencia = a.id_asistencia "
            + "LEFT JOIN tipo_permiso tp ON tp.id_tipo = p.id_tipo "
            + "LEFT JOIN documento doc ON doc.id_permiso = p.id_permiso ";

    // ------------------------------------------------------------------
    // Listado con filtros
    // ------------------------------------------------------------------

    /** Devuelve una página de resultados que cumple TODOS los filtros activos. */
    public List<Asistencia> listar(FiltroAsistencia filtro, int offset, int limit) throws SQLException {
        List<Object> params = new ArrayList<>();
        String sql = SELECT + FROM + where(filtro, params)
                + "ORDER BY a.fecha DESC, a.hora_entrada DESC, a.id_asistencia DESC LIMIT ? OFFSET ?";
        List<Asistencia> lista = new ArrayList<>();
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            int i = bind(ps, params);
            ps.setInt(i++, limit);
            ps.setInt(i, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    /** Cuenta con los mismos JOIN y filtros que listar(), para que la paginación sea exacta. */
    public int contar(FiltroAsistencia filtro) throws SQLException {
        List<Object> params = new ArrayList<>();
        String sql = "SELECT COUNT(*) " + FROM + where(filtro, params);
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /** Construye el WHERE: cada filtro presente agrega una condición AND con su parámetro. */
    private String where(FiltroAsistencia f, List<Object> params) {
        StringBuilder sb = new StringBuilder("WHERE 1 = 1 ");
        if (f == null) {
            return sb.toString();
        }
        if (f.getIdPersonal() != null) {
            sb.append("AND a.id_personal = ? ");
            params.add(f.getIdPersonal());
        }
        if (hayTexto(f.getTexto())) {
            sb.append("AND (per.dni LIKE ? OR per.nombre LIKE ?) ");
            String like = "%" + f.getTexto().trim() + "%";
            params.add(like);
            params.add(like);
        }
        if (hayTexto(f.getCargo())) {
            sb.append("AND per.cargo = ? ");
            params.add(f.getCargo());
        }
        if (hayTexto(f.getTurno())) {
            sb.append("AND t.nombre = ? ");
            params.add(f.getTurno());
        }
        if (hayTexto(f.getEstado())) {
            sb.append("AND a.estado = ? ");
            params.add(f.getEstado());
        }
        if ("Si".equals(f.getPermiso())) {
            sb.append("AND p.id_permiso IS NOT NULL ");
        } else if ("No".equals(f.getPermiso())) {
            sb.append("AND p.id_permiso IS NULL ");
        }
        if (hayTexto(f.getTipoPermiso())) {
            sb.append("AND tp.nombre = ? ");
            params.add(f.getTipoPermiso());
        }
        if (f.getDesde() != null) {
            sb.append("AND a.fecha >= ? ");
            params.add(Date.valueOf(f.getDesde()));
        }
        if (f.getHasta() != null) {
            sb.append("AND a.fecha <= ? ");
            params.add(Date.valueOf(f.getHasta()));
        }
        return sb.toString();
    }

    private boolean hayTexto(String s) {
        return s != null && !s.isBlank();
    }

    /** Asigna los parámetros en orden y devuelve el siguiente índice libre. */
    private int bind(PreparedStatement ps, List<Object> params) throws SQLException {
        int i = 1;
        for (Object p : params) {
            ps.setObject(i++, p);
        }
        return i;
    }

    private Asistencia mapear(ResultSet rs) throws SQLException {
        Asistencia a = new Asistencia();
        a.setIdAsistencia(rs.getInt("id_asistencia"));
        a.setIdPersonal(rs.getInt("id_personal"));
        a.setDni(rs.getString("dni"));
        a.setNombre(rs.getString("nombre"));
        a.setCargo(rs.getString("cargo"));
        a.setTurno(rs.getString("turno"));
        Date fecha = rs.getDate("fecha");
        a.setFecha(fecha == null ? null : fecha.toLocalDate());
        Time entrada = rs.getTime("hora_entrada");
        a.setHoraEntrada(entrada == null ? null : entrada.toLocalTime());
        Time salida = rs.getTime("hora_salida");
        a.setHoraSalida(salida == null ? null : salida.toLocalTime());
        a.setEstado(rs.getString("estado"));
        rs.getInt("id_permiso");
        a.setConPermiso(!rs.wasNull());
        a.setTipoPermiso(rs.getString("tipo_permiso"));
        a.setDias(rs.getInt("dias_totales"));
        Date desde = rs.getDate("fecha_inicio");
        a.setPermisoDesde(desde == null ? null : desde.toLocalDate());
        Date hasta = rs.getDate("fecha_fin");
        a.setPermisoHasta(hasta == null ? null : hasta.toLocalDate());
        a.setObservacion(rs.getString("observacion"));
        a.setDocumento(rs.getString("documento"));
        return a;
    }

    // ------------------------------------------------------------------
    // Eliminar
    // ------------------------------------------------------------------

    /** Elimina la asistencia; sus permisos y documentos se borran por ON DELETE CASCADE. */
    public void eliminar(int idAsistencia) throws SQLException {
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM asistencia WHERE id_asistencia = ?")) {
            ps.setInt(1, idAsistencia);
            ps.executeUpdate();
        }
    }

    // ------------------------------------------------------------------
    // Registro (transacción)
    // ------------------------------------------------------------------

    /**
     * Registra la asistencia de hoy. Si el DNI no existe crea al colaborador en personal.
     * Si pidió permiso, crea también el permiso (y el documento adjunto).
     * Todo ocurre en una transacción: o se guarda completo o no se guarda nada.
     *
     * @return el estado calculado (Puntual, Tardanza o Permiso)
     */
    public String registrar(SolicitudRegistro s) throws SQLException, NegocioException {
        LocalDate hoy = LocalDate.now(LIMA);
        LocalTime ahora = LocalTime.now(LIMA).withNano(0);

        try (Connection con = Conexion.getConnection()) {
            con.setAutoCommit(false);
            try {
                int idPersonal;
                Integer idTurno;

                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT id_personal, id_turno FROM personal WHERE dni = ?")) {
                    ps.setString(1, s.getDni());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            idPersonal = rs.getInt("id_personal");
                            int t = rs.getInt("id_turno");
                            idTurno = rs.wasNull() ? null : t;
                        } else {
                            if (s.getIdTurno() == null) {
                                throw new NegocioException("Seleccione el turno del nuevo colaborador");
                            }
                            idTurno = s.getIdTurno();
                            idPersonal = insertarPersonal(con, s);
                        }
                    }
                }

                LocalTime inicioTurno = horaInicio(con, idTurno);

                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT 1 FROM asistencia WHERE id_personal = ? AND fecha = ?")) {
                    ps.setInt(1, idPersonal);
                    ps.setDate(2, Date.valueOf(hoy));
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            throw new NegocioException("Este colaborador ya tiene su asistencia registrada hoy");
                        }
                    }
                }

                String estado = s.tienePermiso() ? "Permiso" : calcularEstado(ahora, inicioTurno);
                int idAsistencia = insertarAsistencia(con, idPersonal, hoy, ahora, estado);

                if (s.tienePermiso()) {
                    int idPermiso = insertarPermiso(con, s, idPersonal, hoy, idAsistencia);
                    if (s.getRutaArchivo() != null) {
                        insertarDocumento(con, idPermiso, s);
                    }
                }
                con.commit();
                return estado;
            } catch (SQLException | NegocioException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    private int insertarPersonal(Connection con, SolicitudRegistro s) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO personal (dni, nombre, cargo, id_turno, activo) VALUES (?, ?, ?, ?, TRUE)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getDni());
            ps.setString(2, s.getNombre());
            ps.setString(3, s.getCargo());
            ps.setInt(4, s.getIdTurno());
            ps.executeUpdate();
            return claveGenerada(ps);
        }
    }

    private LocalTime horaInicio(Connection con, Integer idTurno) throws SQLException {
        if (idTurno == null) {
            return null;
        }
        try (PreparedStatement ps = con.prepareStatement("SELECT hora_inicio FROM turno WHERE id_turno = ?")) {
            ps.setInt(1, idTurno);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getTime(1).toLocalTime() : null;
            }
        }
    }

    private int insertarAsistencia(Connection con, int idPersonal, LocalDate fecha, LocalTime hora, String estado)
            throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO asistencia (id_personal, fecha, hora_entrada, estado) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idPersonal);
            ps.setDate(2, Date.valueOf(fecha));
            ps.setTime(3, Time.valueOf(hora));
            ps.setString(4, estado);
            ps.executeUpdate();
            return claveGenerada(ps);
        }
    }

    private int insertarPermiso(Connection con, SolicitudRegistro s, int idPersonal, LocalDate inicio,
            int idAsistencia) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO permiso (id_personal, id_tipo, fecha_inicio, fecha_fin, dias_totales, motivo, "
                + "estado, id_asistencia) VALUES (?, ?, ?, ?, ?, ?, 'APROBADO', ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idPersonal);
            ps.setInt(2, s.getIdTipoPermiso());
            ps.setDate(3, Date.valueOf(inicio));
            ps.setDate(4, Date.valueOf(inicio.plusDays(Math.max(1, s.getDias()) - 1L)));
            ps.setInt(5, Math.max(1, s.getDias()));
            ps.setString(6, s.getMotivo());
            ps.setInt(7, idAsistencia);
            ps.executeUpdate();
            return claveGenerada(ps);
        }
    }

    private void insertarDocumento(Connection con, int idPermiso, SolicitudRegistro s) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO documento (id_permiso, nombre_archivo, ruta_archivo) VALUES (?, ?, ?)")) {
            ps.setInt(1, idPermiso);
            ps.setString(2, s.getNombreArchivo());
            ps.setString(3, s.getRutaArchivo());
            ps.executeUpdate();
        }
    }

    private int claveGenerada(PreparedStatement ps) throws SQLException {
        try (ResultSet keys = ps.getGeneratedKeys()) {
            if (keys.next()) {
                return keys.getInt(1);
            }
        }
        throw new SQLException("La base de datos no devolvió el id generado");
    }

    /**
     * Puntual si entra hasta TOLERANCIA_MIN minutos después del inicio del turno (o antes);
     * Tardanza si llega más tarde dentro de la ventana del turno. Maneja turnos nocturnos.
     */
    static String calcularEstado(LocalTime ahora, LocalTime inicioTurno) {
        if (inicioTurno == null) {
            return "Puntual";
        }
        int minutos = Math.floorMod(ahora.toSecondOfDay() / 60 - inicioTurno.toSecondOfDay() / 60, 1440);
        return (minutos > TOLERANCIA_MIN && minutos < VENTANA_TARDANZA_MIN) ? "Tardanza" : "Puntual";
    }

    // ------------------------------------------------------------------
    // Indicadores del dashboard
    // ------------------------------------------------------------------

    public int contarTotal() throws SQLException {
        return escalar("SELECT COUNT(*) FROM asistencia", null);
    }

    public int contarHoy() throws SQLException {
        return escalar("SELECT COUNT(*) FROM asistencia WHERE fecha = ?", Date.valueOf(LocalDate.now(LIMA)));
    }

    public int contarPermisos() throws SQLException {
        return escalar("SELECT COUNT(*) FROM permiso", null);
    }

    public int contarPersonalActivo() throws SQLException {
        return escalar("SELECT COUNT(*) FROM personal WHERE activo = TRUE", null);
    }

    /** Asistencias acumuladas por turno (según el turno asignado al colaborador). */
    public List<Indicador> contarPorTurno() throws SQLException {
        return agrupar("SELECT t.nombre, COUNT(a.id_asistencia) FROM turno t "
                + "LEFT JOIN personal per ON per.id_turno = t.id_turno "
                + "LEFT JOIN asistencia a ON a.id_personal = per.id_personal "
                + "GROUP BY t.nombre ORDER BY MIN(t.id_turno)");
    }

    public List<Indicador> contarPorEstado() throws SQLException {
        return agrupar("SELECT estado, COUNT(*) FROM asistencia GROUP BY estado ORDER BY estado");
    }

    private List<Indicador> agrupar(String sql) throws SQLException {
        List<Indicador> lista = new ArrayList<>();
        try (Connection con = Conexion.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Indicador(rs.getString(1), rs.getInt(2)));
            }
        }
        return lista;
    }

    private int escalar(String sql, Object parametro) throws SQLException {
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (parametro != null) {
                ps.setObject(1, parametro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}
