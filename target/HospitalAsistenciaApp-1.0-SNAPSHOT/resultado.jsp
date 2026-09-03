<%@ page contentType="text/html;charset=UTF-8"
         language="java" %>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">
    <title>
        Registro Exitoso
    </title>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/estilos.css">
</head>
<body>
<div class="container resultado">
    <div class="resultado-icono">
        ✓
    </div>
    <h1>
        ¡Registro exitoso!
    </h1>
    <p class="mensaje-exito">
        ${mensaje}
    </p>
    <div class="datos">
        <div class="dato">
            <strong>
                DNI
            </strong>
            <span>
                ${dni}
            </span>
        </div>
        <div class="dato">
            <strong>
                Colaborador
            </strong>
            <span>
                ${nombre}
            </span>
        </div>
        <div class="dato">
            <strong>
                Turno
            </strong>
            <span>
                ${turno}
            </span>
        </div>
        <div class="dato">
            <strong>
                Permiso solicitado
            </strong>
            <span>
                ${permiso}
            </span>
        </div>
        <div class="dato">
            <strong>
                Tipo de permiso
            </strong>
            <span>
                ${motivo}
            </span>
        </div>
        <div class="dato">
            <strong>
                Días de permiso
            </strong>
            <span>
                ${dias}
            </span>
        </div>
        <div class="dato">
            <strong>
                Documento adjunto
            </strong>
            <span>
                <%
                    String archivo =
                            (String) request.getAttribute("archivo");
                    if (archivo != null
                            && !archivo.isEmpty()) {
                %>
                    <%= archivo %>
                <%
                    } else {
                %>
                    No adjuntado
                <%
                    }
                %>
            </span>
        </div>
    </div>
    <a
        class="btn"
        href="${pageContext.request.contextPath}/registro.jsp"
    >
        ← Registrar otra asistencia
    </a>
</div>
</body>
</html>
