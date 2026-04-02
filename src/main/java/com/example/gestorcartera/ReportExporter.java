package com.example.gestorcartera;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportExporter {

    private static final String RUTA_INFORME =
            System.getProperty("user.home") + "/gestorcartera_informe.html";

    public static String getRutaInforme() { return RUTA_INFORME; }

    /**
     * Genera el informe HTML, lo escribe en disco y lo abre en el navegador.
     *
     * @param activos  lista actual de activos
     * @param metricas double[3]: [0]=rentAnualizada, [1]=volatilidad, [2]=sharpe
     */
    public static void exportar(List<Activo> activos, double[] metricas) throws Exception {
        double totalInv  = activos.stream().mapToDouble(Activo::getInvertido).sum();
        double totalAct  = activos.stream().mapToDouble(Activo::getValorActual).sum();
        double beneficio = totalAct - totalInv;
        double rentGlob  = totalInv > 0 ? ((totalAct - totalInv) / totalInv) * 100 : 0;
        long   positivos = activos.stream().filter(a -> a.getRentabilidad() >= 0).count();
        long   negativos = activos.stream().filter(a -> a.getRentabilidad() <  0).count();
        double maxConc   = activos.stream().mapToDouble(a -> totalAct > 0 ? (a.getValorActual() / totalAct) * 100 : 0).max().orElse(0);
        Activo mejorAct  = activos.stream().max((a, b) -> Double.compare(a.getRentabilidad(), b.getRentabilidad())).orElse(null);
        Activo peorAct   = activos.stream().min((a, b) -> Double.compare(a.getRentabilidad(), b.getRentabilidad())).orElse(null);
        double rentMedia = activos.stream().mapToDouble(Activo::getRentabilidad).average().orElse(0);

        double rentAnualExport = metricas[0];
        double volAnualExport  = metricas[1];
        double sharpeExport    = metricas[2];

        Map<String, Double> porTipo = activos.stream().collect(
                Collectors.groupingBy(Activo::getTipo, Collectors.summingDouble(Activo::getValorActual)));
        Map<String, Double> porPlat = activos.stream().filter(a -> !a.getPlataforma().isEmpty())
                .collect(Collectors.groupingBy(Activo::getPlataforma, Collectors.summingDouble(Activo::getValorActual)));
        List<Activo> top5    = activos.stream().sorted((a, b) -> Double.compare(b.getRentabilidad(), a.getRentabilidad())).limit(5).collect(Collectors.toList());
        List<Activo> peores5 = activos.stream().sorted((a, b) -> Double.compare(a.getRentabilidad(), b.getRentabilidad())).limit(5).collect(Collectors.toList());

        double objetivoVal = 50000;
        double progreso    = Math.min((totalAct / objetivoVal) * 100, 100);
        String ahora       = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'><title>Informe GestorCartera</title><style>")
                .append("*{margin:0;padding:0;box-sizing:border-box}body{font-family:Arial,sans-serif;background:#f5f7fa;color:#1a1a2e}")
                .append(".page{max-width:1100px;margin:0 auto;padding:32px}")
                .append(".header{background:#1e1e2e;color:white;padding:36px;border-radius:16px;margin-bottom:28px}")
                .append(".header h1{font-size:26px;font-weight:700;margin-bottom:4px}.header .ts{color:#aaaacc;font-size:13px;margin-top:6px}")
                .append(".kpis{display:grid;grid-template-columns:repeat(4,1fr);gap:16px;margin-bottom:28px}")
                .append(".kpi{background:white;border-radius:12px;padding:20px;box-shadow:0 2px 12px rgba(0,0,0,.08)}")
                .append(".kpi .label{font-size:10px;font-weight:700;color:#888;text-transform:uppercase;margin-bottom:8px}")
                .append(".kpi .value{font-size:22px;font-weight:800}.kpi .sub{font-size:11px;color:#888;margin-top:4px}")
                .append(".green{color:#16a34a}.red{color:#dc2626}.purple{color:#7c6af7}.blue{color:#2563eb}")
                .append(".section{background:white;border-radius:12px;padding:22px;box-shadow:0 2px 12px rgba(0,0,0,.08);margin-bottom:22px}")
                .append(".section h2{font-size:15px;font-weight:700;margin-bottom:16px;padding-bottom:10px;border-bottom:2px solid #f0f0f5}")
                .append("table{width:100%;border-collapse:collapse;font-size:12px}")
                .append("th{background:#f8f9fc;padding:10px 12px;text-align:left;font-weight:700;color:#555;border-bottom:2px solid #e8e8f0}")
                .append("td{padding:9px 12px;border-bottom:1px solid #f0f0f5}")
                .append(".badge{display:inline-block;padding:2px 8px;border-radius:20px;font-size:10px;font-weight:700;color:white;background:#1a5276}")
                .append(".bar-bg{background:#f0f0f5;border-radius:99px;height:8px;overflow:hidden}.bar-fg{height:8px;border-radius:99px}")
                .append(".dist-row{display:flex;align-items:center;gap:12px;padding:8px 0;border-bottom:1px solid #f0f0f5}")
                .append(".dist-label{width:150px;font-size:12px;font-weight:600}.dist-pct{width:50px;font-size:12px;font-weight:700;text-align:right}")
                .append(".two-col{display:grid;grid-template-columns:1fr 1fr;gap:20px;margin-bottom:22px}")
                .append(".risk-grid{display:grid;grid-template-columns:repeat(3,1fr);gap:14px}")
                .append(".risk-item{background:#f8f9fc;border-radius:10px;padding:14px;text-align:center}")
                .append(".risk-item .r-val{font-size:20px;font-weight:800;margin-bottom:4px}.risk-item .r-lbl{font-size:10px;color:#888;text-transform:uppercase}")
                .append(".progress-bar{background:#f0f0f5;border-radius:99px;height:16px;overflow:hidden;margin:10px 0}")
                .append(".progress-fill{height:16px;border-radius:99px;background:linear-gradient(90deg,#7c6af7,#4ade80)}")
                .append(".footer{text-align:center;color:#aaa;font-size:11px;margin-top:28px;padding-top:14px;border-top:1px solid #e8e8f0}")
                .append("</style></head><body><div class='page'>");

        html.append(String.format("<div class='header'><h1>📊 Informe de Cartera de Inversiones</h1><p>%d activos · <b>%.2f € en cartera</b></p><p class='ts'>Generado el %s</p></div>",
                activos.size(), totalAct, ahora));

        String rentColor = rentGlob >= 0 ? "green" : "red";
        String benColor  = beneficio >= 0 ? "green" : "red";
        html.append("<div class='kpis'>")
                .append(String.format("<div class='kpi'><div class='label'>Total Invertido</div><div class='value blue'>%.2f €</div><div class='sub'>Capital aportado</div></div>", totalInv))
                .append(String.format("<div class='kpi'><div class='label'>Valor Actual</div><div class='value purple'>%.2f €</div><div class='sub'>Valoración a %s</div></div>", totalAct, LocalDate.now()))
                .append(String.format("<div class='kpi'><div class='label'>Rentabilidad</div><div class='value %s'>%+.2f%%</div><div class='sub'>Sobre capital</div></div>", rentColor, rentGlob))
                .append(String.format("<div class='kpi'><div class='label'>Beneficio</div><div class='value %s'>%+.2f €</div><div class='sub'>Ganancia neta</div></div>", benColor, beneficio))
                .append("</div>");

        html.append("<div class='two-col'><div class='section'><h2>Distribución por Tipo</h2>");
        porTipo.entrySet().stream().sorted((a, b) -> Double.compare(b.getValue(), a.getValue())).forEach(e -> {
            double pct = totalAct > 0 ? (e.getValue() / totalAct) * 100 : 0;
            html.append(String.format("<div class='dist-row'><div class='dist-label'>%s</div><div style='flex:1'><div class='bar-bg'><div class='bar-fg' style='width:%.1f%%;background:#7c6af7'></div></div></div><div class='dist-pct'>%.1f%%</div></div>",
                    e.getKey(), pct, pct));
        });
        html.append("</div><div class='section'><h2>Distribución por Plataforma</h2>");
        porPlat.entrySet().stream().sorted((a, b) -> Double.compare(b.getValue(), a.getValue())).forEach(e -> {
            double pct = totalAct > 0 ? (e.getValue() / totalAct) * 100 : 0;
            html.append(String.format("<div class='dist-row'><div class='dist-label'>%s</div><div style='flex:1'><div class='bar-bg'><div class='bar-fg' style='width:%.1f%%;background:#1a5276'></div></div></div><div class='dist-pct'>%.1f%%</div></div>",
                    e.getKey(), pct, pct));
        });
        html.append("</div></div>");

        html.append("<div class='two-col'><div class='section'><h2>🏆 Top 5 Posiciones</h2><table><tr><th>Activo</th><th>Valor</th><th>Rent.</th></tr>");
        for (Activo a : top5) html.append(String.format("<tr><td><b>%s</b></td><td>%.0f €</td><td class='green'><b>%+.2f%%</b></td></tr>", a.getNombre(), a.getValorActual(), a.getRentabilidad()));
        html.append("</table></div><div class='section'><h2>⚠️ Peores 5 Posiciones</h2><table><tr><th>Activo</th><th>Valor</th><th>Rent.</th></tr>");
        for (Activo a : peores5) html.append(String.format("<tr><td><b>%s</b></td><td>%.0f €</td><td class='red'><b>%+.2f%%</b></td></tr>", a.getNombre(), a.getValorActual(), a.getRentabilidad()));
        html.append("</table></div></div>");

        String concColor = maxConc > 40 ? "#dc2626" : maxConc > 25 ? "#d97706" : "#16a34a";
        html.append("<div class='section'><h2>Métricas de Riesgo</h2><div class='risk-grid'>")
                .append(String.format("<div class='risk-item'><div class='r-val green'>%d</div><div class='r-lbl'>En positivo</div></div>", positivos))
                .append(String.format("<div class='risk-item'><div class='r-val red'>%d</div><div class='r-lbl'>En negativo</div></div>", negativos))
                .append(String.format("<div class='risk-item'><div class='r-val' style='color:%s'>%.1f%%</div><div class='r-lbl'>Máx concentración</div></div>", concColor, maxConc))
                .append(String.format("<div class='risk-item'><div class='r-val %s'>%+.1f%%</div><div class='r-lbl'>Rent. media</div></div>", rentMedia >= 0 ? "green" : "red", rentMedia))
                .append(String.format("<div class='risk-item'><div class='r-val green' style='font-size:14px'>%s</div><div class='r-lbl'>Mejor activo</div></div>", mejorAct != null ? mejorAct.getNombre() : "-"))
                .append(String.format("<div class='risk-item'><div class='r-val red' style='font-size:14px'>%s</div><div class='r-lbl'>Peor activo</div></div>", peorAct != null ? peorAct.getNombre() : "-"))
                .append("</div></div>");

        String rentAnualColor = rentAnualExport >= 0 ? "#16a34a" : "#dc2626";
        String volColor  = volAnualExport < 15 ? "#16a34a" : volAnualExport < 30 ? "#d97706" : "#dc2626";
        String volDesc   = volAnualExport < 15 ? "Baja" : volAnualExport < 30 ? "Moderada" : "Alta";
        String sharpeColor = sharpeExport >= 1 ? "#16a34a" : sharpeExport >= 0 ? "#d97706" : "#dc2626";
        String sharpeDesc  = sharpeExport >= 1 ? "Bueno" : sharpeExport >= 0 ? "Aceptable" : "Bajo";
        html.append("<div class='section'><h2>Métricas Financieras Avanzadas</h2>")
                .append("<p style='color:#888;font-size:12px;margin-bottom:16px'>Calculadas a partir del histórico diario registrado. Tasa libre de riesgo: 2,5%.</p>")
                .append("<div class='risk-grid'>");
        html.append(rentAnualExport != 0
                ? String.format("<div class='risk-item'><div class='r-val' style='color:%s;font-size:16px'>%+.1f%%</div><div class='r-lbl'>Rentabilidad anualizada</div></div>", rentAnualColor, Math.max(-999, Math.min(9999, rentAnualExport)))
                : "<div class='risk-item'><div class='r-val' style='color:#888'>Sin datos</div><div class='r-lbl'>Rentabilidad anualizada</div></div>");
        html.append(volAnualExport != 0
                ? String.format("<div class='risk-item'><div class='r-val' style='color:%s'>%.1f%%</div><div class='r-lbl'>Volatilidad anual (%s)</div></div>", volColor, volAnualExport, volDesc)
                : "<div class='risk-item'><div class='r-val' style='color:#888'>Sin datos</div><div class='r-lbl'>Volatilidad anual</div></div>");
        html.append(sharpeExport != 0
                ? String.format("<div class='risk-item'><div class='r-val' style='color:%s'>%.2f</div><div class='r-lbl'>Ratio Sharpe (%s)</div></div>", sharpeColor, sharpeExport, sharpeDesc)
                : "<div class='risk-item'><div class='r-val' style='color:#888'>Sin datos</div><div class='r-lbl'>Ratio Sharpe</div></div>");
        html.append("</div>")
                .append("<p style='font-size:11px;color:#aaa;margin-top:12px'>")
                .append("⚠ Métricas basadas en histórico limitado. A mayor número de días registrados, mayor precisión.</p>")
                .append("</div>");

        html.append("<div class='section'><h2>Detalle completo de Holdings</h2><table>")
                .append("<tr><th>Activo</th><th>Tipo</th><th>Plataforma</th><th>Invertido</th><th>Valor</th><th>Rent.</th><th>Peso</th><th>Compra</th><th>Registrado</th></tr>");
        activos.stream().sorted((x, y) -> Double.compare(y.getValorActual(), x.getValorActual())).forEach(a -> {
            double pct = totalAct > 0 ? (a.getValorActual() / totalAct) * 100 : 0;
            String rc  = a.getRentabilidad() >= 0 ? "#16a34a" : "#dc2626";
            html.append(String.format("<tr><td><b>%s</b></td><td><span class='badge'>%s</span></td><td>%s</td><td>%.2f €</td><td><b>%.2f €</b></td><td style='color:%s;font-weight:700'>%+.2f%%</td><td>%.1f%%</td><td>%s</td><td style='color:#888;font-size:11px'>%s</td></tr>",
                    a.getNombre(), a.getTipo(), a.getPlataforma(), a.getInvertido(), a.getValorActual(), rc, a.getRentabilidad(), pct, a.getFecha(), a.getRegistradoEnFormateado()));
        });
        html.append("</table></div>");

        html.append(String.format("<div class='section'><h2>🎯 Progreso hacia Objetivo (50.000 €)</h2><p style='color:#888;font-size:12px;margin-bottom:10px'>Valor actual: <b>%.2f €</b> · Falta: <b>%.2f €</b></p><div class='progress-bar'><div class='progress-fill' style='width:%.1f%%'></div></div><p style='font-size:12px;margin-top:8px'><b>%.1f%%</b> completado</p></div>",
                totalAct, Math.max(objetivoVal - totalAct, 0), progreso, progreso));
        html.append(String.format("<div class='footer'>GestorCartera · Informe generado el %s · Solo para uso personal.</div></div></body></html>", ahora));

        Files.writeString(Path.of(RUTA_INFORME), html.toString());
        try { new ProcessBuilder("xdg-open", RUTA_INFORME).start(); } catch (IOException ignored) {}
    }
}