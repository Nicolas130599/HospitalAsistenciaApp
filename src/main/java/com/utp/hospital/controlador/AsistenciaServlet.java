/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.utp.hospital.controlador;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@WebServlet(name = "AsistenciaServlet", urlPatterns = {"/registrarAsistencia"})
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2, 
    maxFileSize = 1024 * 1024 * 10, 
    maxRequestSize = 1024 * 1024 * 50  
)
public class AsistenciaServlet extends HttpServlet {

    private static final String URL = "jdbc:mysql://localhost:3307/hospital_db?useSSL=false&serverTimezone=UTC";
    private static final String USUARIO = "root";
    private static final String PASSWORD = "123456"; 

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        
        String dni = request.getParameter("dni");
        String nombre = request.getParameter("nombre");
        String turno = request.getParameter("turno");
        
        String permisoSalud = request.getParameter("permisoSalud");
        String motivoSalud = request.getParameter("motivoSalud");
        String diasPermisoStr = request.getParameter("diasPermiso");
        
        int diasPermiso = 0;
        if (diasPermisoStr != null && !diasPermisoStr.isEmpty()) {
            try {
                diasPermiso = Integer.parseInt(diasPermisoStr);
            } catch (NumberFormatException e) {
                diasPermiso = 0;
            }
        }
        
        Part archivoPart = request.getPart("documentoMedico");
        String nombreArchivo = "";
        if (archivoPart != null && archivoPart.getSize() > 0) {
            nombreArchivo = Paths.get(archivoPart.getSubmittedFileName()).getFileName().toString();
        }

        PrintWriter out = response.getWriter();

        String sql = "INSERT INTO asistencia (dni, nombre, turno, permiso_solicitado, tipo_permiso, dias_permiso, archivo_adjunto) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            try (Connection conn = DriverManager.getConnection(URL, USUARIO, PASSWORD);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, dni);
                pstmt.setString(2, nombre);
                pstmt.setString(3, turno);
                pstmt.setString(4, permisoSalud != null ? "Si" : "No");
                pstmt.setString(5, motivoSalud != null ? motivoSalud : "");
                pstmt.setInt(6, diasPermiso);
                pstmt.setString(7, nombreArchivo);
                
                pstmt.executeUpdate();
                
                out.println("<html><head><title>Registro Exitoso</title>");
                out.println("<style>body{font-family:Inter,sans-serif;background:#eef4f2;display:flex;justify-content:center;align-items:center;height:100vh;margin:0;}");
                out.println(".card{background:white;padding:30px;border-radius:12px;box-shadow:0 4px 15px rgba(0,0,0,0.1);text-align:center;max-width:400px;}");
                out.println("h3{color:#0a5c53;margin-bottom:15px;}p{color:#2f3e46;font-size:14px;line-height:1.5;}</style></head><body>");
                out.println("<div class='card'>");
                out.println("<h3>¡Asistencia registrada y guardada en la BD con éxito!</h3>");
                out.println("<p><b>Colaborador:</b> " + nombre + " (DNI: " + dni + ")</p>");
                out.println("<p><b>Turno:</b> " + turno + "</p>");
                if (permisoSalud != null) {
                    out.println("<p><b>Permiso:</b> " + motivoSalud + " (" + diasPermiso + " días)</p>");
                    if (!nombreArchivo.isEmpty()) {
                        out.println("<p><b>Archivo adjunto:</b> " + nombreArchivo + "</p>");
                    }
                }
                out.println("<br><a href='registro.html' style='color:#0a5c53;text-decoration:none;font-weight:600;'>&larr; Volver al Registro</a>");
                out.println("</div></body></html>");
            }
            
        } catch (Exception e) {
            out.println("<h3 style='color:red;'>Error al guardar en la Base de Datos: " + e.getMessage() + "</h3>");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
}