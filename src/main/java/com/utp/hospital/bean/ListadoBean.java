package com.utp.hospital.bean;

import com.utp.hospital.dao.AsistenciaDAO;
import com.utp.hospital.dao.CatalogoDAO;
import com.utp.hospital.dao.PersonalDAO;
import com.utp.hospital.model.Asistencia;
import com.utp.hospital.model.FiltroAsistencia;
import com.utp.hospital.model.Personal;
import com.utp.hospital.model.TipoPermiso;
import com.utp.hospital.model.Turno;
import com.utp.hospital.model.Usuario;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named("listadoBean")
@ViewScoped
public class ListadoBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(ListadoBean.class.getName());
    private static final List<String> ESTADOS = List.of("Puntual", "Tardanza", "Permiso");

    @Inject
    private AsistenciaDAO asistenciaDAO;
    @Inject
    private PersonalDAO personalDAO;
    @Inject
    private CatalogoDAO catalogoDAO;

    private FiltroAsistencia filtro = new FiltroAsistencia();
    private String desdeTexto;   // yyyy-MM-dd, tal como lo envía <input type="date">
    private String hastaTexto;

    private List<Personal> personal = new ArrayList<>();
    private List<String> cargos = new ArrayList<>();
    private List<Turno> turnos = new ArrayList<>();
    private List<TipoPermiso> tiposPermiso = new ArrayList<>();

    private int pagina = 1;
    private int tamanoPagina = 5;
    private int totalRegistros;
    private List<Asistencia> registros = new ArrayList<>();

    @PostConstruct
    public void init() {
        try {
            personal = personalDAO.listarActivos();
            cargos = catalogoDAO.listarCargos();
            turnos = catalogoDAO.listarTurnos();
            tiposPermiso = catalogoDAO.listarTiposPermiso();
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "No se pudieron cargar las listas del filtro", e);
            error("No se pudo leer la base de datos.");
        }
        cargar();
    }

    public void buscar() {
        try {
            LocalDate desde = parsear(desdeTexto);
            LocalDate hasta = parsear(hastaTexto);
            if (desde != null && hasta != null && desde.isAfter(hasta)) {
                error("La fecha \"Desde\" no puede ser posterior a \"Hasta\"");
                return;
            }
            filtro.setDesde(desde);
            filtro.setHasta(hasta);
        } catch (DateTimeParseException e) {
            error("Las fechas deben tener el formato correcto");
            return;
        }
        pagina = 1;
        cargar();
    }

    public void cambiarTamano() {
        pagina = 1;
        cargar();
    }

    public void limpiar() {
        filtro = new FiltroAsistencia();
        desdeTexto = null;
        hastaTexto = null;
        pagina = 1;
        cargar();
    }

    public void anterior() {
        if (pagina > 1) {
            pagina--;
            cargar();
        }
    }

    public void siguiente() {
        if (pagina < getTotalPaginas()) {
            pagina++;
            cargar();
        }
    }

    public void eliminar(int idAsistencia) {
        FacesContext fc = FacesContext.getCurrentInstance();
        Object u = fc.getExternalContext().getSessionMap().get(LoginBean.SESION_USUARIO);
        if (!(u instanceof Usuario) || !((Usuario) u).isAdmin()) {
            error("No tiene permisos para eliminar registros");
            return;
        }
        try {
            asistenciaDAO.eliminar(idAsistencia);
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Registro eliminado", null));
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "No se pudo eliminar la asistencia " + idAsistencia, e);
            error("No se pudo eliminar el registro");
        }
        cargar();
    }

    private void cargar() {
        try {
            totalRegistros = asistenciaDAO.contar(filtro);
            int totalPaginas = getTotalPaginas();
            if (pagina > totalPaginas) {
                pagina = totalPaginas;
            }
            registros = asistenciaDAO.listar(filtro, (pagina - 1) * tamanoPagina, tamanoPagina);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "No se pudo cargar el listado", e);
            registros = new ArrayList<>();
            totalRegistros = 0;
            error("No se pudo leer la base de datos.");
        }
    }

    private LocalDate parsear(String texto) {
        return (texto == null || texto.isBlank()) ? null : LocalDate.parse(texto.trim());
    }

    private void error(String mensaje) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, mensaje, null));
    }

    public int getTotalPaginas() {
        return Math.max(1, (int) Math.ceil(totalRegistros / (double) tamanoPagina));
    }

    public int getDesde() {
        return totalRegistros == 0 ? 0 : (pagina - 1) * tamanoPagina + 1;
    }

    public int getHasta() {
        return Math.min(pagina * tamanoPagina, totalRegistros);
    }

    public FiltroAsistencia getFiltro() { return filtro; }
    public String getDesdeTexto() { return desdeTexto; }
    public void setDesdeTexto(String desdeTexto) { this.desdeTexto = desdeTexto; }
    public String getHastaTexto() { return hastaTexto; }
    public void setHastaTexto(String hastaTexto) { this.hastaTexto = hastaTexto; }
    public List<Personal> getPersonal() { return personal; }
    public List<String> getCargos() { return cargos; }
    public List<Turno> getTurnos() { return turnos; }
    public List<TipoPermiso> getTiposPermiso() { return tiposPermiso; }
    public List<String> getEstados() { return ESTADOS; }
    public int getPagina() { return pagina; }
    public int getTamanoPagina() { return tamanoPagina; }
    public void setTamanoPagina(int tamanoPagina) { this.tamanoPagina = tamanoPagina; }
    public int getTotalRegistros() { return totalRegistros; }
    public List<Asistencia> getRegistros() { return registros; }
}
