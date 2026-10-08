package com.utp.hospital.bean;

import com.utp.hospital.dao.AsistenciaDAO;
import com.utp.hospital.model.Asistencia;
import com.utp.hospital.model.FiltroAsistencia;
import com.utp.hospital.model.Indicador;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named("dashboardBean")
@RequestScoped
public class DashboardBean {

    private static final Logger LOG = Logger.getLogger(DashboardBean.class.getName());

    @Inject
    private AsistenciaDAO asistenciaDAO;

    private int total;
    private int hoy;
    private int permisos;
    private int personalActivo;
    private List<Indicador> porTurno = new ArrayList<>();
    private List<Indicador> porEstado = new ArrayList<>();
    private List<Asistencia> ultimos = new ArrayList<>();

    @PostConstruct
    public void cargar() {
        try {
            total = asistenciaDAO.contarTotal();
            hoy = asistenciaDAO.contarHoy();
            permisos = asistenciaDAO.contarPermisos();
            personalActivo = asistenciaDAO.contarPersonalActivo();
            porTurno = asistenciaDAO.contarPorTurno();
            porEstado = asistenciaDAO.contarPorEstado();
            ultimos = asistenciaDAO.listar(new FiltroAsistencia(), 0, 5);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "No se pudo cargar el dashboard", e);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, "No se pudo leer la base de datos.", null));
        }
    }

    public int getTotal() { return total; }
    public int getHoy() { return hoy; }
    public int getPermisos() { return permisos; }
    public int getPersonalActivo() { return personalActivo; }
    public List<Indicador> getPorTurno() { return porTurno; }
    public List<Indicador> getPorEstado() { return porEstado; }
    public List<Asistencia> getUltimos() { return ultimos; }
}
