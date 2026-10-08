package com.utp.hospital.bean;

import com.utp.hospital.dao.UsuarioDAO;
import com.utp.hospital.model.Usuario;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;


@Named("loginBean")
@SessionScoped
public class LoginBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(LoginBean.class.getName());

    public static final String SESION_USUARIO = "usuario";

    @Inject
    private UsuarioDAO usuarioDAO;

    private String username;
    private String password;
    private Usuario usuario;

    public String login() {
        FacesContext fc = FacesContext.getCurrentInstance();
        try {
            Usuario u = usuarioDAO.autenticar(username == null ? null : username.trim(), password);
            password = null;
            if (u == null) {
                fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Usuario o contraseña incorrectos", null));
                return null;
            }
            HttpServletRequest req = (HttpServletRequest) fc.getExternalContext().getRequest();
            req.changeSessionId(); // evita fijación de sesión
            req.getSession().setAttribute(SESION_USUARIO, u);
            this.usuario = u;
            return "exito";
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error al autenticar", e);
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "No se pudo conectar con la base de datos. Revise MySQL y db.properties.", null));
            return null;
        }
    }

    public String logout() {
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.invalidateSession();
        return "cerrarSesion";
    }

    public boolean isAdmin() {
        return usuario != null && usuario.isAdmin();
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Usuario getUsuario() { return usuario; }
}
