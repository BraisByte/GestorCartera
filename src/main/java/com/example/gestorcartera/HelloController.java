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
    private DashboardBuilder dashboardBuilder;
    private ChartsManager chartsManager;

    // ─────────────────────────────────────────────────────────────────────────
    // INICIALIZACIÓN
    // ─────────────────────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        dashboardBuilder = new DashboardBuilder(dashboardContainer, objetivosContainer, activos, ARCHIVO_HISTORICO);
        chartsManager    = new ChartsManager(graficoPorTipo, graficaComparativa, graficaHistorico, evolucionContent, activos, ARCHIVO_HISTORICO);
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
                            ? "-fx-text-fill: " + Theme.SUCCESS + "; -fx-font-weight: bold;"
                            : "-fx-text-fill: " + Theme.DANGER  + "; -fx-font-weight: bold;");
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
            { bar.setMaxWidth(Double.MAX_VALUE); bar.setStyle("-fx-accent: " + Theme.ACCENT + ";"); }
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
            String rowHover = "-fx-background-color: " + Theme.card() + "; -fx-font-size: 12px;";
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
        aplicarEstiloBoton(btnAnadir,
                "#7c6af7", "#9b8cf9", "rgba(124,106,247,0.5)");
        aplicarEstiloBoton(btnImportar,
                "#2563eb", "#3b82f6", "rgba(37,99,235,0.5)");
        aplicarEstiloBoton(btnExportar,
                "#16a34a", "#22c55e", "rgba(22,163,74,0.5)");

        botonEliminar.setDisable(true);
        actualizarEstiloEliminar(false);
        tablaActivos.getSelectionModel().selectedItemProperty().addListener((obs, a, n) -> {
            boolean sel = n != null;
            botonEliminar.setDisable(!sel);
            actualizarEstiloEliminar(sel);
        });
    }

    private void aplicarEstiloBoton(Button btn, String color, String colorHover, String sombra) {
        String base = "-fx-background-color: " + color + "; -fx-text-fill: white; " +
                "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 11 22; " +
                "-fx-background-radius: 9; -fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, " + sombra + ", 10, 0, 0, 3);";
        String hover = "-fx-background-color: " + colorHover + "; -fx-text-fill: white; " +
                "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 11 22; " +
                "-fx-background-radius: 9; -fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, " + sombra + ", 16, 0, 0, 5);";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(base));
    }

    private void actualizarEstiloEliminar(boolean activo) {
        if (activo) {
            String base = "-fx-background-color: #dc2626; -fx-text-fill: white; " +
                    "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 11 22; " +
                    "-fx-background-radius: 9; -fx-cursor: hand; " +
                    "-fx-effect: dropshadow(gaussian, rgba(220,38,38,0.5), 10, 0, 0, 3);";
            String hover = "-fx-background-color: #ef4444; -fx-text-fill: white; " +
                    "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 11 22; " +
                    "-fx-background-radius: 9; -fx-cursor: hand; " +
                    "-fx-effect: dropshadow(gaussian, rgba(220,38,38,0.65), 16, 0, 0, 5);";
            botonEliminar.setStyle(base);
            botonEliminar.setOnMouseEntered(ev -> botonEliminar.setStyle(hover));
            botonEliminar.setOnMouseExited(ev -> botonEliminar.setStyle(base));
        } else {
            botonEliminar.setStyle(
                    "-fx-background-color: #3a1a1a; -fx-text-fill: #664444; " +
                    "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 11 22; " +
                    "-fx-background-radius: 9; -fx-border-color: #5a2a2a; " +
                    "-fx-border-radius: 9; -fx-border-width: 1;");
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
    // DASHBOARD (delegado a DashboardBuilder)
    // ─────────────────────────────────────────────────────────────────────────

    private void actualizarDashboard() {
        dashboardBuilder.actualizarDashboard();
    }

    private void actualizarObjetivos() {
        dashboardBuilder.actualizarObjetivos();
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
                ? "-fx-text-fill: " + Theme.SUCCESS + "; -fx-font-size: 15px; -fx-font-weight: bold;"
                : "-fx-text-fill: " + Theme.DANGER  + "; -fx-font-size: 15px; -fx-font-weight: bold;");
        labelTotal.setText(String.format("Total: %.2f €", totalAct));
        labelHoraActualizacion.setText("Actualizado: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GRÁFICAS (delegado a ChartsManager)
    // ─────────────────────────────────────────────────────────────────────────

    private void actualizarGrafico()            { chartsManager.actualizarGrafico(); }
    private void actualizarGraficaComparativa() { chartsManager.actualizarGraficaComparativa(); }
    private void cargarGraficaHistorico()       { chartsManager.cargarGraficaHistorico(); }

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
                ya.setStyle("-fx-text-fill: " + Theme.SUCCESS + "; -fx-font-size: 16px; -fx-font-weight: bold;");
                resultadoSimulador.getChildren().add(ya);
                return;
            }

            int meses = PortfolioSimulator.calcularMesesHastaObjetivo(actual, objetivo, rentAnual, aportacion);
            int anios = meses / 12, mesesResto = meses % 12;

            VBox resultado = new VBox(12);
            resultado.setPadding(new Insets(22));
            resultado.setStyle("-fx-background-color: " + Theme.panel() + "; -fx-background-radius: 14; " +
                    "-fx-border-color: " + Theme.border() + "; -fx-border-radius: 14; -fx-border-width: 1;");

            Label lTiempo = new Label(String.format("⏱  %d años y %d meses", anios, mesesResto));
            lTiempo.setStyle("-fx-text-fill: " + Theme.ACCENT + "; -fx-font-size: 20px; -fx-font-weight: bold;");

            for (Label l : new Label[]{
                    new Label(String.format("💰  Valor actual:       %.2f €", actual)),
                    new Label(String.format("🎯  Objetivo:           %.2f €", objetivo)),
                    new Label(String.format("📈  Falta por crecer:   %.2f €", objetivo - actual)),
                    new Label(String.format("💵  Aportación mensual: %.2f €", aportacion)),
                    new Label(String.format("📊  Rentabilidad anual: %.1f%%", rentAnual * 100))
            }) {
                l.setStyle("-fx-text-fill: " + Theme.texto() + "; -fx-font-size: 13px;");
                resultado.getChildren().add(l);
            }

            double progreso = Math.min(actual / objetivo, 1.0);
            ProgressBar barra = new ProgressBar(progreso);
            barra.setMaxWidth(Double.MAX_VALUE); barra.setPrefHeight(12);
            barra.setStyle("-fx-accent: " + Theme.SUCCESS + ";");
            Label lProg = new Label(String.format("%.1f%% completado", progreso * 100));
            lProg.setStyle("-fx-text-fill: " + Theme.sub() + "; -fx-font-size: 11px;");

            resultado.getChildren().addAll(0, List.of(lTiempo));
            resultado.getChildren().addAll(barra, lProg);
            resultadoSimulador.getChildren().add(resultado);

        } catch (NumberFormatException e) {
            Label err = new Label("⚠️  Introduce valores numéricos válidos.");
            err.setStyle("-fx-text-fill: " + Theme.DANGER + "; -fx-font-size: 13px;");
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
            ReportExporter.exportar(activos, MetricsService.calcular(ARCHIVO_HISTORICO));
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
    private String traducirTipo(String tipo)  { return PortfolioUtils.traducirTipo(tipo); }

    private void mostrarError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(msg);
        alert.showAndWait();
    }
}