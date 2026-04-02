package com.example.gestorcartera;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ExcelImporter {

    /**
     * Parsea la hoja "Holdings" de un archivo Excel y devuelve la lista de activos.
     *
     * @throws IllegalArgumentException si la hoja "Holdings" no existe en el archivo.
     */
    public static List<Activo> parsear(File archivo) throws Exception {
        try (Workbook wb = new XSSFWorkbook(new FileInputStream(archivo))) {
            Sheet holdings = wb.getSheet("Holdings");
            if (holdings == null) throw new IllegalArgumentException("No se encontró la hoja 'Holdings'.");

            List<Activo> nuevos = new ArrayList<>();
            for (Row row : holdings) {
                if (row.getRowNum() < 2) continue;
                Cell celdaNombre = row.getCell(2);
                if (celdaNombre == null || celdaNombre.getCellType() == CellType.BLANK) continue;
                String nombre = celdaNombre.getStringCellValue().trim();
                if (nombre.isEmpty() || nombre.startsWith("▶")) continue;

                Cell celdaPlat  = row.getCell(0);
                Cell celdaTipo  = row.getCell(1);
                Cell celdaValor = row.getCell(6);
                Cell celdaRent  = row.getCell(7);

                String plataforma = celdaPlat != null ? PortfolioUtils.limpiarTexto(celdaPlat.getStringCellValue()) : "";
                String tipo       = celdaTipo != null ? PortfolioUtils.limpiarTexto(celdaTipo.getStringCellValue()) : "Otro";
                double valor = celdaValor != null && celdaValor.getCellType() == CellType.NUMERIC ? celdaValor.getNumericCellValue() : 0;
                double rent  = celdaRent  != null && celdaRent.getCellType()  == CellType.NUMERIC ? celdaRent.getNumericCellValue()  : 0;

                if (valor <= 0) continue;
                double invertido = rent != 0 ? valor / (1 + rent) : valor;
                nuevos.add(new Activo(nombre, tipo, invertido, valor, LocalDate.now().toString(), plataforma, ""));
            }
            return nuevos;
        }
    }
}