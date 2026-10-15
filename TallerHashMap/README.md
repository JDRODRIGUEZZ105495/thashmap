# Cajero automático con Trove

Aplicación de escritorio en Java Swing para simular retiros. El inventario de billetes se almacena en un `TIntIntHashMap` de Trove4j, con 100 billetes iniciales de cada denominación: $10.000, $20.000, $50.000 y $100.000.

El valor se ingresa con el teclado en pantalla. Los retiros deben ser positivos, múltiplos de 10 y contar con una combinación exacta de billetes disponibles; al completarse, se actualizan las existencias y el total de cada denominación.

## Clases principales

- `CajeroApp` dibuja la ventana, procesa las acciones de los botones y presenta los resultados.
- `ModeloCajero` guarda las cantidades en un mapa y valida los retiros.
- `ModeloCajero.buscarCombinacion` utiliza programación dinámica para encontrar una combinación exacta con los billetes disponibles.

Los nombres de clases, métodos y resultados están en español para que el flujo del programa sea más fácil de estudiar. Los comentarios del código explican qué responsabilidad tiene cada método y cómo se calcula la combinación de billetes.

## Requisitos

- JDK 17
- Maven 3.8 o posterior

## Ejecutar

Desde la carpeta del proyecto:

```text
mvn clean test
mvn exec:java
```

Maven descarga Trove4j y las dependencias de prueba desde Maven Central.
