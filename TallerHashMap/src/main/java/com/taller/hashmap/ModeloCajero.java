package com.taller.hashmap;

import gnu.trove.map.hash.TIntIntHashMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Mantiene el inventario de billetes y calcula cómo entregar cada retiro.
 */
public final class ModeloCajero {
    private static final int[] DENOMINACIONES = {100_000, 50_000, 20_000, 10_000};
    private static final int UNIDADES_POR_BILLETE_MENOR = 10_000;
    private final TIntIntHashMap inventario = new TIntIntHashMap();

    /**
     * Crea un cajero con 100 billetes disponibles de cada denominación.
     */
    public ModeloCajero() {
        for (int denominacion : DENOMINACIONES) {
            inventario.put(denominacion, 100);
        }
    }

    /**
     * Crea un cajero con un inventario específico, principalmente para pruebas.
     * Las denominaciones desconocidas y las cantidades negativas no son válidas.
     *
     * @param inventarioInicial billetes disponibles por denominación
     */
    ModeloCajero(Map<Integer, Integer> inventarioInicial) {
        for (int denominacion : DENOMINACIONES) {
            int cantidad = inventarioInicial.getOrDefault(denominacion, 0);
            if (cantidad < 0) {
                throw new IllegalArgumentException("La cantidad de billetes no puede ser negativa.");
            }
            inventario.put(denominacion, cantidad);
        }
        for (int denominacion : inventarioInicial.keySet()) {
            if (!inventario.containsKey(denominacion)) {
                throw new IllegalArgumentException("Denominación no reconocida: " + denominacion);
            }
        }
    }

    /**
     * Devuelve una copia de solo lectura para que el inventario no se pueda alterar desde afuera.
     *
     * @return denominaciones y cantidades de billetes disponibles
     */
    public synchronized Map<Integer, Integer> obtenerInventario() {
        Map<Integer, Integer> copiaInventario = new LinkedHashMap<>();
        for (int denominacion : DENOMINACIONES) {
            copiaInventario.put(denominacion, inventario.get(denominacion));
        }
        return Collections.unmodifiableMap(copiaInventario);
    }

    /**
     * Suma el valor de todos los billetes que todavía están disponibles.
     *
     * @return saldo total del cajero
     */
    public synchronized int obtenerSaldo() {
        int saldo = 0;
        for (int denominacion : DENOMINACIONES) {
            saldo += denominacion * inventario.get(denominacion);
        }
        return saldo;
    }

    /**
     * Valida la solicitud, busca una combinación exacta y descuenta los billetes si es posible.
     *
     * @param monto cantidad que se quiere retirar
     * @return resultado del retiro y billetes que se entregarían
     */
    public synchronized ResultadoRetiro retirar(int monto) {
        if (monto <= 0 || monto % 10 != 0) {
            return ResultadoRetiro.fallido(EstadoRetiro.MONTO_INVALIDO, monto);
        }
        if (monto > obtenerSaldo()) {
            return ResultadoRetiro.fallido(EstadoRetiro.SALDO_INSUFICIENTE, monto);
        }
        if (monto % UNIDADES_POR_BILLETE_MENOR != 0) {
            return ResultadoRetiro.fallido(EstadoRetiro.SIN_COMBINACION_EXACTA, monto);
        }

        Map<Integer, Integer> planBilletes = buscarCombinacion(monto / UNIDADES_POR_BILLETE_MENOR);
        if (planBilletes == null) {
            return ResultadoRetiro.fallido(EstadoRetiro.SIN_COMBINACION_EXACTA, monto);
        }

        // El inventario solo se descuenta después de confirmar que existe una combinación completa.
        for (int denominacion : DENOMINACIONES) {
            inventario.put(denominacion, inventario.get(denominacion) - planBilletes.get(denominacion));
        }
        return ResultadoRetiro.exitoso(monto, planBilletes);
    }

