package com.example.gestorcartera;

public class PortfolioSimulator {

    /**
     * Calcula los meses necesarios para alcanzar {@code objetivo} partiendo
     * de {@code actual}, añadiendo {@code aportacionMensual} cada mes y con
     * una rentabilidad anual de {@code rentAnual} (valor en tanto por uno).
     *
     * @return 0 si ya se ha alcanzado el objetivo; número de meses en otro caso.
     *         Limitado a 600 meses para evitar bucles infinitos.
     */
    public static int calcularMesesHastaObjetivo(double actual, double objetivo,
                                                  double rentAnual, double aportacionMensual) {
        if (actual >= objetivo) return 0;
        double rentMensual = Math.pow(1 + rentAnual, 1.0 / 12) - 1;
        double capital = actual;
        int meses = 0;
        while (capital < objetivo && meses < 600) {
            capital = capital * (1 + rentMensual) + aportacionMensual;
            meses++;
        }
        return meses;
    }
}