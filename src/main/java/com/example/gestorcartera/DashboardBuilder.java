package com.example.gestorcartera;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardBuilder {

    private final VBox dashboardContainer;
    private final VBox objetivosContainer;
    private final ObservableList<Activo> activos;
    private final String archivoHistorico;

    public DashboardBuilder(VBox dashboardContainer, VBox objetivosContainer,
                             ObservableList<Activo> activos, String archivoHistorico) {
        this.dashboardContainer = dashboardContainer;
        this.objetivosContainer = objetivosContainer;
        this.activos            = activos;
        this.archivoHistorico   = archivoHistorico;
    }

    // ── Dashboard principal ───────────────────────────────────────────────────

    public void actualizarDashboard() {
        dashboardContainer.getChildren().clear();

        if (activos.isEmpty()) {
            mostrarDashboardVacio();
            return;
        }

        double totalInv  = activos.stream().mapToDouble(Activo::getInvertido).sum();
        double totalAct  = activos.stream().mapToDouble(Activo::getValorActual).sum();
        double beneficio = totalAct - totalInv;
        double rentGlob  = totalInv > 0 ? (beneficio / totalInv) * 100 : 0;
        long   positivos = activos.stream().filter(a -> a.getRentabilidad() >= 0).count();
        long   negativos = activos.size() - positivos;
        Activo mejor = activos.stream().max((a, b) -> Double.compare(a.getRentabilidad(), b.getRentabilidad())).orElse(null);
        Activo peor  = activos.stream().min((a, b) -> Double.compare(a.getRentabilidad(), b.getRentabilidad())).orElse(null);

        HBox heroRow = new HBox(14);
        heroRow.getChildren().addAll(
                heroCard("💰", "TOTAL INVERTIDO",   String.format("%.2f €", totalInv),  null, Theme.INFO),
                heroCard("📈", "VALOR ACTUAL",       String.format("%.2f €", totalAct),  null, Theme.ACCENT),
                heroCard(beneficio >= 0 ? "💹" : "📉", "BENEFICIO / PÉRD.",
                        String.format("%+.2f €", beneficio),
                        String.format("sobre %.0f € invertidos", totalInv),
                        beneficio >= 0 ? Theme.SUCCESS : Theme.DANGER),
                heroCard(rentGlob >= 0 ? "📊" : "📉", "RENTABILIDAD",
                        String.format("%+.2f%%", rentGlob),
                        activos.size() + " posiciones",
                        rentGlob >= 0 ? Theme.SUCCESS : Theme.DANGER)
        );
        heroRow.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));

        HBox middleRow = new HBox(14);
        VBox pieBox = new VBox(4);
        pieBox.setPadding(new Insets(16, 16, 8, 16));
        pieBox.setMinWidth(400); pieBox.setMaxWidth(480);
        pieBox.setStyle("-fx-background-color: " + Theme.panel() + "; -fx-background-radius: 14; " +
                "-fx-border-color: " + Theme.border() + "; -fx-border-radius: 14; -fx-border-width: 1;");
        Label pieTitulo = new Label("Distribución por tipo");
        pieTitulo.setStyle("-fx-text-fill: " + Theme.texto() + "; -fx-font-size: 14px; -fx-font-weight: bold;");
        pieBox.getChildren().addAll(pieTitulo, crearMiniPie());
        HBox.setHgrow(pieBox, Priority.SOMETIMES);

        VBox statsBox = new VBox(12);
        HBox.setHgrow(statsBox, Priority.ALWAYS);

        HBox statsRow1 = new HBox(12);
        statsRow1.getChildren().addAll(
                miniStatCard("✅", "En positivo", String.valueOf(positivos), Theme.SUCCESS),
                miniStatCard("❌", "En negativo", String.valueOf(negativos), Theme.DANGER));
        statsRow1.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));

        HBox statsRow2 = new HBox(12);
        if (mejor != null) statsRow2.getChildren().add(miniStatCard("🏆", "Mejor", mejor.getNombre(), Theme.SUCCESS));
        if (peor  != null) statsRow2.getChildren().add(miniStatCard("⚠️", "Peor",  peor.getNombre(),  Theme.DANGER));
        statsRow2.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));

        double[] metricas = MetricsService.calcular(archivoHistorico);
        double rentAnual  = metricas[0];
        double volAnual   = metricas[1];
        double sharpe     = metricas[2];

        HBox statsRow3 = new HBox(12);
        statsRow3.getChildren().addAll(
                miniStatCard("📅", "Rent. anualizada",
                        metricas[0] != 0 ? String.format("%+.1f%%", Math.max(-999, Math.min(9999, rentAnual))) : "Sin datos",
                        rentAnual >= 0 ? Theme.SUCCESS : Theme.DANGER),
                miniStatCard("📉", "Volatilidad anual",
                        metricas[1] != 0 ? String.format("%.1f%%", volAnual) : "Sin datos",
                        volAnual < 15 ? Theme.SUCCESS : volAnual < 30 ? Theme.WARNING : Theme.DANGER),
                miniStatCard("⚖️", "Ratio Sharpe",
                        metricas[2] != 0 ? String.format("%.2f", sharpe) : "Sin datos",
                        sharpe >= 1 ? Theme.SUCCESS : sharpe >= 0 ? Theme.WARNING : Theme.DANGER));
        statsRow3.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));

        VBox distBox = buildDistribucionBox(totalAct);
        VBox.setVgrow(distBox, Priority.ALWAYS);
        statsBox.getChildren().addAll(statsRow1, statsRow2, statsRow3, distBox);
        middleRow.getChildren().addAll(pieBox, statsBox);

        HBox detalleHeader = new HBox(10);
        detalleHeader.setAlignment(Pos.CENTER_LEFT);
        Label detalleTitulo = new Label("Tus posiciones");
        detalleTitulo.setStyle("-fx-text-fill: " + Theme.texto() + "; -fx-font-size: 16px; -fx-font-weight: bold;");
        Region det = new Region(); HBox.setHgrow(det, Priority.ALWAYS);
        Label detalleSub = new Label(String.format("%d activos · %s", activos.size(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        detalleSub.setStyle("-fx-text-fill: " + Theme.muted() + "; -fx-font-size: 11px;");
        detalleHeader.getChildren().addAll(detalleTitulo, det, detalleSub);

        int cols = 3;
        List<HBox> filas = new ArrayList<>();
        HBox filaActual = new HBox(14);
        int contador = 0;
        List<Activo> sorted = activos.stream()
                .sorted((a, b) -> Double.compare(b.getValorActual(), a.getValorActual()))
                .collect(Collectors.toList());

        for (Activo a : sorted) {
            VBox card = activoCard(a, totalAct, contador * 60L);
            HBox.setHgrow(card, Priority.ALWAYS);
            filaActual.getChildren().add(card);
            contador++;
            if (contador % cols == 0) { filas.add(filaActual); filaActual = new HBox(14); }
        }
        if (!filaActual.getChildren().isEmpty()) {
            while (filaActual.getChildren().size() < cols) {
                Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
                filaActual.getChildren().add(sp);
            }
            filas.add(filaActual);
        }

        dashboardContainer.getChildren().add(heroRow);
        dashboardContainer.getChildren().add(middleRow);
        dashboardContainer.getChildren().add(detalleHeader);
        dashboardContainer.getChildren().addAll(filas);
    }

    // ── Objetivos ─────────────────────────────────────────────────────────────

    public void actualizarObjetivos() {
        objetivosContainer.getChildren().clear();
        Label titulo = new Label("Objetivos de distribución por tipo");
        titulo.setStyle("-fx-text-fill: " + Theme.texto() + "; -fx-font-size: 18px; -fx-font-weight: bold;");
        Label info = new Label("Comparativa de tu distribución actual frente a los objetivos predefinidos.");
        info.setStyle("-fx-text-fill: " + Theme.sub() + "; -fx-font-size: 12px;");
        info.setWrapText(true);

        double totalAct = activos.stream().mapToDouble(Activo::getValorActual).sum();
        Map<String, Double> porTipo = activos.stream().collect(
                Collectors.groupingBy(Activo::getTipo, Collectors.summingDouble(Activo::getValorActual)));
        Map<String, Double> objetivos = Map.of(
                "ETF", 30.0, "Fund Equity", 25.0, "Crypto", 20.0,
                "Fund Bond", 10.0, "Money Market", 8.0, "Commodity", 5.0, "Stock", 2.0);

        VBox filas = new VBox(10);
        porTipo.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .forEach(entry -> {
                    String tipo  = entry.getKey();
                    double valor = entry.getValue();
                    double pct   = totalAct > 0 ? (valor / totalAct) * 100 : 0;
                    double obj   = objetivos.getOrDefault(tipo, 0.0);
                    double desv  = pct - obj;
                    String desvColor = Math.abs(desv) < 5 ? Theme.SUCCESS : Math.abs(desv) < 10 ? Theme.WARNING : Theme.DANGER;

                    VBox fila = new VBox(8);
                    fila.setPadding(new Insets(14));
                    fila.setStyle("-fx-background-color: " + Theme.panel() + "; -fx-background-radius: 12; " +
                            "-fx-border-color: " + Theme.border() + "; -fx-border-radius: 12; -fx-border-width: 1;");

                    HBox topRow = new HBox(12);
                    topRow.setAlignment(Pos.CENTER_LEFT);

                    Label lTipo = new Label(PortfolioUtils.traducirTipo(tipo));
                    lTipo.setMinWidth(150);
                    lTipo.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; " +
                            "-fx-background-color: " + PortfolioUtils.colorPorTipo(tipo) + "; -fx-padding: 4 12; -fx-background-radius: 4;");

                    Label lValor = new Label(String.format("%.0f €  (%.1f%%)", valor, pct));
                    lValor.setStyle("-fx-text-fill: " + Theme.texto() + "; -fx-font-size: 13px; -fx-font-weight: bold;");

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    String desvTxt = obj > 0
                            ? String.format("Obj: %.0f%%  ·  Desv: %+.1f%%", obj, desv)
                            : "Sin objetivo definido";
                    Label lDesv = new Label(desvTxt);
                    lDesv.setStyle("-fx-text-fill: " + (obj > 0 ? desvColor : Theme.muted()) + "; -fx-font-size: 12px;");

                    topRow.getChildren().addAll(lTipo, lValor, spacer, lDesv);

                    Pane barraFondo = new Pane();
                    barraFondo.setPrefHeight(14);
                    barraFondo.setMaxWidth(Double.MAX_VALUE);
                    barraFondo.setStyle("-fx-background-color: " + Theme.border() + "; -fx-background-radius: 6;");

                    Region barraRelleno = new Region();
                    barraRelleno.setPrefHeight(14);
                    barraRelleno.setStyle("-fx-background-color: " + PortfolioUtils.colorPorTipo(tipo) +
                            "; -fx-background-radius: 6; -fx-opacity: 0.85;");
                    barraRelleno.setPrefWidth(0);
                    barraFondo.widthProperty().addListener((obs, oldW, newW) ->
                            barraRelleno.setPrefWidth(newW.doubleValue() * pct / 100.0));
                    barraFondo.getChildren().add(barraRelleno);

                    fila.getChildren().addAll(topRow, barraFondo);
                    filas.getChildren().add(fila);
                });
        objetivosContainer.getChildren().addAll(titulo, info, filas);
    }

    // ── Helpers privados ──────────────────────────────────────────────────────

    private void mostrarDashboardVacio() {
        VBox empty = new VBox(14);
        empty.setAlignment(Pos.CENTER);
        empty.setPadding(new Insets(80));
        Label icon  = new Label("💼"); icon.setStyle("-fx-font-size: 52px;");
        Label title = new Label("Tu cartera está vacía");
        title.setStyle("-fx-text-fill: " + Theme.sub() + "; -fx-font-size: 18px; -fx-font-weight: bold;");
        Label hint = new Label("Añade tu primer activo con el botón inferior, o importa desde Excel");
        hint.setStyle("-fx-text-fill: " + Theme.muted() + "; -fx-font-size: 13px;");
        empty.getChildren().addAll(icon, title, hint);
        dashboardContainer.getChildren().add(empty);
    }

    private HBox heroCard(String icono, String titulo, String valor, String sub, String accentColor) {
        Region accentBar = new Region();
        accentBar.setPrefWidth(4); accentBar.setMinWidth(4);
        accentBar.setStyle("-fx-background-color: " + accentColor + "; -fx-background-radius: 4 0 0 4;");

        VBox content = new VBox(6);
        content.setPadding(new Insets(16, 20, 16, 16));
        HBox.setHgrow(content, Priority.ALWAYS);

        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label icon = new Label(icono); icon.setStyle("-fx-font-size: 16px;");
        Label lbl  = new Label(titulo);
        lbl.setStyle("-fx-text-fill: " + Theme.sub() + "; -fx-font-size: 9px; -fx-font-weight: bold;");
        titleRow.getChildren().addAll(icon, lbl);

        Label valorLabel = new Label(valor);
        valorLabel.setStyle("-fx-text-fill: " + accentColor + "; -fx-font-size: 24px; -fx-font-weight: bold;");
        content.getChildren().addAll(titleRow, valorLabel);

        if (sub != null && !sub.isEmpty()) {
            Label subLabel = new Label(sub);
            subLabel.setStyle("-fx-text-fill: " + Theme.sub() + "; -fx-font-size: 10px;");
            content.getChildren().add(subLabel);
        }

        HBox card = new HBox();
        String baseStyle = "-fx-background-color: " + Theme.panel() + "; -fx-background-radius: 12; " +
                "-fx-border-color: " + Theme.border() + "; -fx-border-radius: 12; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 2);";
        card.setStyle(baseStyle);
        card.setOpacity(0);
        card.getChildren().addAll(accentBar, content);
        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: " + Theme.card() + "; -fx-background-radius: 12; " +
                        "-fx-border-color: " + accentColor + "; -fx-border-radius: 12; -fx-border-width: 1; " +
                        "-fx-effect: dropshadow(gaussian, rgba(124,106,247,0.3), 16, 0, 0, 4); -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle(baseStyle));

        FadeTransition ft = new FadeTransition(Duration.millis(500), card);
        ft.setFromValue(0); ft.setToValue(1); ft.setDelay(Duration.millis(80));
        ft.play();
        return card;
    }

    private VBox miniStatCard(String icono, String titulo, String valor, String accentColor) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(14, 16, 14, 16));
        card.setStyle("-fx-background-color: " + Theme.card() + "; -fx-background-radius: 10; " +
                "-fx-border-color: " + Theme.border() + "; -fx-border-radius: 10; -fx-border-width: 1;");
        HBox row = new HBox(6); row.setAlignment(Pos.CENTER_LEFT);
        Label ico = new Label(icono); ico.setStyle("-fx-font-size: 14px;");
        Label tit = new Label(titulo);
        tit.setStyle("-fx-text-fill: " + Theme.sub() + "; -fx-font-size: 10px; -fx-font-weight: bold;");
        row.getChildren().addAll(ico, tit);
        Label val = new Label(valor);
        val.setStyle("-fx-text-fill: " + accentColor + "; -fx-font-size: 18px; -fx-font-weight: bold;");
        val.setWrapText(true);
        card.getChildren().addAll(row, val);
        return card;
    }

    private VBox buildDistribucionBox(double totalAct) {
        VBox box = new VBox(6);
        box.setPadding(new Insets(12));
        box.setStyle("-fx-background-color: " + Theme.card() + "; -fx-background-radius: 10; " +
                "-fx-border-color: " + Theme.border() + "; -fx-border-radius: 10; -fx-border-width: 1;");

        Label titulo = new Label("Distribución por tipo");
        titulo.setStyle("-fx-text-fill: " + Theme.sub() + "; -fx-font-size: 10px; -fx-font-weight: bold;");
        box.getChildren().add(titulo);

        Map<String, Double> porTipo = activos.stream().collect(
                Collectors.groupingBy(Activo::getTipo, Collectors.summingDouble(Activo::getValorActual)));
        porTipo.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .forEach(entry -> {
                    double pct = totalAct > 0 ? (entry.getValue() / totalAct) * 100 : 0;

                    VBox fila = new VBox(3);
                    HBox labelRow = new HBox(8);
                    labelRow.setAlignment(Pos.CENTER_LEFT);
                    Label badge = new Label(PortfolioUtils.traducirTipo(entry.getKey()));
                    badge.setMinWidth(150);
                    badge.setStyle("-fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; " +
                            "-fx-background-color: " + PortfolioUtils.colorPorTipo(entry.getKey()) + "; " +
                            "-fx-padding: 3 10; -fx-background-radius: 4;");
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    Label pctLbl = new Label(String.format("%.1f%%", pct));
                    pctLbl.setStyle("-fx-text-fill: " + Theme.texto() + "; -fx-font-size: 11px; -fx-font-weight: bold;");
                    Label valLbl = new Label(String.format("  %.0f \u20ac", entry.getValue()));
                    valLbl.setStyle("-fx-text-fill: " + Theme.sub() + "; -fx-font-size: 10px;");
                    labelRow.getChildren().addAll(badge, spacer, pctLbl, valLbl);

                    Pane barraFondo = new Pane();
                    barraFondo.setPrefHeight(10);
                    barraFondo.setMaxWidth(Double.MAX_VALUE);
                    barraFondo.setStyle("-fx-background-color: " + Theme.border() + "; -fx-background-radius: 5;");

                    Region barraRelleno = new Region();
                    barraRelleno.setPrefHeight(10);
                    barraRelleno.setStyle("-fx-background-color: " + PortfolioUtils.colorPorTipo(entry.getKey()) +
                            "; -fx-background-radius: 5; -fx-opacity: 0.85;");
                    barraRelleno.setPrefWidth(0);
                    barraFondo.widthProperty().addListener((obs, oldW, newW) ->
                            barraRelleno.setPrefWidth(newW.doubleValue() * pct / 100.0));
                    barraFondo.getChildren().add(barraRelleno);
                    fila.getChildren().addAll(labelRow, barraFondo);
                    box.getChildren().add(fila);
                });
        return box;
    }

    private PieChart crearMiniPie() {
        Map<String, Double> porTipo = activos.stream().collect(
                Collectors.groupingBy(Activo::getTipo, Collectors.summingDouble(Activo::getValorActual)));
        ObservableList<PieChart.Data> datos = FXCollections.observableArrayList();
        porTipo.forEach((tipo, valor) -> datos.add(new PieChart.Data(PortfolioUtils.traducirTipo(tipo), valor)));
        PieChart pie = new PieChart(datos);
        pie.setLabelsVisible(false); pie.setLegendVisible(true);
        pie.setPrefHeight(460); pie.setPrefWidth(Double.MAX_VALUE);
        pie.setMaxHeight(Double.MAX_VALUE); pie.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(pie, Priority.ALWAYS);
        pie.setStyle("-fx-background-color: transparent;");
        return pie;
    }

    private VBox activoCard(Activo a, double totalAct, long delayMs) {
        double pct    = totalAct > 0 ? (a.getValorActual() / totalAct) * 100 : 0;
        double rent   = a.getRentabilidad();
        String rentCol = rent >= 0 ? Theme.SUCCESS : Theme.DANGER;

        VBox card = new VBox(10);
        card.setPadding(new Insets(16));
        card.setOpacity(0);
        String baseStyle = "-fx-background-color: " + Theme.panel() + "; -fx-background-radius: 12; " +
                "-fx-border-color: " + Theme.border() + "; -fx-border-radius: 12; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 2);";
        card.setStyle(baseStyle);

        VBox nombreBadgeBox = new VBox(3);
        Label nombreLbl = new Label(a.getNombre());
        nombreLbl.setStyle("-fx-text-fill: " + Theme.texto() + "; -fx-font-size: 13px; -fx-font-weight: bold;");
        nombreLbl.setWrapText(true);
        Label tipoBadge = new Label(PortfolioUtils.traducirTipo(a.getTipo()));
        tipoBadge.setStyle("-fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; " +
                "-fx-background-color: " + PortfolioUtils.colorPorTipo(a.getTipo()) + "; -fx-padding: 2 8; -fx-background-radius: 4;");
        nombreBadgeBox.getChildren().addAll(nombreLbl, tipoBadge);
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.TOP_LEFT);
        topRow.getChildren().add(nombreBadgeBox);

        Label valorLbl = new Label(String.format("%.2f €", a.getValorActual()));
        valorLbl.setStyle("-fx-text-fill: " + Theme.texto() + "; -fx-font-size: 22px; -fx-font-weight: bold;");

        double beneficio = a.getValorActual() - a.getInvertido();
        Label beneficioLbl = new Label(String.format("%+.2f €", beneficio));
        beneficioLbl.setStyle("-fx-text-fill: " + rentCol + "; -fx-font-size: 12px; -fx-font-weight: bold;");

        HBox rentRow = new HBox(10); rentRow.setAlignment(Pos.CENTER_LEFT);
        Label invLbl = new Label(String.format("Inv: %.0f €", a.getInvertido()));
        invLbl.setStyle("-fx-text-fill: " + Theme.sub() + "; -fx-font-size: 11px;");
        Region rentSpacer = new Region(); HBox.setHgrow(rentSpacer, Priority.ALWAYS);
        Label rentLbl = new Label(String.format("%+.2f%%", rent));
        rentLbl.setStyle("-fx-text-fill: " + rentCol + "; -fx-font-size: 13px; -fx-font-weight: bold;" +
                " -fx-background-color: " + (rent >= 0 ? "rgba(74,222,128,0.15)" : "rgba(248,113,113,0.15)") +
                "; -fx-background-radius: 4; -fx-padding: 2 8;");
        rentRow.getChildren().addAll(invLbl, rentSpacer, rentLbl);

        card.getChildren().addAll(topRow, valorLbl, beneficioLbl, rentRow);
        if (!a.getPlataforma().isEmpty()) {
            Label platLbl = new Label("🏦  " + a.getPlataforma());
            platLbl.setStyle("-fx-text-fill: " + Theme.sub() + "; -fx-font-size: 10px;");
            card.getChildren().add(platLbl);
        }

        VBox weightBox = new VBox(3);
        HBox weightRow = new HBox();
        Label wLbl = new Label("Peso en cartera");
        wLbl.setStyle("-fx-text-fill: " + Theme.sub() + "; -fx-font-size: 10px;");
        Region ws = new Region(); HBox.setHgrow(ws, Priority.ALWAYS);
        Label pctLbl = new Label(String.format("%.1f%%", pct));
        pctLbl.setStyle("-fx-text-fill: " + Theme.texto() + "; -fx-font-size: 10px; -fx-font-weight: bold;");
        weightRow.getChildren().addAll(wLbl, ws, pctLbl);
        ProgressBar bar = new ProgressBar(pct / 100);
        bar.setMaxWidth(Double.MAX_VALUE); bar.setPrefHeight(5);
        bar.setStyle("-fx-accent: " + Theme.ACCENT + ";");
        weightBox.getChildren().addAll(weightRow, bar);
        card.getChildren().add(weightBox);

        Label tsLbl = new Label("🕐  " + a.getRegistradoEnFormateado());
        tsLbl.setStyle("-fx-text-fill: " + Theme.muted() + "; -fx-font-size: 10px;");
        card.getChildren().add(tsLbl);

        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: " + Theme.card() + "; -fx-background-radius: 12; " +
                        "-fx-border-color: " + Theme.ACCENT + "; -fx-border-radius: 12; -fx-border-width: 1; " +
                        "-fx-effect: dropshadow(gaussian, rgba(124,106,247,0.3), 14, 0, 0, 4); -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle(baseStyle));

        FadeTransition ft = new FadeTransition(Duration.millis(400), card);
        ft.setFromValue(0); ft.setToValue(1); ft.setDelay(Duration.millis(delayMs));
        ft.play();
        return card;
    }
}