    /**
     * Busca una combinación de billetes usando programación dinámica acotada por el inventario.
     *
     * @param unidadesObjetivo monto expresado en unidades de 10.000
     * @return cantidad de billetes por denominación, o {@code null} si no hay combinación exacta
     */
    private Map<Integer, Integer> buscarCombinacion(int unidadesObjetivo) {
        int cantidadDenominaciones = DENOMINACIONES.length;
        int inalcanzable = Integer.MAX_VALUE / 4;
        int[][] minimoBilletes = new int[cantidadDenominaciones + 1][unidadesObjetivo + 1];
        int[][] billetesElegidos = new int[cantidadDenominaciones + 1][unidadesObjetivo + 1];

        // Al inicio, ninguna suma es posible salvo cero, que requiere cero billetes.
        for (int[] fila : minimoBilletes) {
            Arrays.fill(fila, inalcanzable);
        }
        minimoBilletes[0][0] = 0;

        // Cada fila calcula qué sumas se pueden formar al considerar una denominación adicional.
        for (int indice = 0; indice < cantidadDenominaciones; indice++) {
            int denominacion = DENOMINACIONES[indice];
            int unidadesPorBillete = denominacion / UNIDADES_POR_BILLETE_MENOR;
            int disponibles = inventario.get(denominacion);
            for (int unidadesPrevias = 0; unidadesPrevias <= unidadesObjetivo; unidadesPrevias++) {
                if (minimoBilletes[indice][unidadesPrevias] == inalcanzable) {
                    continue;
                }
                int maximoBilletes = Math.min(disponibles,
                        (unidadesObjetivo - unidadesPrevias) / unidadesPorBillete);
                for (int cantidad = 0; cantidad <= maximoBilletes; cantidad++) {
                    int nuevasUnidades = unidadesPrevias + cantidad * unidadesPorBillete;
                    int totalBilletes = minimoBilletes[indice][unidadesPrevias] + cantidad;
                    if (totalBilletes < minimoBilletes[indice + 1][nuevasUnidades]) {
                        minimoBilletes[indice + 1][nuevasUnidades] = totalBilletes;
                        billetesElegidos[indice + 1][nuevasUnidades] = cantidad;
                    }
                }
            }
        }

        // Si el objetivo quedó marcado como inalcanzable, no existe entrega exacta.
        if (minimoBilletes[cantidadDenominaciones][unidadesObjetivo] == inalcanzable) {
            return null;
        }

        // Recorre las decisiones al revés para reconstruir cuántos billetes de cada tipo se usarán.
        Map<Integer, Integer> planBilletes = new LinkedHashMap<>();
        int unidadesRestantes = unidadesObjetivo;
        for (int indice = cantidadDenominaciones; indice > 0; indice--) {
            int denominacion = DENOMINACIONES[indice - 1];
            int cantidad = billetesElegidos[indice][unidadesRestantes];
            planBilletes.put(denominacion, cantidad);
            unidadesRestantes -= cantidad * (denominacion / UNIDADES_POR_BILLETE_MENOR);
        }
        return planBilletes;
    }

    /**
     * Describimos el resultado posible de una solicitud de retiro.
     */
    public enum EstadoRetiro {
        EXITOSO,
        MONTO_INVALIDO,
        SALDO_INSUFICIENTE,
        SIN_COMBINACION_EXACTA
    }

    /**
     * Agrupa el estado del retiro, el monto solicitado y los billetes de la entrega.
     */
    public static final class ResultadoRetiro {
        private final EstadoRetiro estado;
        private final int monto;
        private final Map<Integer, Integer> billetes;

        /**
         * Guarda una copia inmutable de los datos de este resultado.
         */
        private ResultadoRetiro(EstadoRetiro estado, int monto, Map<Integer, Integer> billetes) {
            this.estado = estado;
            this.monto = monto;
            this.billetes = Collections.unmodifiableMap(new LinkedHashMap<>(billetes));
        }

        /**
         * Construye el resultado para un retiro que sí se pudo completar.
         */
        private static ResultadoRetiro exitoso(int monto, Map<Integer, Integer> billetes) {
            return new ResultadoRetiro(EstadoRetiro.EXITOSO, monto, billetes);
        }

        /**
         * Construye un resultado fallido sin indicar billetes entregados.
         */
        private static ResultadoRetiro fallido(EstadoRetiro estado, int monto) {
            return new ResultadoRetiro(estado, monto, Map.of());
        }

        /**
         * Indica si el retiro tuvo éxito, fue inválido o no pudo atenderse.
         */
        public EstadoRetiro obtenerEstado() {
            return estado;
        }

        /**
         * Devuelve el monto que se intentó retirar.
         */
        public int obtenerMonto() {
            return monto;
        }

        /**
         * Devuelve la cantidad de billetes por denominación del resultado.
         */
        public Map<Integer, Integer> obtenerBilletes() {
            return billetes;
        }
    }
}
