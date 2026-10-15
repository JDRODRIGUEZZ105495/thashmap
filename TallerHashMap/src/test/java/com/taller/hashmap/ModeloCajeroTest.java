package com.taller.hashmap;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModeloCajeroTest {
    /**
     * Comprueba que el cajero empieza con 100 billetes de cada tipo y el saldo esperado.
     */
    @Test
    void iniciaConCienBilletesDeCadaDenominacion() {
        ModeloCajero cajero = new ModeloCajero();

        assertEquals(Map.of(100_000, 100, 50_000, 100, 20_000, 100, 10_000, 100),
                cajero.obtenerInventario());
        assertEquals(18_000_000, cajero.obtenerSaldo());
    }

    /**
     * Confirma que un retiro válido entrega billetes y descuenta su cantidad del inventario.
     */
    @Test
    void retiraDineroYActualizaInventario() {
        ModeloCajero cajero = new ModeloCajero();

        ModeloCajero.ResultadoRetiro resultado = cajero.retirar(160_000);

        assertEquals(ModeloCajero.EstadoRetiro.EXITOSO, resultado.obtenerEstado());
        assertEquals(Map.of(100_000, 1, 50_000, 1, 20_000, 0, 10_000, 1),
                resultado.obtenerBilletes());
        assertEquals(99, cajero.obtenerInventario().get(100_000));
        assertEquals(99, cajero.obtenerInventario().get(50_000));
        assertEquals(99, cajero.obtenerInventario().get(10_000));
        assertEquals(17_840_000, cajero.obtenerSaldo());
    }

    /**
     * Verifica que no se acepte un monto que no sea múltiplo de diez ni se cambie el inventario.
     */
    @Test
    void rechazaMontosQueNoSeanMultiploDeDiez() {
        ModeloCajero cajero = new ModeloCajero();
        Map<Integer, Integer> inventarioAntes = cajero.obtenerInventario();

        ModeloCajero.ResultadoRetiro resultado = cajero.retirar(10_001);

        assertEquals(ModeloCajero.EstadoRetiro.MONTO_INVALIDO, resultado.obtenerEstado());
        assertEquals(inventarioAntes, cajero.obtenerInventario());
    }

    /**
     * Verifica que el cajero informe cuando las denominaciones no permiten formar el monto exacto.
     */
    @Test
    void rechazaMontosSinCombinacionExacta() {
        ModeloCajero cajero = new ModeloCajero();
        Map<Integer, Integer> inventarioAntes = cajero.obtenerInventario();

        ModeloCajero.ResultadoRetiro resultado = cajero.retirar(10_010);

        assertEquals(ModeloCajero.EstadoRetiro.SIN_COMBINACION_EXACTA, resultado.obtenerEstado());
        assertEquals(inventarioAntes, cajero.obtenerInventario());
    }

    /**
     * Comprueba que el algoritmo encuentre una combinación aunque elegir primero el billete mayor falle.
     */
    @Test
    void encuentraCombinacionDisponibleSinUsarUnaEleccionCodiciosa() {
        ModeloCajero cajero = new ModeloCajero(Map.of(10_000, 0, 20_000, 3, 50_000, 1));

        ModeloCajero.ResultadoRetiro resultado = cajero.retirar(60_000);

        assertEquals(ModeloCajero.EstadoRetiro.EXITOSO, resultado.obtenerEstado());
        assertEquals(Map.of(100_000, 0, 50_000, 0, 20_000, 3, 10_000, 0),
                resultado.obtenerBilletes());
        assertEquals(0, cajero.obtenerInventario().get(20_000));
        assertTrue(cajero.obtenerSaldo() >= 0);
        assertFalse(cajero.obtenerInventario().values().stream().anyMatch(cantidad -> cantidad < 0));
    }

    /**
     * Confirma que una solicitud mayor al saldo se rechace sin afectar los billetes disponibles.
     */
    @Test
    void noCambiaInventarioCuandoElSaldoEsInsuficiente() {
        ModeloCajero cajero = new ModeloCajero();
        Map<Integer, Integer> inventarioAntes = cajero.obtenerInventario();

        ModeloCajero.ResultadoRetiro resultado = cajero.retirar(18_010_000);

        assertEquals(ModeloCajero.EstadoRetiro.SALDO_INSUFICIENTE, resultado.obtenerEstado());
        assertEquals(inventarioAntes, cajero.obtenerInventario());
    }
}
