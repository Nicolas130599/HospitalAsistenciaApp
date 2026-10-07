package com.utp.hospital.filter;

import com.utp.hospital.bean.LoginBean;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Filtro de autenticación: solo deja pasar a las páginas .xhtml si hay un
 * usuario en sesión. login.xhtml y los recursos de Faces (CSS, imágenes) son públicos.
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = {"*.xhtml"})
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String ruta = req.getRequestURI().substring(req.getContextPath().length());
        boolean esRecurso = ruta.startsWith("/jakarta.faces.resource/");
        boolean esLogin = ruta.equals("/login.xhtml");

        HttpSession sesion = req.getSession(false);
        boolean autenticado = sesion != null && sesion.getAttribute(LoginBean.SESION_USUARIO) != null;

        if (esRecurso) {
            chain.doFilter(request, response);
            return;
        }

        if (autenticado) {
            // Evita que el botón "Atrás" muestre páginas privadas tras cerrar sesión.
            res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            res.setHeader("Pragma", "no-cache");
            res.setDateHeader("Expires", 0);
            if (esLogin) {
                res.sendRedirect(req.getContextPath() + "/dashboard.xhtml");
            } else {
                chain.doFilter(request, response);
            }
            return;
        }

        if (esLogin) {
            chain.doFilter(request, response);
            return;
        }

        String destino = req.getContextPath() + "/login.xhtml";
        if ("partial/ajax".equals(req.getHeader("Faces-Request"))) {
            // Petición AJAX de JSF con la sesión vencida: se pide al navegador ir al login.
            res.setContentType("text/xml;charset=UTF-8");
            res.getWriter().printf("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<partial-response><redirect url=\"%s\"/></partial-response>", destino);
        } else {
            res.sendRedirect(destino);
        }
    }
}
