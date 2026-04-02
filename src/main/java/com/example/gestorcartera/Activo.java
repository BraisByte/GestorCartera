package com.example.gestorcartera;

import javafx.beans.property.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Activo {
    private final StringProperty nombre;
    private final StringProperty tipo;
    private final DoubleProperty  invertido;
    private final DoubleProperty  valorActual;
    private final StringProperty fecha;        // fecha de compra (YYYY-MM-DD)
    private final StringProperty plataforma;
    private final StringProperty notas;
    private final StringProperty registradoEn; // ISO LocalDateTime completo

    // ── Constructor principal ─────────────────────────────────────────────────
    public Activo(String nombre, String tipo, double invertido, double valorActual,
                  String fecha, String plataforma, String notas, String registradoEn) {
        this.nombre       = new SimpleStringProperty(nombre);
        this.tipo         = new SimpleStringProperty(tipo);
        this.invertido    = new SimpleDoubleProperty(invertido);
        this.valorActual  = new SimpleDoubleProperty(valorActual);
        this.fecha        = new SimpleStringProperty(fecha != null && !fecha.isBlank()
                ? fecha : LocalDate.now().toString());
        this.plataforma   = new SimpleStringProperty(plataforma != null ? plataforma : "");
        this.notas        = new SimpleStringProperty(notas != null ? notas : "");
        this.registradoEn = new SimpleStringProperty(registradoEn != null && !registradoEn.isBlank()
                ? registradoEn : LocalDateTime.now().toString());
    }

    // ── Constructores de compatibilidad (no rompen el CSV antiguo) ────────────
    public Activo(String nombre, String tipo, double invertido, double valorActual,
                  String fecha, String plataforma, String notas) {
        this(nombre, tipo, invertido, valorActual, fecha, plataforma, notas,
                LocalDateTime.now().toString());
    }
    public Activo(String nombre, String tipo, double invertido, double valorActual,
                  String fecha, String plataforma) {
        this(nombre, tipo, invertido, valorActual, fecha, plataforma, "",
                LocalDateTime.now().toString());
    }
    public Activo(String nombre, String tipo, double invertido, double valorActual, String fecha) {
        this(nombre, tipo, invertido, valorActual, fecha, "", "",
                LocalDateTime.now().toString());
    }
    public Activo(String nombre, String tipo, double invertido, double valorActual) {
        this(nombre, tipo, invertido, valorActual, LocalDate.now().toString(), "", "",
                LocalDateTime.now().toString());
    }

    // ── Propiedades ───────────────────────────────────────────────────────────
    public String  getNombre()      { return nombre.get(); }
    public StringProperty nombreProperty() { return nombre; }
    public void    setNombre(String v)     { nombre.set(v); }

    public String  getTipo()        { return tipo.get(); }
    public StringProperty tipoProperty()   { return tipo; }
    public void    setTipo(String v)       { tipo.set(v); }

    public double  getInvertido()   { return invertido.get(); }
    public DoubleProperty invertidoProperty()  { return invertido; }
    public void    setInvertido(double v)      { invertido.set(v); }

    public double  getValorActual() { return valorActual.get(); }
    public DoubleProperty valorActualProperty() { return valorActual; }
    public void    setValorActual(double v)     { valorActual.set(v); }

    public String  getFecha()       { return fecha.get(); }
    public StringProperty fechaProperty()    { return fecha; }
    public void    setFecha(String v)        { fecha.set(v); }

    public String  getPlataforma()  { return plataforma.get(); }
    public StringProperty plataformaProperty() { return plataforma; }
    public void    setPlataforma(String v)    { plataforma.set(v); }

    public String  getNotas()       { return notas.get(); }
    public StringProperty notasProperty()    { return notas; }
    public void    setNotas(String v)        { notas.set(v); }

    public String  getRegistradoEn()         { return registradoEn.get(); }
    public StringProperty registradoEnProperty() { return registradoEn; }
    public void    setRegistradoEn(String v)  { registradoEn.set(v); }

    // ── Computed ──────────────────────────────────────────────────────────────
    public double getRentabilidad() {
        if (invertido.get() == 0) return 0;
        return ((valorActual.get() - invertido.get()) / invertido.get()) * 100;
    }

    /**
     * Devuelve el timestamp en formato legible "dd/MM/yyyy HH:mm"
     * para mostrar en tablas e informes.
     */
    public String getRegistradoEnFormateado() {
        try {
            String ts = registradoEn.get();
            if (ts == null || ts.isBlank()) return "-";
            // Formato ISO: 2024-01-15T14:30:22.123
            LocalDateTime dt = LocalDateTime.parse(ts.substring(0, Math.min(19, ts.length())));
            return dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        } catch (Exception e) {
            return registradoEn.get();
        }
    }
}