package com.example.gestorcartera;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PersistenceService {

    private final String archivoDatos;
    private final String archivoHistorico;

    public PersistenceService(String archivoDatos, String archivoHistorico) {
        this.archivoDatos     = archivoDatos;
        this.archivoHistorico = archivoHistorico;
    }

    /**
     * Carga los activos desde el CSV. Si el archivo no existe, crea una
     * cartera de ejemplo y la persiste antes de devolver la lista.
     */
    public List<Activo> cargar() {
        List<Activo> resultado = new ArrayList<>();
        File archivo = new File(archivoDatos);
        if (!archivo.exists()) {
            resultado.add(new Activo("MSCI World", "ETF",    3000, 3450));
            resultado.add(new Activo("Bitcoin",    "Crypto", 1000, 1340));
            resultado.add(new Activo("S&P 500",    "ETF",    2000, 2180));
            guardar(resultado);
        } else {
            try {
                for (String linea : Files.readAllLines(Path.of(archivoDatos))) {
                    String[] p = linea.split(";");
                    if (p.length >= 4) {
                        String fecha      = p.length >= 5 ? p[4] : LocalDate.now().toString();
                        String plat       = p.length >= 6 ? p[5] : "";
                        String notas      = p.length >= 7 ? p[6] : "";
                        String registrado = p.length >= 8 ? p[7] : (fecha + "T00:00:00");
                        resultado.add(new Activo(p[0], p[1],
                                Double.parseDouble(p[2].replace(",", ".")),
                                Double.parseDouble(p[3].replace(",", ".")),
                                fecha, plat, notas, registrado));
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
        return resultado;
    }

    public void guardar(List<Activo> activos) {
        try {
            StringBuilder sb = new StringBuilder();
            for (Activo a : activos) {
                sb.append(a.getNombre()).append(";").append(a.getTipo()).append(";")
                        .append(a.getInvertido()).append(";").append(a.getValorActual()).append(";")
                        .append(a.getFecha()).append(";").append(a.getPlataforma()).append(";")
                        .append(a.getNotas()).append(";").append(a.getRegistradoEn()).append("\n");
            }
            Files.writeString(Path.of(archivoDatos), sb.toString());
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void guardarPuntoHistorico(double total) {
        try {
            String hoy = LocalDate.now().toString();
            if (Files.exists(Path.of(archivoHistorico))) {
                List<String> lineas = Files.readAllLines(Path.of(archivoHistorico));
                if (!lineas.isEmpty() && lineas.get(lineas.size() - 1).split(";")[0].equals(hoy)) return;
            }
            Files.writeString(Path.of(archivoHistorico),
                    hoy + ";" + String.format(java.util.Locale.US, "%.2f", total) + "\n",
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception e) { e.printStackTrace(); }
    }
}