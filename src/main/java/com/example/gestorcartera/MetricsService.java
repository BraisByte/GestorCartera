package com.example.gestorcartera;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MetricsService {

    /**
     * Calcula volatilidad anualizada, rentabilidad anualizada y ratio Sharpe
     * usando el histórico diario de gestorcartera_historico.csv.
     *
     * @return double[3]: [0]=rentAnualizada, [1]=volatilidad, [2]=sharpe
     */
    public static double[] calcular(String archivoHistorico) {
        try {
            if (!Files.exists(Path.of(archivoHistorico))) return new double[]{0, 0, 0};
            List<String> lineas = Files.readAllLines(Path.of(archivoHistorico));
            if (lineas.size() < 2) return new double[]{0, 0, 0};

            List<Double> valores = new ArrayList<>();
            for (String linea : lineas) {
                String[] p = linea.split(";");
                if (p.length == 2) {
                    try { valores.add(Double.parseDouble(p[1].replace(",", "."))); }
                    catch (NumberFormatException ignored) {}
                }
            }
            if (valores.size() < 2) return new double[]{0, 0, 0};

            List<Double> retornos = new ArrayList<>();
            for (int i = 1; i < valores.size(); i++) {
                double prev = valores.get(i - 1);
                if (prev > 0) retornos.add((valores.get(i) - prev) / prev);
            }
            if (retornos.size() < 5) return new double[]{0, 0, 0};

            double mediaRet = retornos.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            double varianza = retornos.stream()
                    .mapToDouble(r -> Math.pow(r - mediaRet, 2))
                    .average().orElse(0);
            double stdDiaria = Math.sqrt(varianza);

            // Anualizar (252 días de trading)
            double volAnual  = stdDiaria * Math.sqrt(252) * 100;
            double rentAnual = (Math.pow(1 + mediaRet, 252) - 1) * 100;

            if (Double.isInfinite(rentAnual) || Double.isNaN(rentAnual) || Math.abs(rentAnual) > 9999)
                return new double[]{0, 0, 0};

            // Ratio Sharpe con tasa libre de riesgo del 2.5%
            double tasaLibre = 2.5;
            double sharpe    = volAnual > 0 ? (rentAnual - tasaLibre) / volAnual : 0;

            return new double[]{rentAnual, volAnual, sharpe};

        } catch (Exception e) {
            e.printStackTrace();
            return new double[]{0, 0, 0};
        }
    }
}