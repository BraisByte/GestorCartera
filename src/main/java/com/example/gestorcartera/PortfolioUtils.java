package com.example.gestorcartera;

public class PortfolioUtils {

    public static String colorPorTipo(String tipo) {
        if (tipo == null) return "#424949";
        return switch (tipo.replaceAll("[^a-zA-Z ]", "").trim()) {
            case "ETF"            -> "#1a5276";
            case "Crypto"         -> "#7d6608";
            case "Stock"          -> "#1e8449";
            case "Fondo indexado" -> "#6c3483";
            case "Fund Equity"    -> "#6c3483";
            case "Fund Bond"      -> "#2e86c1";
            case "Money Market"   -> "#117a65";
            case "Commodity"      -> "#784212";
            default               -> "#424949";
        };
    }

    public static String traducirTipo(String tipo) {
        if (tipo == null) return "Otro";
        return switch (tipo.replaceAll("[^a-zA-Z ]", "").trim()) {
            case "ETF"            -> "ETF";
            case "Crypto"         -> "Cripto";
            case "Stock"          -> "Acciones";
            case "Fondo indexado" -> "Fondo indexado";
            case "Fund Equity"    -> "Fondo renta variable";
            case "Fund Bond"      -> "Fondo renta fija";
            case "Money Market"   -> "Mercado monetario";
            case "Commodity"      -> "Materias primas";
            default               -> tipo;
        };
    }

    public static String limpiarTexto(String s) {
        if (s == null) return "";
        return s.replaceAll("[^\\p{L}\\p{N}\\s/]", "").trim();
    }
}