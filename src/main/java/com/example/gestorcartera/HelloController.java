package com.example.gestorcartera;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.converter.DoubleStringConverter;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HelloController {

    @FXML private BorderPane rootPane;
    @FXML private VBox   topBar;
    @FXML private HBox   bottomBar;
    @FXML private HBox   carteraContent;
    @FXML private TabPane tabPane;
    @FXML private VBox   comparativaContent;
    @FXML private VBox   evolucionContent;
    @FXML private VBox   dashboardContainer;
    @FXML private VBox   objetivosContainer;
    @FXML private VBox   simuladorContainer;
    @FXML private VBox   resultadoSimulador;

    @FXML private Label  labelInvertido;
    @FXML private Label  labelValorActual;
    @FXML private Label  labelRentabilidadGlobal;
    @FXML private Label  labelTituloInvertido;
    @FXML private Label  labelTituloValor;
    @FXML private Label  labelTituloRent;
    @FXML private Label  labelTotal;
    @FXML private Label  labelHoraActualizacion;
    @FXML private Label  labelReloj;
    @FXML private Label  labelFechaHoy;

    @FXML private Button btnAnadir;
    @FXML private Button btnImportar;
    @FXML private Button btnExportar;
    @FXML private Button botonEliminar;

    @FXML private TableView<Activo>            tablaActivos;
    @FXML private TableColumn<Activo, String>  colNombre;
    @FXML private TableColumn<Activo, String>  colTipo;
    @FXML private TableColumn<Activo, Double>  colInvertido;
    @FXML private TableColumn<Activo, Double>  colValorActual;
    @FXML private TableColumn<Activo, String>  colRentabilidad;
    @FXML private TableColumn<Activo, String>  colPorcentaje;
    @FXML private TableColumn<Activo, String>  colProgreso;
    @FXML private TableColumn<Activo, String>  colFecha;
    @FXML private TableColumn<Activo, String>  colPlataforma;
    @FXML private TableColumn<Activo, String>  colRegistrado;
    @FXML private TableColumn<Activo, String>  colNotas;

    @FXML private ComboBox<String> filtroTipo;
    @FXML private ComboBox<String> filtroPlataforma;
    @FXML private TextField        campoBusqueda;

    @FXML private PieChart                  graficoPorTipo;
    @FXML private LineChart<String, Number> graficaHistorico;
    @FXML private BarChart<String, Number>  graficaComparativa;

    @FXML private TextField campoObjetivo;
    @FXML private TextField campoAportacion;
    @FXML private TextField campoRentabilidadEsperada;

    private final ObservableList<Activo> activos = FXCollections.observableArrayList();
    private FilteredList<Activo> activosFiltrados;

    private static final String ARCHIVO_DATOS     = System.getProperty("user.home") + "/gestorcartera.csv";
    private static final String ARCHIVO_HISTORICO = System.getProperty("user.home") + "/gestorcartera_historico.csv";

    private final PersistenceService persistence = new PersistenceService(ARCHIVO_DATOS, ARCHIVO_HISTORICO);

    private String bg()     { return "#1a1a2c"; }
    private String panel()  { return "#242437"; }
    private String card()   { return "#2d2d42"; }
    private String border() { return "#3a3a52"; }
    private String texto()  { return "#e8e8f5"; }
    private String sub()    { return "#8888aa"; }
    private String muted()  { return "#555570"; }

    private static final String C_ACCENT  = "#7c6af7";
    private static final String C_SUCCESS = "#4ade80";
    private static final String C_DANGER  = "#f87171";
    private static final String C_WARNING = "#fbbf24";
    private static final String C_INFO    = "#60a5fa";

    // ─────────────────────────────────────────────────────────────────────────
    // INICIALIZACIÓN
    // ─────────────────────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        configurarTabla();
        configurarFiltros();
        configurarBotones();
        iniciarReloj();
        cargarDatos();
        // Aplicar CSS de modo cuando la escena esté lista
        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) aplicarEstiloModo();
        });

        // Animación fade entre pestañas
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null && newTab.getContent() != null) {
                javafx.scene.Node contenido = newTab.getContent();
                contenido.setOpacity(0);
                FadeTransition ft = new FadeTransition(Duration.millis(220), contenido);
                ft.setFromValue(0); ft.setToValue(1);
                ft.play();
            }
        });
    }

    private void configurarTabla() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colInvertido.setCellValueFactory(new PropertyValueFactory<>("invertido"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colPlataforma.setCellValueFactory(new PropertyValueFactory<>("plataforma"));
        colNotas.setCellValueFactory(new PropertyValueFactory<>("notas"));

        colRegistrado.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getRegistradoEnFormateado()));

        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colTipo.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else {
                    setText(traducirTipo(item));
                    setStyle("-fx-background-color: " + colorPorTipo(item) + ";"
                            + "-fx-text-fill: white; -fx-font-weight: bold;"
                            + "-fx-alignment: CENTER; -fx-background-radius: 4; -fx-font-size: 11px;");
                }
            }
        });

        colValorActual.setCellValueFactory(new PropertyValueFactory<>("valorActual"));
        colValorActual.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        colValorActual.setOnEditCommit(event -> {
            event.getRowValue().setValorActual(event.getNewValue());
            tablaActivos.refresh();
            refrescarTodo();
            guardarDatos();
        });

        colRentabilidad.setCellValueFactory(data ->
                new SimpleStringProperty(String.format("%+.2f%%", data.getValue().getRentabilidad())));
        colRentabilidad.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else {
                    setText(item);
                    setStyle(item.startsWith("+")
                            ? "-fx-text-fill: " + C_SUCCESS + "; -fx-font-weight: bold;"
                            : "-fx-text-fill: " + C_DANGER  + "; -fx-font-weight: bold;");
                }
            }
        });

        colPorcentaje.setCellValueFactory(data -> {
            double total = activos.stream().mapToDouble(Activo::getValorActual).sum();
            double pct = total > 0 ? (data.getValue().getValorActual() / total) * 100 : 0;
            return new SimpleStringProperty(String.format("%.1f%%", pct));
        });

        colProgreso.setCellValueFactory(data -> {
            double total = activos.stream().mapToDouble(Activo::getValorActual).sum();
            double pct = total > 0 ? (data.getValue().getValorActual() / total) * 100 : 0;
            return new SimpleStringProperty(String.valueOf(pct / 100));
        });
        colProgreso.setCellFactory(col -> new TableCell<>() {
            private final ProgressBar bar = new ProgressBar();
            { bar.setMaxWidth(Double.MAX_VALUE); bar.setStyle("-fx-accent: " + C_ACCENT + ";"); }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setGraphic(null);
                else { bar.setProgress(Double.parseDouble(item)); setGraphic(bar); }
            }
        });

        activosFiltrados = new FilteredList<>(activos, p -> true);
        tablaActivos.setItems(activosFiltrados);

        tablaActivos.setRowFactory(tv -> {
            TableRow<Activo> row = new TableRow<>();
            row.setPrefHeight(38);
            String rowBase  = "-fx-font-size: 12px;";
            String rowHover = "-fx-background-color: " + card() + "; -fx-font-size: 12px;";
            row.setOnMouseEntered(e -> { if (!row.isSelected()) row.setStyle(rowHover); });
            row.setOnMouseExited(e  -> row.setStyle(rowBase));
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    TableColumn<Activo, ?> col = tablaActivos.getFocusModel().getFocusedCell().getTableColumn();
                    if (col != colValorActual) onEditarActivo(row.getItem());
                }
            });
            return row;
        });
    }

    private void configurarFiltros() {
        filtroTipo.getItems().add("Todos");
        filtroTipo.setValue("Todos");
        filtroPlataforma.getItems().add("Todas");
        filtroPlataforma.setValue("Todas");
        filtroTipo.setOnAction(e -> aplicarFiltros());
        filtroPlataforma.setOnAction(e -> aplicarFiltros());
        campoBusqueda.textProperty().addListener((obs, a, n) -> aplicarFiltros());
    }

    private void configurarBotones() {
        String estiloAnadir =
                "-fx-background-color: " + C_ACCENT + "; -fx-text-fill: white; " +
                        "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 10 22; " +
                        "-fx-background-radius: 8; -fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(124,106,247,0.45), 10, 0, 0, 3);";
        String estiloAnadirHover =
                "-fx-background-color: #9b8cf9; -fx-text-fill: white; " +
                        "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 10 22; " +
                        "-fx-background-radius: 8; -fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(124,106,247,0.6), 14, 0, 0, 4);";
        btnAnadir.setStyle(estiloAnadir);
        btnAnadir.setOnMouseEntered(e -> btnAnadir.setStyle(estiloAnadirHover));
        btnAnadir.setOnMouseExited(e -> btnAnadir.setStyle(estiloAnadir));

        String estiloSec =
                "-fx-background-color: #2d2d42; -fx-text-fill: #aaaacc; " +
                        "-fx-font-size: 12px; -fx-padding: 10 18; -fx-background-radius: 8; " +
                        "-fx-cursor: hand; -fx-border-color: #3a3a52; -fx-border-radius: 8; -fx-border-width: 1;";
        String estiloSecHover =
                "-fx-background-color: #3a3a55; -fx-text-fill: white; " +
                        "-fx-font-size: 12px; -fx-padding: 10 18; -fx-background-radius: 8; " +
                        "-fx-cursor: hand; -fx-border-color: #5a5a7a; -fx-border-radius: 8; -fx-border-width: 1;";
        for (Button b : new Button[]{btnImportar, btnExportar}) {
            b.setStyle(estiloSec);
            b.setOnMouseEntered(e -> b.setStyle(estiloSecHover));
            b.setOnMouseExited(e -> b.setStyle(estiloSec));
        }

        botonEliminar.setDisable(true);
        actualizarEstiloEliminar(false);
        tablaActivos.getSelectionModel().selectedItemProperty().addListener((obs, a, n) -> {
            boolean sel = n != null;
            botonEliminar.setDisable(!sel);
            actualizarEstiloEliminar(sel);
        });
    }

    private void actualizarEstiloEliminar(boolean activo) {
        if (activo) {
            String e = "-fx-background-color: #dc2626; -fx-text-fill: white; " +
                    "-fx-font-size: 12px; -fx-padding: 10 18; -fx-background-radius: 8; " +
                    "-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(220,38,38,0.4), 8, 0, 0, 2);";
            botonEliminar.setStyle(e);
            botonEliminar.setOnMouseEntered(ev -> botonEliminar.setStyle(
                    e.replace("#dc2626", "#ef4444").replace("0.4", "0.55")));
            botonEliminar.setOnMouseExited(ev -> botonEliminar.setStyle(e));
        } else {
            botonEliminar.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: " + muted() + "; " +
                            "-fx-font-size: 12px; -fx-padding: 10 18; -fx-background-radius: 8; " +
                            "-fx-border-color: " + border() + "; -fx-border-radius: 8; -fx-border-width: 1;");
        }
    }

    private void iniciarReloj() {
        DateTimeFormatter fmtHora  = DateTimeFormatter.ofPattern("HH:mm:ss");
        DateTimeFormatter fmtFecha = DateTimeFormatter.ofPattern("dd MMM yyyy");
        Timeline reloj = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            LocalDateTime now = LocalDateTime.now();
            labelReloj.setText(now.format(fmtHora));
            labelFechaHoy.setText(now.format(fmtFecha));
        }));
        reloj.setCycleCount(Animation.INDEFINITE);
        reloj.play();
        LocalDateTime now = LocalDateTime.now();
        labelReloj.setText(now.format(fmtHora));
        labelFechaHoy.setText(now.format(fmtFecha));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CSS DINÁMICO — resuelve el problema del modo claro en gráficas y tabla
    // ─────────────────────────────────────────────────────────────────────────

    private void aplicarEstiloModo() {
        Scene scene = rootPane.getScene();
        if (scene == null) return;
        scene.getStylesheets().removeIf(s -> s.contains("dark.css") || s.contains("gestorcartera_theme"));
        var url = getClass().getResource("dark.css");
        if (url != null) scene.getStylesheets().add(url.toExternalForm());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MÉTRICAS FINANCIERAS AVANZADAS
    // ─────────────────────────────────────────────────────────────────────────

    private double[] calcularMetricas() {
        return MetricsService.calcular(ARCHIVO_HISTORICO);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DASHBOARD
    // ─────────────────────────────────────────────────────────────────────────

    private void actualizarDashboard() {
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
                heroCard("💰", "TOTAL INVERTIDO",   String.format("%.2f €", totalInv),  null, C_INFO),
                heroCard("📈", "VALOR ACTUAL",       String.format("%.2f €", totalAct),  null, C_ACCENT),
                heroCard(beneficio >= 0 ? "💹" : "📉", "BENEFICIO / PÉRD.",
                        String.format("%+.2f €", beneficio),
                        String.format("sobre %.0f € invertidos", totalInv),
                        beneficio >= 0 ? C_SUCCESS : C_DANGER),
                heroCard(rentGlob >= 0 ? "📊" : "📉", "RENTABILIDAD",
                        String.format("%+.2f%%", rentGlob),
                        activos.size() + " posiciones",
                        rentGlob >= 0 ? C_SUCCESS : C_DANGER)
        );
        heroRow.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));

        HBox middleRow = new HBox(14);
        VBox pieBox = new VBox(4);
        pieBox.setPadding(new Insets(16, 16, 8, 16));
        pieBox.setMinWidth(400); pieBox.setMaxWidth(480);
        pieBox.setStyle("-fx-background-color: " + panel() + "; -fx-background-radius: 14; " +
                "-fx-border-color: " + border() + "; -fx-border-radius: 14; -fx-border-width: 1;");
        Label pieTitulo = new Label("Distribución por tipo");
        pieTitulo.setStyle("-fx-text-fill: " + texto() + "; -fx-font-size: 14px; -fx-font-weight: bold;");
        Label pieSub = new Label("Por valor actual de cada categoría");
        pieSub.setStyle("-fx-text-fill: " + sub() + "; -fx-font-size: 10px;");
        pieBox.getChildren().addAll(pieTitulo, crearMiniPie());
        HBox.setHgrow(pieBox, Priority.SOMETIMES);

        VBox statsBox = new VBox(12);
        HBox.setHgrow(statsBox, Priority.ALWAYS);

        HBox statsRow1 = new HBox(12);
        statsRow1.getChildren().addAll(
                miniStatCard("✅", "En positivo", String.valueOf(positivos), C_SUCCESS),
                miniStatCard("❌", "En negativo", String.valueOf(negativos), C_DANGER));
        statsRow1.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));

        HBox statsRow2 = new HBox(12);
        if (mejor != null) statsRow2.getChildren().add(miniStatCard("🏆", "Mejor", mejor.getNombre(), C_SUCCESS));
        if (peor  != null) statsRow2.getChildren().add(miniStatCard("⚠️", "Peor",  peor.getNombre(),  C_DANGER));
        statsRow2.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));

        // Fila de métricas financieras avanzadas
        double[] metricas = calcularMetricas();
        double rentAnual  = metricas[0];
        double volAnual   = metricas[1];
        double sharpe     = metricas[2];

        HBox statsRow3 = new HBox(12);
        statsRow3.getChildren().addAll(
                miniStatCard("📅", "Rent. anualizada",
                        metricas[0] != 0 ? String.format("%+.1f%%", Math.max(-999, Math.min(9999, rentAnual))) : "Sin datos",
                        rentAnual >= 0 ? C_SUCCESS : C_DANGER),
                miniStatCard("📉", "Volatilidad anual",
                        metricas[1] != 0 ? String.format("%.1f%%", volAnual) : "Sin datos",
                        volAnual < 15 ? C_SUCCESS : volAnual < 30 ? C_WARNING : C_DANGER),
                miniStatCard("⚖️", "Ratio Sharpe",
                        metricas[2] != 0 ? String.format("%.2f", sharpe) : "Sin datos",
                        sharpe >= 1 ? C_SUCCESS : sharpe >= 0 ? C_WARNING : C_DANGER));
        statsRow3.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));

        VBox distBox = buildDistribucionBox(totalAct);
        VBox.setVgrow(distBox, Priority.ALWAYS);
        statsBox.getChildren().addAll(statsRow1, statsRow2, statsRow3, distBox);
        middleRow.getChildren().addAll(pieBox, statsBox);

        HBox detalleHeader = new HBox(10);
        detalleHeader.setAlignment(Pos.CENTER_LEFT);
        Label detalleTitulo = new Label("Tus posiciones");
        detalleTitulo.setStyle("-fx-text-fill: " + texto() + "; -fx-font-size: 16px; -fx-font-weight: bold;");
        Region det = new Region(); HBox.setHgrow(det, Priority.ALWAYS);
        Label detalleSub = new Label(String.format("%d activos · %s", activos.size(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        detalleSub.setStyle("-fx-text-fill: " + muted() + "; -fx-font-size: 11px;");
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

    private void mostrarDashboardVacio() {
        VBox empty = new VBox(14);
        empty.setAlignment(Pos.CENTER);
        empty.setPadding(new Insets(80));
        Label icon  = new Label("💼"); icon.setStyle("-fx-font-size: 52px;");
        Label title = new Label("Tu cartera está vacía");
        title.setStyle("-fx-text-fill: " + sub() + "; -fx-font-size: 18px; -fx-font-weight: bold;");
        Label hint = new Label("Añade tu primer activo con el botón inferior, o importa desde Excel");
        hint.setStyle("-fx-text-fill: " + muted() + "; -fx-font-size: 13px;");
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
        lbl.setStyle("-fx-text-fill: " + sub() + "; -fx-font-size: 9px; -fx-font-weight: bold;");
        titleRow.getChildren().addAll(icon, lbl);

        Label valorLabel = new Label(valor);
        valorLabel.setStyle("-fx-text-fill: " + accentColor + "; -fx-font-size: 24px; -fx-font-weight: bold;");
        content.getChildren().addAll(titleRow, valorLabel);

        if (sub != null && !sub.isEmpty()) {
            Label subLabel = new Label(sub);
            subLabel.setStyle("-fx-text-fill: " + sub() + "; -fx-font-size: 10px;");
            content.getChildren().add(subLabel);
        }

        HBox card = new HBox();
        String baseStyle = "-fx-background-color: " + panel() + "; -fx-background-radius: 12; " +
                "-fx-border-color: " + border() + "; -fx-border-radius: 12; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 2);";
        card.setStyle(baseStyle);
        card.setOpacity(0);
        card.getChildren().addAll(accentBar, content);
        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: " + card() + "; -fx-background-radius: 12; " +
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
        card.setStyle("-fx-background-color: " + card() + "; -fx-background-radius: 10; " +
                "-fx-border-color: " + border() + "; -fx-border-radius: 10; -fx-border-width: 1;");
        HBox row = new HBox(6); row.setAlignment(Pos.CENTER_LEFT);
        Label ico = new Label(icono); ico.setStyle("-fx-font-size: 14px;");
        Label tit = new Label(titulo);
        tit.setStyle("-fx-text-fill: " + sub() + "; -fx-font-size: 10px; -fx-font-weight: bold;");
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
        box.setStyle("-fx-background-color: " + card() + "; -fx-background-radius: 10; " +
                "-fx-border-color: " + border() + "; -fx-border-radius: 10; -fx-border-width: 1;");

        Label titulo = new Label("Distribución por tipo");
        titulo.setStyle("-fx-text-fill: " + sub() + "; -fx-font-size: 10px; -fx-font-weight: bold;");
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
                    Label badge = new Label(traducirTipo(entry.getKey()));
                    badge.setMinWidth(150);
                    badge.setStyle("-fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; " +
                            "-fx-background-color: " + colorPorTipo(entry.getKey()) + "; " +
                            "-fx-padding: 3 10; -fx-background-radius: 4;");
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    Label pctLbl = new Label(String.format("%.1f%%", pct));
                    pctLbl.setStyle("-fx-text-fill: " + texto() + "; -fx-font-size: 11px; -fx-font-weight: bold;");
                    Label valLbl = new Label(String.format("  %.0f \u20ac", entry.getValue()));
                    valLbl.setStyle("-fx-text-fill: " + sub() + "; -fx-font-size: 10px;");
                    labelRow.getChildren().addAll(badge, spacer, pctLbl, valLbl);

                    Pane barraFondo = new Pane();
                    barraFondo.setPrefHeight(10);
                    barraFondo.setMaxWidth(Double.MAX_VALUE);
                    barraFondo.setStyle("-fx-background-color: " + border() + "; -fx-background-radius: 5;");

                    Region barraRelleno = new Region();
                    barraRelleno.setPrefHeight(10);
                    barraRelleno.setStyle("-fx-background-color: " + colorPorTipo(entry.getKey()) +
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
        porTipo.forEach((tipo, valor) -> datos.add(new PieChart.Data(traducirTipo(tipo), valor)));
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
        String rentCol = rent >= 0 ? C_SUCCESS : C_DANGER;

        VBox card = new VBox(10);
        card.setPadding(new Insets(16));
        card.setOpacity(0);
        String baseStyle = "-fx-background-color: " + panel() + "; -fx-background-radius: 12; " +
                "-fx-border-color: " + border() + "; -fx-border-radius: 12; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 2);";
        card.setStyle(baseStyle);

        // Nombre + badge pegado
        VBox nombreBadgeBox = new VBox(3);
        Label nombreLbl = new Label(a.getNombre());
        nombreLbl.setStyle("-fx-text-fill: " + texto() + "; -fx-font-size: 13px; -fx-font-weight: bold;");
        nombreLbl.setWrapText(true);
        Label tipoBadge = new Label(traducirTipo(a.getTipo()));
        tipoBadge.setStyle("-fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; " +
                "-fx-background-color: " + colorPorTipo(a.getTipo()) + "; -fx-padding: 2 8; -fx-background-radius: 4;");
        nombreBadgeBox.getChildren().addAll(nombreLbl, tipoBadge);
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.TOP_LEFT);
        topRow.getChildren().add(nombreBadgeBox);

        // Valor actual grande
        Label valorLbl = new Label(String.format("%.2f €", a.getValorActual()));
        valorLbl.setStyle("-fx-text-fill: " + texto() + "; -fx-font-size: 22px; -fx-font-weight: bold;");

        // Beneficio absoluto
        double beneficio = a.getValorActual() - a.getInvertido();
        Label beneficioLbl = new Label(String.format("%+.2f €", beneficio));
        beneficioLbl.setStyle("-fx-text-fill: " + rentCol + "; -fx-font-size: 12px; -fx-font-weight: bold;");

        // Fila invertido + rentabilidad %
        HBox rentRow = new HBox(10); rentRow.setAlignment(Pos.CENTER_LEFT);
        Label invLbl = new Label(String.format("Inv: %.0f €", a.getInvertido()));
        invLbl.setStyle("-fx-text-fill: " + sub() + "; -fx-font-size: 11px;");
        Region rentSpacer = new Region(); HBox.setHgrow(rentSpacer, Priority.ALWAYS);
        Label rentLbl = new Label(String.format("%+.2f%%", rent));
        rentLbl.setStyle("-fx-text-fill: " + rentCol + "; -fx-font-size: 13px; -fx-font-weight: bold;" +
                " -fx-background-color: " + (rent >= 0 ? "rgba(74,222,128,0.15)" : "rgba(248,113,113,0.15)") +
                "; -fx-background-radius: 4; -fx-padding: 2 8;");
        rentRow.getChildren().addAll(invLbl, rentSpacer, rentLbl);

        card.getChildren().addAll(topRow, valorLbl, beneficioLbl, rentRow);
        if (!a.getPlataforma().isEmpty()) {
            Label platLbl = new Label("🏦  " + a.getPlataforma());
            platLbl.setStyle("-fx-text-fill: " + sub() + "; -fx-font-size: 10px;");
            card.getChildren().add(platLbl);
        }

        VBox weightBox = new VBox(3);
        HBox weightRow = new HBox();
        Label wLbl = new Label("Peso en cartera");
        wLbl.setStyle("-fx-text-fill: " + sub() + "; -fx-font-size: 10px;");
        Region ws = new Region(); HBox.setHgrow(ws, Priority.ALWAYS);
        Label pctLbl = new Label(String.format("%.1f%%", pct));
        pctLbl.setStyle("-fx-text-fill: " + texto() + "; -fx-font-size: 10px; -fx-font-weight: bold;");
        weightRow.getChildren().addAll(wLbl, ws, pctLbl);
        ProgressBar bar = new ProgressBar(pct / 100);
        bar.setMaxWidth(Double.MAX_VALUE); bar.setPrefHeight(5);
        bar.setStyle("-fx-accent: " + C_ACCENT + ";");
        weightBox.getChildren().addAll(weightRow, bar);
        card.getChildren().add(weightBox);

        Label tsLbl = new Label("🕐  " + a.getRegistradoEnFormateado());
        tsLbl.setStyle("-fx-text-fill: " + muted() + "; -fx-font-size: 10px;");
        card.getChildren().add(tsLbl);

        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: " + card() + "; -fx-background-radius: 12; " +
                        "-fx-border-color: " + C_ACCENT + "; -fx-border-radius: 12; -fx-border-width: 1; " +
                        "-fx-effect: dropshadow(gaussian, rgba(124,106,247,0.3), 14, 0, 0, 4); -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle(baseStyle));

        FadeTransition ft = new FadeTransition(Duration.millis(400), card);
        ft.setFromValue(0); ft.setToValue(1); ft.setDelay(Duration.millis(delayMs));
        ft.play();
        return card;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // OBJETIVOS
    // ─────────────────────────────────────────────────────────────────────────

    private void actualizarObjetivos() {
        objetivosContainer.getChildren().clear();
        Label titulo = new Label("Objetivos de distribución por tipo");
        titulo.setStyle("-fx-text-fill: " + texto() + "; -fx-font-size: 18px; -fx-font-weight: bold;");
        Label info = new Label("Comparativa de tu distribución actual frente a los objetivos predefinidos.");
        info.setStyle("-fx-text-fill: " + sub() + "; -fx-font-size: 12px;");
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
                    String desvColor = Math.abs(desv) < 5 ? C_SUCCESS : Math.abs(desv) < 10 ? C_WARNING : C_DANGER;

                    VBox fila = new VBox(8);
                    fila.setPadding(new Insets(14));
                    fila.setStyle("-fx-background-color: " + panel() + "; -fx-background-radius: 12; " +
                            "-fx-border-color: " + border() + "; -fx-border-radius: 12; -fx-border-width: 1;");

                    // Fila superior: badge + valor + desviación
                    HBox topRow = new HBox(12);
                    topRow.setAlignment(Pos.CENTER_LEFT);

                    Label lTipo = new Label(traducirTipo(tipo));
                    lTipo.setMinWidth(150);
                    lTipo.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; " +
                            "-fx-background-color: " + colorPorTipo(tipo) + "; -fx-padding: 4 12; -fx-background-radius: 4;");

                    Label lValor = new Label(String.format("%.0f €  (%.1f%%)", valor, pct));
                    lValor.setStyle("-fx-text-fill: " + texto() + "; -fx-font-size: 13px; -fx-font-weight: bold;");

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    String desvTxt = obj > 0
                            ? String.format("Obj: %.0f%%  ·  Desv: %+.1f%%", obj, desv)
                            : "Sin objetivo definido";
                    Label lDesv = new Label(desvTxt);
                    lDesv.setStyle("-fx-text-fill: " + (obj > 0 ? desvColor : muted()) + "; -fx-font-size: 12px;");

                    topRow.getChildren().addAll(lTipo, lValor, spacer, lDesv);

                    // Barra proporcional en línea propia
                    Pane barraFondo = new Pane();
                    barraFondo.setPrefHeight(14);
                    barraFondo.setMaxWidth(Double.MAX_VALUE);
                    barraFondo.setStyle("-fx-background-color: " + border() + "; -fx-background-radius: 6;");

                    Region barraRelleno = new Region();
                    barraRelleno.setPrefHeight(14);
                    barraRelleno.setStyle("-fx-background-color: " + colorPorTipo(tipo) +
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

    // ─────────────────────────────────────────────────────────────────────────
    // FILTROS
    // ─────────────────────────────────────────────────────────────────────────

    private void aplicarFiltros() {
        String tipo = filtroTipo.getValue();
        String plat = filtroPlataforma.getValue();
        String busq = campoBusqueda.getText().toLowerCase().trim();
        activosFiltrados.setPredicate(a -> {
            boolean okTipo = tipo == null || tipo.equals("Todos") || a.getTipo().equals(tipo);
            boolean okPlat = plat == null || plat.equals("Todas") || a.getPlataforma().equals(plat);
            boolean okBusq = busq.isEmpty() || a.getNombre().toLowerCase().contains(busq);
            return okTipo && okPlat && okBusq;
        });
    }

    private void actualizarFiltroOpciones() {
        String actTipo = filtroTipo.getValue();
        String actPlat = filtroPlataforma.getValue();
        filtroTipo.getItems().clear();
        filtroTipo.getItems().add("Todos");
        activos.stream().map(Activo::getTipo).distinct().sorted().forEach(t -> filtroTipo.getItems().add(t));
        filtroTipo.setValue(filtroTipo.getItems().contains(actTipo) ? actTipo : "Todos");
        filtroPlataforma.getItems().clear();
        filtroPlataforma.getItems().add("Todas");
        activos.stream().map(Activo::getPlataforma).filter(p -> !p.isEmpty()).distinct().sorted()
                .forEach(p -> filtroPlataforma.getItems().add(p));
        filtroPlataforma.setValue(filtroPlataforma.getItems().contains(actPlat) ? actPlat : "Todas");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RESUMEN CABECERA
    // ─────────────────────────────────────────────────────────────────────────

    private void actualizarResumen() {
        double totalInv = activos.stream().mapToDouble(Activo::getInvertido).sum();
        double totalAct = activos.stream().mapToDouble(Activo::getValorActual).sum();
        double rent     = totalInv > 0 ? ((totalAct - totalInv) / totalInv) * 100 : 0;
        labelInvertido.setText(String.format("%.2f €", totalInv));
        labelValorActual.setText(String.format("%.2f €", totalAct));
        labelRentabilidadGlobal.setText(String.format("%+.2f%%", rent));
        labelRentabilidadGlobal.setStyle(rent >= 0
                ? "-fx-text-fill: " + C_SUCCESS + "; -fx-font-size: 15px; -fx-font-weight: bold;"
                : "-fx-text-fill: " + C_DANGER  + "; -fx-font-size: 15px; -fx-font-weight: bold;");
        labelTotal.setText(String.format("Total: %.2f €", totalAct));
        labelHoraActualizacion.setText("Actualizado: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GRÁFICAS
    // ─────────────────────────────────────────────────────────────────────────

    private void actualizarGrafico() {
        double totalAct = activos.stream().mapToDouble(Activo::getValorActual).sum();
        Map<String, Double> porTipo = activos.stream().collect(
                Collectors.groupingBy(Activo::getTipo, Collectors.summingDouble(Activo::getValorActual)));
        ObservableList<PieChart.Data> datos = FXCollections.observableArrayList();
        porTipo.forEach((tipo, valor) -> datos.add(new PieChart.Data(traducirTipo(tipo), valor)));
        graficoPorTipo.setData(datos);
        // Tooltips en cada sector del pie
        graficoPorTipo.getData().forEach(d -> {
            double pct = totalAct > 0 ? (d.getPieValue() / totalAct) * 100 : 0;
            Tooltip tp = new Tooltip(String.format("%s%n%.2f €  (%.1f%%)", d.getName(), d.getPieValue(), pct));
            tp.setStyle("-fx-font-size: 12px; -fx-background-color: #2d2d42; -fx-text-fill: white; " +
                    "-fx-background-radius: 8; -fx-padding: 8 12;");
            Tooltip.install(d.getNode(), tp);
            // Mini efecto hover en pie
            d.getNode().setOnMouseEntered(e -> d.getNode().setStyle("-fx-opacity: 0.8; -fx-cursor: hand;"));
            d.getNode().setOnMouseExited(e  -> d.getNode().setStyle("-fx-opacity: 1.0;"));
        });
    }

    private void actualizarGraficaComparativa() {
        graficaComparativa.getData().clear();
        XYChart.Series<String, Number> serieInv = new XYChart.Series<>();
        serieInv.setName("Invertido");
        XYChart.Series<String, Number> serieAct = new XYChart.Series<>();
        serieAct.setName("Valor actual");
        for (Activo a : activos) {
            XYChart.Data<String, Number> dInv = new XYChart.Data<>(a.getNombre(), a.getInvertido());
            XYChart.Data<String, Number> dAct = new XYChart.Data<>(a.getNombre(), a.getValorActual());
            serieInv.getData().add(dInv);
            serieAct.getData().add(dAct);
        }
        graficaComparativa.getData().addAll(serieInv, serieAct);
        // Tooltips en barras tras renderizar
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

    private void cargarGraficaHistorico() {
        try {
            graficaHistorico.getData().clear();
            XYChart.Series<String, Number> serie = new XYChart.Series<>();
            List<double[]> puntos = new ArrayList<>();

            if (Files.exists(Path.of(ARCHIVO_HISTORICO))) {
                for (String linea : Files.readAllLines(Path.of(ARCHIVO_HISTORICO))) {
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
                serie.getNode().setStyle("-fx-stroke: " + C_ACCENT + "; -fx-stroke-width: 2.5px;");

            // Bloque de estadísticas históricas
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
                        {String.valueOf(puntos.size()), "Días registrados", C_ACCENT},
                        {String.format("%.0f €", primerValor), "Valor inicial", sub()},
                        {String.format("%.0f €", ultimoValor), "Valor actual", C_ACCENT},
                        {String.format("%+.2f €", cambioTotal), "Variación total", cambioTotal >= 0 ? C_SUCCESS : C_DANGER},
                        {String.format("%+.1f%%", cambioPct), "Rentab. período", cambioPct >= 0 ? C_SUCCESS : C_DANGER},
                        {String.format("%.0f €", maxValor), "Máximo histórico", C_WARNING},
                        {String.format("%.0f €", minValor), "Mínimo histórico", C_INFO}
                }) {
                    VBox card = new VBox(4);
                    card.setPadding(new Insets(12, 16, 12, 16));
                    card.setStyle("-fx-background-color: " + panel() + "; -fx-background-radius: 10; " +
                            "-fx-border-color: " + border() + "; -fx-border-radius: 10; -fx-border-width: 1;");
                    HBox.setHgrow(card, Priority.ALWAYS);
                    Label lVal = new Label(stat[0]);
                    lVal.setStyle("-fx-text-fill: " + stat[2] + "; -fx-font-size: 16px; -fx-font-weight: bold;");
                    Label lLbl = new Label(stat[1]);
                    lLbl.setStyle("-fx-text-fill: " + sub() + "; -fx-font-size: 10px;");
                    card.getChildren().addAll(lLbl, lVal);
                    statsRow.getChildren().add(card);
                }
                evolucionContent.getChildren().add(statsRow);
            } else if (puntos.size() < 2 && evolucionContent.getChildren().size() < 4) {
                Label hint = new Label("ℹ️  Con más días de uso aparecerán estadísticas: rentabilidad del período, máximos, mínimos y días registrados.");
                hint.setStyle("-fx-text-fill: " + sub() + "; -fx-font-size: 12px;");
                hint.setWrapText(true);
                evolucionContent.getChildren().add(hint);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PERSISTENCIA
    // ─────────────────────────────────────────────────────────────────────────

    private void guardarDatos() {
        persistence.guardar(activos);
    }

    private void cargarDatos() {
        activos.addAll(persistence.cargar());
        refrescarTodo();
        persistence.guardarPuntoHistorico(activos.stream().mapToDouble(Activo::getValorActual).sum());
        cargarGraficaHistorico();
    }

    private void guardarPuntoHistorico(double total) {
        persistence.guardarPuntoHistorico(total);
    }

    private void refrescarTodo() {
        actualizarResumen();
        actualizarGrafico();
        actualizarFiltroOpciones();
        actualizarGraficaComparativa();
        actualizarDashboard();
        actualizarObjetivos();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SIMULADOR
    // ─────────────────────────────────────────────────────────────────────────

    @FXML
    private void onCalcularSimulador() {
        resultadoSimulador.getChildren().clear();
        try {
            double objetivo   = Double.parseDouble(campoObjetivo.getText().replace(",", "."));
            double aportacion = Double.parseDouble(campoAportacion.getText().replace(",", "."));
            double rentAnual  = Double.parseDouble(campoRentabilidadEsperada.getText().replace(",", ".")) / 100;
            double actual     = activos.stream().mapToDouble(Activo::getValorActual).sum();

            if (actual >= objetivo) {
                Label ya = new Label("🎉  ¡Ya has alcanzado tu objetivo!");
                ya.setStyle("-fx-text-fill: " + C_SUCCESS + "; -fx-font-size: 16px; -fx-font-weight: bold;");
                resultadoSimulador.getChildren().add(ya);
                return;
            }

            int meses = PortfolioSimulator.calcularMesesHastaObjetivo(actual, objetivo, rentAnual, aportacion);
            int anios = meses / 12, mesesResto = meses % 12;

            VBox resultado = new VBox(12);
            resultado.setPadding(new Insets(22));
            resultado.setStyle("-fx-background-color: " + panel() + "; -fx-background-radius: 14; " +
                    "-fx-border-color: " + border() + "; -fx-border-radius: 14; -fx-border-width: 1;");

            Label lTiempo = new Label(String.format("⏱  %d años y %d meses", anios, mesesResto));
            lTiempo.setStyle("-fx-text-fill: " + C_ACCENT + "; -fx-font-size: 20px; -fx-font-weight: bold;");

            for (Label l : new Label[]{
                    new Label(String.format("💰  Valor actual:       %.2f €", actual)),
                    new Label(String.format("🎯  Objetivo:           %.2f €", objetivo)),
                    new Label(String.format("📈  Falta por crecer:   %.2f €", objetivo - actual)),
                    new Label(String.format("💵  Aportación mensual: %.2f €", aportacion)),
                    new Label(String.format("📊  Rentabilidad anual: %.1f%%", rentAnual * 100))
            }) {
                l.setStyle("-fx-text-fill: " + texto() + "; -fx-font-size: 13px;");
                resultado.getChildren().add(l);
            }

            double progreso = Math.min(actual / objetivo, 1.0);
            ProgressBar barra = new ProgressBar(progreso);
            barra.setMaxWidth(Double.MAX_VALUE); barra.setPrefHeight(12);
            barra.setStyle("-fx-accent: " + C_SUCCESS + ";");
            Label lProg = new Label(String.format("%.1f%% completado", progreso * 100));
            lProg.setStyle("-fx-text-fill: " + sub() + "; -fx-font-size: 11px;");

            resultado.getChildren().addAll(0, List.of(lTiempo));
            resultado.getChildren().addAll(barra, lProg);
            resultadoSimulador.getChildren().add(resultado);

        } catch (NumberFormatException e) {
            Label err = new Label("⚠️  Introduce valores numéricos válidos.");
            err.setStyle("-fx-text-fill: " + C_DANGER + "; -fx-font-size: 13px;");
            resultadoSimulador.getChildren().add(err);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // IMPORTAR EXCEL
    // ─────────────────────────────────────────────────────────────────────────

    @FXML
    private void onImportarExcelClick() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Selecciona tu Portfolio_Dashboard.xlsx");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel", "*.xlsx"));
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File archivo = chooser.showOpenDialog(tablaActivos.getScene().getWindow());
        if (archivo != null) importarDesdeExcel(archivo);
    }

    private void importarDesdeExcel(File archivo) {
        try {
            List<Activo> nuevos = ExcelImporter.parsear(archivo);
            if (nuevos.isEmpty()) { mostrarError("No se encontraron activos válidos."); return; }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Importar Excel");
            confirm.setHeaderText("Se encontraron " + nuevos.size() + " activos");
            confirm.setContentText("¿Reemplazar la cartera actual o añadir?");
            ButtonType reemplazar = new ButtonType("Reemplazar");
            ButtonType anadir     = new ButtonType("Añadir");
            ButtonType cancelar   = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
            confirm.getButtonTypes().setAll(reemplazar, anadir, cancelar);
            confirm.showAndWait().ifPresent(r -> {
                if (r == reemplazar) { activos.clear(); activos.addAll(nuevos); }
                else if (r == anadir) { activos.addAll(nuevos); }
                refrescarTodo();
                guardarDatos();
                Alert ok = new Alert(Alert.AlertType.INFORMATION);
                ok.setTitle("Importación completada");
                ok.setHeaderText(nuevos.size() + " activos importados");
                ok.setContentText("Archivo: " + archivo.getName());
                ok.showAndWait();
            });
        } catch (Exception e) { mostrarError("Error: " + e.getMessage()); e.printStackTrace(); }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // EXPORTAR INFORME HTML
    // ─────────────────────────────────────────────────────────────────────────

    @FXML
    private void onExportarClick() {
        try {
            ReportExporter.exportar(activos, calcularMetricas());
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Informe exportado");
            info.setHeaderText("Informe HTML generado correctamente");
            info.setContentText("Guardado en:\n" + ReportExporter.getRutaInforme());
            info.showAndWait();
        } catch (Exception e) { e.printStackTrace(); mostrarError("Error al exportar: " + e.getMessage()); }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CRUD DE ACTIVOS
    // ─────────────────────────────────────────────────────────────────────────

    @FXML
    private void onAnadirClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("activo-dialog.fxml"));
            Stage dialog = new Stage();
            dialog.setScene(new Scene(loader.load()));
            dialog.setTitle("Nuevo activo");
            dialog.initOwner(tablaActivos.getScene().getWindow());
            dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialog.showAndWait();
            Activo nuevo = ((ActivoDialogController) loader.getController()).getResultado();
            if (nuevo != null) { activos.add(nuevo); refrescarTodo(); guardarDatos(); }
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void onEliminarClick() {
        Activo sel = tablaActivos.getSelectionModel().getSelectedItem();
        if (sel != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Eliminar activo");
            confirm.setHeaderText("¿Eliminar \"" + sel.getNombre() + "\"?");
            confirm.setContentText("Esta acción no se puede deshacer.");
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.OK) { activos.remove(sel); refrescarTodo(); guardarDatos(); }
            });
        }
    }

    private void onEditarActivo(Activo activo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("activo-dialog.fxml"));
            Stage dialog = new Stage();
            dialog.setScene(new Scene(loader.load()));
            dialog.setTitle("Editar activo");
            dialog.initOwner(tablaActivos.getScene().getWindow());
            dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            ActivoDialogController ctrl = loader.getController();
            ctrl.preRellenar(activo);
            dialog.showAndWait();
            Activo editado = ctrl.getResultado();
            if (editado != null) {
                activo.setNombre(editado.getNombre()); activo.setTipo(editado.getTipo());
                activo.setInvertido(editado.getInvertido()); activo.setValorActual(editado.getValorActual());
                activo.setFecha(editado.getFecha()); activo.setNotas(editado.getNotas());
                tablaActivos.refresh(); refrescarTodo(); guardarDatos();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }



    // ─────────────────────────────────────────────────────────────────────────
    // UTILIDADES
    // ─────────────────────────────────────────────────────────────────────────

    private String colorPorTipo(String tipo) { return PortfolioUtils.colorPorTipo(tipo); }
    private String traducirTipo(String tipo) { return PortfolioUtils.traducirTipo(tipo); }

    private void mostrarError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(msg);
        alert.showAndWait();
    }
}