package com.utp.hospital.bean;

import com.utp.hospital.dao.AsistenciaDAO;
import com.utp.hospital.dao.CatalogoDAO;
import com.utp.hospital.dao.NegocioException;
import com.utp.hospital.dao.PersonalDAO;
import com.utp.hospital.model.Personal;
import com.utp.hospital.model.SolicitudRegistro;
import com.utp.hospital.model.TipoPermiso;
import com.utp.hospital.model.Turno;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.ValidatorException;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Managed Bean del formulario de registro de asistencia.
 * Alcance @ViewScoped: conserva los datos mientras el usuario permanece en
 * registro.xhtml (necesario porque hay peticiones AJAX: buscar DNI y mostrar el permiso).
 * EL en la vista: #{asistenciaBean.dni}, #{asistenciaBean.registrar}.
 */
@Named("asistenciaBean")
@ViewScoped
public class AsistenciaBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(AsistenciaBean.class.getName());
    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final List<String> TIPOS_ARCHIVO = List.of("application/pdf", "image/jpeg", "image/png");
    private static final List<String> CARGOS = List.of("Médico", "Enfermera", "Técnico", "Auxiliar", "Administrativo");

    @Inject
    private AsistenciaDAO asistenciaDAO;
    @Inject
    private PersonalDAO personalDAO;
    @Inject
    private CatalogoDAO catalogoDAO;

    private List<Turno> turnos = new ArrayList<>();
    private List<TipoPermiso> tiposPermiso = new ArrayList<>();

    private String dni;
    private String nombre;
    private String cargo;
    private Integer idTurno;
    private boolean personalExistente;

    private boolean permisoSolicitado;
    private Integer idTipoPermiso;
    private Integer diasPermiso = 1;
    private String motivo;
    private transient Part documento; // Part no es serializable

    /** Carga los catálogos y preselecciona el turno que corresponde a la hora actual. */
    @PostConstruct
    public void init() {
        try {
            turnos = catalogoDAO.listarTurnos();
            tiposPermiso = catalogoDAO.listarTiposPermiso();
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "No se pudieron cargar turnos y tipos de permiso", e);
            mensaje(FacesMessage.SEVERITY_ERROR, "No se pudo leer la base de datos.");
        }
        LocalTime ahora = LocalTime.now(LIMA);
        for (Turno t : turnos) {
            if (t.contiene(ahora)) {
                idTurno = t.getIdTurno();
                break;
            }
        }
        if (idTurno == null && !turnos.isEmpty()) {
            idTurno = turnos.get(0).getIdTurno();
        }
        if (!tiposPermiso.isEmpty()) {
            idTipoPermiso = tiposPermiso.get(0).getIdTipo();
        }
    }

    /**
     * Listener AJAX del campo DNI: si el colaborador ya existe, completa nombre, cargo y turno
     * y bloquea esos campos; si no existe, se podrá registrar como colaborador nuevo.
     */
    public void buscarPersonal() {
        personalExistente = false;
        if (dni == null || !dni.matches("\\d{8}")) {
            return;
        }
        try {
            Personal p = personalDAO.buscarPorDni(dni);
            if (p != null) {
                nombre = p.getNombre();
                cargo = p.getCargo();
                if (p.getIdTurno() != null) {
                    idTurno = p.getIdTurno();
                }
                personalExistente = true;
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "No se pudo buscar el DNI " + dni, e);
            mensaje(FacesMessage.SEVERITY_ERROR, "No se pudo consultar la base de datos.");
        }
    }

    /** Valida el archivo adjunto: máximo 5 MB y solo PDF, JPG o PNG. */
    public void validarArchivo(FacesContext ctx, UIComponent componente, Object valor) {
        Part part = (Part) valor;
        if (part == null || part.getSize() == 0) {
            return; // el adjunto es opcional
        }
        if (part.getSize() > MAX_BYTES) {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "El archivo supera el máximo de 5 MB", null));
        }
        if (!TIPOS_ARCHIVO.contains(part.getContentType())) {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Formato no permitido. Suba un PDF, JPG o PNG", null));
        }
    }

    /** Acción del botón "Registrar asistencia". */
    public String registrar() {
        FacesContext fc = FacesContext.getCurrentInstance();
        String rutaGuardada = null;
        try {
            SolicitudRegistro s = new SolicitudRegistro();
            s.setDni(dni.trim());
            s.setNombre(nombre == null ? "" : nombre.trim());
            s.setCargo(cargo);
            s.setIdTurno(idTurno);
            if (permisoSolicitado) {
                s.setIdTipoPermiso(idTipoPermiso);
                s.setDias(diasPermiso == null ? 1 : diasPermiso);
                s.setMotivo(motivo == null || motivo.isBlank() ? null : motivo.trim());
                if (documento != null && documento.getSize() > 0) {
                    s.setNombreArchivo(nombreOriginal(documento));
                    rutaGuardada = guardarArchivo(documento, s.getNombreArchivo());
                    s.setRutaArchivo(rutaGuardada);
                }
            }
            String estado = asistenciaDAO.registrar(s);

            // Mantiene el mensaje después de la redirección (que además limpia el formulario).
            fc.getExternalContext().getFlash().setKeepMessages(true);
            mensaje(FacesMessage.SEVERITY_INFO, "Asistencia registrada: " + s.getNombre() + " (" + estado + ")");
            return "registro?faces-redirect=true";
        } catch (NegocioException e) {
            borrarArchivo(rutaGuardada);
            mensaje(FacesMessage.SEVERITY_ERROR, e.getMessage());
            return null;
        } catch (SQLException | IOException e) {
            borrarArchivo(rutaGuardada);
            LOG.log(Level.SEVERE, "No se pudo registrar la asistencia", e);
            mensaje(FacesMessage.SEVERITY_ERROR, "No se pudo guardar el registro. Intente nuevamente.");
            return null;
        }
    }

    private String nombreOriginal(Part part) {
        return Paths.get(part.getSubmittedFileName()).getFileName().toString()
                .replaceAll("[^A-Za-z0-9._-]", "_");
    }

    /** Guarda el adjunto en ~/hospital_uploads y devuelve la ruta completa donde quedó. */
    private String guardarArchivo(Part part, String original) throws IOException {
        Path carpeta = Paths.get(System.getProperty("user.home"), "hospital_uploads");
        Files.createDirectories(carpeta);
        String guardado = UUID.randomUUID().toString().substring(0, 8) + "_" + original;
        Path destino = carpeta.resolve(guardado);
        try (InputStream in = part.getInputStream()) {
            Files.copy(in, destino, StandardCopyOption.REPLACE_EXISTING);
        }
        return destino.toString();
    }

    /** Si la base de datos rechazó el registro, no dejamos el adjunto huérfano en disco. */
    private void borrarArchivo(String ruta) {
        if (ruta == null) {
            return;
        }
        try {
            Files.deleteIfExists(Paths.get(ruta));
        } catch (IOException e) {
            LOG.log(Level.WARNING, "No se pudo borrar el adjunto " + ruta, e);
        }
    }

    private void mensaje(FacesMessage.Severity severidad, String texto) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severidad, texto, null));
    }

    public List<Turno> getTurnos() { return turnos; }
    public List<TipoPermiso> getTiposPermiso() { return tiposPermiso; }
    public List<String> getCargos() { return CARGOS; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }
    public Integer getIdTurno() { return idTurno; }
    public void setIdTurno(Integer idTurno) { this.idTurno = idTurno; }
    public boolean isPersonalExistente() { return personalExistente; }
    public boolean isPermisoSolicitado() { return permisoSolicitado; }
    public void setPermisoSolicitado(boolean permisoSolicitado) { this.permisoSolicitado = permisoSolicitado; }
    public Integer getIdTipoPermiso() { return idTipoPermiso; }
    public void setIdTipoPermiso(Integer idTipoPermiso) { this.idTipoPermiso = idTipoPermiso; }
    public Integer getDiasPermiso() { return diasPermiso; }
    public void setDiasPermiso(Integer diasPermiso) { this.diasPermiso = diasPermiso; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public Part getDocumento() { return documento; }
    public void setDocumento(Part documento) { this.documento = documento; }
}
