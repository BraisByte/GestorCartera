# 📊 GestorCartera

Aplicación de escritorio para la gestión personal de carteras de inversión, desarrollada en **JavaFX**. Permite registrar, visualizar y analizar activos financieros (ETFs, fondos, acciones, cripto, materias primas, etc.) con una interfaz oscura moderna, gráficas interactivas y métricas avanzadas de riesgo.

---

## ✨ Funcionalidades

### 🗂️ Gestión de activos
- Añadir, editar y eliminar activos con nombre, tipo, capital invertido, valor actual, fecha de compra, plataforma y notas
- Edición inline del valor actual directamente en la tabla
- Filtros por tipo, plataforma y búsqueda por nombre

### 📈 Dashboard
- Tarjetas resumen con total invertido, valor actual, beneficio/pérdida y rentabilidad global
- Mini estadísticas: activos en positivo/negativo, mejor y peor activo
- Métricas avanzadas: rentabilidad anualizada, volatilidad anual (σ) y ratio Sharpe
- Distribución por tipo con barras de progreso
- Tarjetas individuales por activo con animación fade-in

### 📉 Gráficas
- Gráfico de tarta por tipo de activo con tooltips
- Gráfica comparativa invertido vs. valor actual por activo
- Gráfica de línea del histórico de evolución del portfolio con estadísticas del período (valor inicial/actual, variación, máximo, mínimo)

### 🎯 Objetivos
- Comparativa de distribución actual vs. objetivos predefinidos por tipo de activo
- Indicador visual de desviación (verde / amarillo / rojo)

### 🔮 Simulador
- Calcula los meses/años necesarios para alcanzar un objetivo dado capital actual, aportación mensual y rentabilidad esperada
- Barra de progreso hacia el objetivo

### 📁 Importación / Exportación
- Importar cartera desde Excel (`.xlsx`) con opción de reemplazar o añadir activos
- Exportar informe completo en HTML con KPIs, distribución, top 5 / peores 5, métricas de riesgo y tabla detallada

### 💾 Persistencia
- Guardado automático en CSV (`~/gestorcartera.csv`)
- Histórico diario de valor total (`~/gestorcartera_historico.csv`) para calcular métricas a lo largo del tiempo

### 🎨 UI / UX
- Tema oscuro con paleta unificada
- Reloj en tiempo real y fecha en la cabecera
- Animaciones fade entre pestañas y en las tarjetas del dashboard
- Hover effects en botones, filas de tabla y tarjetas

---

## 🛠️ Tecnologías

- Java 17+
- JavaFX
- Apache POI (importación Excel)
- Maven

---

## 🚀 Ejecución

```bash
./mvnw javafx:run
```

---

## 📌 Estado del proyecto

En desarrollo activo. Proyecto de uso personal.
