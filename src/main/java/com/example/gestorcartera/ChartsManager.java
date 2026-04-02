package com.example.gestorcartera;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChartsManager {

    private final PieChart                  graficoPorTipo;
    private final BarChart<String, Number>  graficaComparativa;
    private final LineChart<String, Number> graficaHistorico;
    private final VBox                      evolucionContent;
    private final ObservableList<Activo>    activos;
    private final String                    archivoHistorico;

    public ChartsManager(PieChart graficoPorTipo,
                         BarChart<String, Number> graficaComparativa,
                         LineChart<String, Number> graficaHistorico,
                         VBox evolucionContent,
                         ObservableList<Activo> activos,
                         String archivoHistorico) {
        this.graficoPorTipo    = graficoPorTipo;
        this.graficaComparativa = graficaComparativa;
        this.graficaHistorico  = graficaHistorico;
        this.evolucionContent  = evolucionContent;
        this.activos           = activos;
        this.archivoHistorico  = archivoHistorico;
    }

    public void actualizarGrafico() {
        double totalAct = activos.stream().mapToDouble(Activo::getValorActual).sum();
        Map<String, Double> porTipo = activos.stream().collect(
                Collectors.groupingBy(Activo::getTipo, Collectors.summingDouble(Activo::getValorActual)));
        ObservableList<PieChart.Data> datos = FXCollections.observableArrayList();
        porTipo.forEach((tipo, valor) -> datos.add(new PieChart.Data(PortfolioUtils.traducirTipo(tipo), valor)));
        graficoPorTipo.setData(datos);
        graficoPorTipo.getData().forEach(d -> {
            double pct = totalAct > 0 ? (d.getPieValue() / totalAct) * 100 : 0;
            Tooltip tp = new Tooltip(String.format("%s%n%.2f €  (%.1f%%)", d.getName(), d.getPieValue(), pct));
            tp.setStyle("-fx-font-size: 12px; -fx-background-color: #2d2d42; -fx-text-fill: white; " +
                    "-fx-background-radius: 8; -fx-padding: 8 12;");
            Tooltip.install(d.getNode(), tp);
            d.getNode().setOnMouseEntered(e -> d.getNode().setStyle("-fx-opacity: 0.8; -fx-cursor: hand;"));
            d.getNode().setOnMouseExited(e  -> d.getNode().setStyle("-fx-opacity: 1.0;"));
        });
    }

    public void actualizarGraficaComparativa() {
        graficaComparativa.getData().clear();
        XYChart.Series<String, Number> serieInv = new XYChart.Series<>();
        serieInv.setName("Invertido");
        XYChart.Series<String, Number> serieAct = new XYChart.Series<>();
        serieAct.setName("Valor actual");
        for (Activo a : activos) {
            serieInv.getData().add(new XYChart.Data<>(a.getNombre(), a.getInvertido()));
            serieAct.getData().add(new XYChart.Data<>(a.getNombre(), a.getValorActual()));
        }
        graficaComparativa.getData().addAll(serieInv, serieAct);
        graficaComparativa.getData().forEach(serie ->
                serie.getData().forEach(d -> {
                    if (d.getNode() != null) {
                        Tooltip tp = new Tooltip(String.format("%s%n%s: %.2f €",
                                d.getXValue(), serie.getName(), d.getYValue().doubleValue()));
                        tp.setStyle("-fx-font-size: 12px; -fx-background-color: #2d2d42; " +
                                "-fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 12;");
                        Tooltip.install(d.getNode(), tp);
                        d.getNode().setOnMouseEntered(e -> d.getNode().setStyle("-fx-opacity:0.8;-fx-cursor:hand;"));
                        d.getNode().setOnMouseExited(e  -> d.getNode().setStyle("-fx-opacity:1.0;"));
                    }
                })
        );
    }

    public void cargarGraficaHistorico() {
        try {
            graficaHistorico.getData().clear();
            XYChart.Series<String, Number> serie = new XYChart.Series<>();
            List<double[]> puntos = new ArrayList<>();

            if (Files.exists(Path.of(archivoHistorico))) {
                for (String linea : Files.readAllLines(Path.of(archivoHistorico))) {
                    String[] p = linea.split(";");
                    if (p.length == 2) {
                        try {
                            double val = Double.parseDouble(p[1].replace(",", "."));
                            serie.getData().add(new XYChart.Data<>(p[0], val));
                            puntos.add(new double[]{val});
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
            graficaHistorico.getData().add(serie);
            if (!serie.getData().isEmpty())
                serie.getNode().setStyle("-fx-stroke: " + Theme.ACCENT + "; -fx-stroke-width: 2.5px;");

            if (puntos.size() >= 2 && evolucionContent.getChildren().size() < 4) {
                double primerValor = puntos.get(0)[0];
                double ultimoValor = puntos.get(puntos.size() - 1)[0];
                double cambioTotal = ultimoValor - primerValor;
                double cambioPct   = primerValor > 0 ? (cambioTotal / primerValor) * 100 : 0;
                double maxValor    = puntos.stream().mapToDouble(x -> x[0]).max().orElse(0);
                double minValor    = puntos.stream().mapToDouble(x -> x[0]).min().orElse(0);

                HBox statsRow = new HBox(14);
                statsRow.setPadding(new Insets(4, 0, 0, 0));

                for (String[] stat : new String[][]{
                        {String.valueOf(puntos.size()), "Días registrados", Theme.ACCENT},
                        {String.format("%.0f €", primerValor), "Valor inicial", Theme.sub()},
                        {String.format("%.0f €", ultimoValor), "Valor actual", Theme.ACCENT},
                        {String.format("%+.2f €", cambioTotal), "Variación total", cambioTotal >= 0 ? Theme.SUCCESS : Theme.DANGER},
                        {String.format("%+.1f%%", cambioPct), "Rentab. período", cambioPct >= 0 ? Theme.SUCCESS : Theme.DANGER},
                        {String.format("%.0f €", maxValor), "Máximo histórico", Theme.WARNING},
                        {String.format("%.0f €", minValor), "Mínimo histórico", Theme.INFO}
                }) {
                    VBox card = new VBox(4);
                    card.setPadding(new Insets(12, 16, 12, 16));
                    card.setStyle("-fx-background-color: " + Theme.panel() + "; -fx-background-radius: 10; " +
                            "-fx-border-color: " + Theme.border() + "; -fx-border-radius: 10; -fx-border-width: 1;");
                    HBox.setHgrow(card, Priority.ALWAYS);
                    Label lVal = new Label(stat[0]);
                    lVal.setStyle("-fx-text-fill: " + stat[2] + "; -fx-font-size: 16px; -fx-font-weight: bold;");
                    Label lLbl = new Label(stat[1]);
                    lLbl.setStyle("-fx-text-fill: " + Theme.sub() + "; -fx-font-size: 10px;");
                    card.getChildren().addAll(lLbl, lVal);
                    statsRow.getChildren().add(card);
                }
                evolucionContent.getChildren().add(statsRow);
            } else if (puntos.size() < 2 && evolucionContent.getChildren().size() < 4) {
                Label hint = new Label("ℹ️  Con más días de uso aparecerán estadísticas: rentabilidad del período, máximos, mínimos y días registrados.");
                hint.setStyle("-fx-text-fill: " + Theme.sub() + "; -fx-font-size: 12px;");
                hint.setWrapText(true);
                evolucionContent.getChildren().add(hint);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}