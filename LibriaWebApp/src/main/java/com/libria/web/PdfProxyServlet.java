package com.libria.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@WebServlet("/pdfproxy")
public class PdfProxyServlet extends HttpServlet {

    // Même base que dans LibriaWebAppService
    private static final String SERVICE_BASE = System.getenv().getOrDefault(
            "LIBRIA_SERVICE_URL",
            "http://payara-libria-service:8080/LibriaService/api"
    );

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String file = req.getParameter("file");
        if (file == null || file.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Paramètre 'file' manquant");
            return;
        }

        // URL interne vers LibriaService (dans Docker)
        String serviceUrl = SERVICE_BASE + "/files/pdf/" + file;
        System.out.println("[PdfProxyServlet] proxy -> " + serviceUrl);

        HttpURLConnection conn = null;
        try {
            URL url = new URL(serviceUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int status = conn.getResponseCode();
            if (status != 200) {
                String errBody = "";
                try (InputStream err = conn.getErrorStream()) {
                    if (err != null) {
                        errBody = new String(err.readAllBytes(), StandardCharsets.UTF_8);
                    }
                }
                System.out.println("[PdfProxyServlet] status=" + status + " body=" + errBody);
                resp.sendError(status);
                return;
            }

            resp.setContentType("application/pdf");
            resp.setHeader("Content-Disposition", "inline; filename=\"" + file + "\"");

            try (InputStream in = conn.getInputStream();
                 OutputStream out = resp.getOutputStream()) {
                in.transferTo(out);
            }

        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erreur proxy PDF");
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}