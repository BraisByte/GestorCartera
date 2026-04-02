package com.example.gestorcartera;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.time.LocalDate;

public class ActivoDialogController {

    @FXML private TextField campoNombre;
    @FXML private ComboBox<String> campoTipo;
    @FXML private TextField campoInvertido;
    @FXML private TextField campoValorActual;
    @FXML private DatePicker campoFecha;
    @FXML private TextArea campoNotas;
    @FXML private Label labelError;

    private Activo resultado = null;

    @FXML
    public void initialize() {
        campoTipo.getItems().addAll("ETF", "Acción", "Crypto", "Fondo indexado",
                "Fund Equity", "Fund Bond", "Money Market", "Commodity", "Stock", "Otro");
        campoTipo.setValue("ETF");
        campoFecha.setValue(LocalDate.now());
    }

    public void preRellenar(Activo activo) {
        campoNombre.setText(activo.getNombre());
        campoTipo.setValue(activo.getTipo());
        campoInvertido.setText(String.valueOf(activo.getInvertido()));
        campoValorActual.setText(String.valueOf(activo.getValorActual()));
        campoNotas.setText(activo.getNotas());
        try {
            campoFecha.setValue(LocalDate.parse(activo.getFecha()));
        } catch (Exception e) {
            campoFecha.setValue(LocalDate.now());
        }
    }

    @FXML
    private void onConfirmar() {
        String nombre = campoNombre.getText().trim();
        String tipo   = campoTipo.getValue();
        String invStr = campoInvertido.getText().trim();
        String valStr = campoValorActual.getText().trim();
        LocalDate fecha = campoFecha.getValue();

        if (nombre.isEmpty() || invStr.isEmpty() || valStr.isEmpty() || fecha == null) {
            labelError.setText("Por favor rellena todos los campos obligatorios.");
            return;
        }
        try {
            double invertido   = Double.parseDouble(invStr.replace(",", "."));
            double valorActual = Double.parseDouble(valStr.replace(",", "."));
            resultado = new Activo(nombre, tipo, invertido, valorActual,
                    fecha.toString(), "", campoNotas.getText().trim());
            cerrar();
        } catch (NumberFormatException e) {
            labelError.setText("Los importes deben ser números (ej: 1500.00)");
        }
    }

    @FXML
    private void onCancelar() { cerrar(); }

    private void cerrar() {
        ((Stage) campoNombre.getScene().getWindow()).close();
    }

    public Activo getResultado() { return resultado; }
}