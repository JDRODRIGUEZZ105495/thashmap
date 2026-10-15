package com.taller.hashmap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * Presenta la interfaz gráfica y conecta sus controles con el modelo del cajero.
 */
public final class CajeroApp {
    private static final int[] DENOMINACIONES = {10_000, 20_000, 50_000, 100_000};

    private final ModeloCajero cajero = new ModeloCajero();
    private final NumberFormat formatoNumerico = NumberFormat.getIntegerInstance(Locale.forLanguageTag("es-CO"));
    private final JTextField campoMonto = new JTextField();
    private final JLabel etiquetaMensaje =
            new JLabel("Ingrese el valor y seleccione Retirar.", SwingConstants.CENTER);
    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"Denominación", "Billetes disponibles", "Valor total"}, 0) {
        /**
         * Evita que el usuario edite directamente los datos del inventario en la tabla.
         */
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };

    /**
     * Constructor privado: la ventana se crea desde el punto de entrada {@code main}.
     */
    private CajeroApp() {
    }

    /**
     * Inicia la interfaz en el hilo de eventos que Swing reserva para dibujar ventanas.
     *
     * @param argumentos opciones recibidas al iniciar la aplicación
     */
    public static void main(String[] argumentos) {
        SwingUtilities.invokeLater(() -> new CajeroApp().mostrarVentana());
    }

    /**
     * Crea la ventana principal, configura su tamaño y la muestra centrada.
     */
    private void mostrarVentana() {
        JFrame ventana = new JFrame("Cajero automático");
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setContentPane(crearContenido());
        ventana.pack();
        ventana.setMinimumSize(new Dimension(560, 700));
        ventana.setSize(new Dimension(
                Math.max(620, ventana.getWidth()),
                Math.max(780, ventana.getHeight())));
        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);
    }

    /**
     * Construye y organiza los campos, botones, tabla y mensajes de la aplicación.
     *
     * @return panel principal que se instala dentro de la ventana
     */
    private JPanel crearContenido() {
        JPanel contenido = new JPanel(new BorderLayout(12, 12));
        contenido.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        contenido.setBackground(new Color(241, 245, 249));

        JLabel titulo = new JLabel("Cajero automático", SwingConstants.CENTER);
        titulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        titulo.setForeground(new Color(30, 41, 59));
        contenido.add(titulo, BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(10, 10));
        centro.setOpaque(false);
        campoMonto.setEditable(false);
        campoMonto.setHorizontalAlignment(SwingConstants.RIGHT);
        campoMonto.setFont(new Font(Font.MONOSPACED, Font.BOLD, 28));
        campoMonto.setBorder(BorderFactory.createTitledBorder("Valor a retirar"));
        campoMonto.setText("$ 0");
        campoMonto.setPreferredSize(new Dimension(480, 72));
        centro.add(campoMonto, BorderLayout.NORTH);

        JPanel teclado = new JPanel(new GridLayout(4, 3, 12, 12));
        teclado.setOpaque(false);
        teclado.setPreferredSize(new Dimension(480, 380));
        for (String tecla : new String[]{
                "1", "2", "3", "4", "5", "6", "7", "8", "9", "Limpiar", "0", "⌫"
        }) {
            JButton boton = new JButton(tecla);
            estilizarBoton(boton, new Color(226, 232, 240), new Color(30, 41, 59), 24);
            boton.addActionListener(evento -> procesarTecla(tecla));
            teclado.add(boton);
        }

        JPanel controles = new JPanel(new GridLayout(1, 2, 8, 8));
        controles.setOpaque(false);
        JButton botonRetirar = new JButton("Retirar");
        estilizarBoton(botonRetirar, new Color(22, 101, 52), Color.WHITE, 16);
        botonRetirar.addActionListener(evento -> retirarDinero());
        JButton botonLimpiar = new JButton("Limpiar pantalla");
        estilizarBoton(botonLimpiar, new Color(203, 213, 225), new Color(30, 41, 59), 15);
        botonLimpiar.addActionListener(evento -> limpiarPantalla());
        controles.add(botonRetirar);
        controles.add(botonLimpiar);

        JPanel panelEntrada = new JPanel(new BorderLayout(8, 8));
        panelEntrada.setOpaque(false);
        panelEntrada.add(teclado, BorderLayout.CENTER);
        panelEntrada.add(controles, BorderLayout.SOUTH);
        centro.add(panelEntrada, BorderLayout.CENTER);

        JPanel panelInventario = new JPanel(new BorderLayout(6, 6));
        panelInventario.setOpaque(false);
        JLabel tituloInventario = new JLabel("Billetes disponibles");
        tituloInventario.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        panelInventario.add(tituloInventario, BorderLayout.NORTH);
        JTable tablaInventario = new JTable(modeloTabla);
        tablaInventario.setRowHeight(21);
        tablaInventario.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        tablaInventario.setFillsViewportHeight(true);
        JScrollPane desplazamientoTabla = new JScrollPane(tablaInventario);
        desplazamientoTabla.setPreferredSize(new Dimension(480, 88));
        panelInventario.add(desplazamientoTabla, BorderLayout.CENTER);
        panelInventario.setPreferredSize(new Dimension(480, 120));
        centro.add(panelInventario, BorderLayout.SOUTH);
        contenido.add(centro, BorderLayout.CENTER);

        etiquetaMensaje.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        contenido.add(etiquetaMensaje, BorderLayout.SOUTH);
        actualizarTablaInventario();
        return contenido;
    }

    private void estilizarBoton(JButton boton, Color fondo, Color texto, int tamanoFuente) {
        boton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, tamanoFuente));
        boton.setForeground(texto);
        boton.setBackground(fondo);
        boton.setOpaque(true);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(148, 163, 184), 1, true),
                BorderFactory.createEmptyBorder(10, 8, 10, 8)));
    }

    /**
     * Interpreta una tecla del teclado gráfico para agregar, borrar o limpiar el monto.
     *
     * @param tecla número o acción asociada al botón presionado
     */
    private void procesarTecla(String tecla) {
        if ("Limpiar".equals(tecla)) {
            limpiarPantalla();
        } else if ("⌫".equals(tecla)) {
            String montoActual = campoMonto.getText();
            String digitos = montoActual.replaceAll("\\D", "");
            if (!digitos.isEmpty()) {
                actualizarMonto(digitos.substring(0, digitos.length() - 1));
            }
        } else {
            String digitosActuales = campoMonto.getText().replaceAll("\\D", "");
            if (digitosActuales.length() >= 10) {
                mostrarError("El valor ingresado supera el límite permitido.");
                return;
            }
            actualizarMonto(digitosActuales + tecla);
        }
    }

    /**
     * Da formato a los dígitos ingresados y muestra un mensaje cuando el valor no cabe en un entero.
     *
     * @param digitos monto sin separadores de miles
     */
    private void actualizarMonto(String digitos) {
        if (digitos.isEmpty()) {
            campoMonto.setText("$ 0");
            return;
        }
        try {
            long monto = Long.parseLong(digitos);
            if (monto > Integer.MAX_VALUE) {
                mostrarError("El valor ingresado supera el límite permitido.");
                return;
            }
            campoMonto.setText("$ " + formatoNumerico.format(monto));
            etiquetaMensaje.setText("Ingrese el valor y seleccione Retirar.");
            etiquetaMensaje.setForeground(new Color(30, 41, 59));
        } catch (NumberFormatException excepcion) {
            mostrarError("El valor ingresado supera el límite permitido.");
        }
    }

    /**
     * Envía el monto al modelo y presenta el resultado o la causa por la que no se pudo retirar.
     */
    private void retirarDinero() {
        String digitos = campoMonto.getText().replaceAll("\\D", "");
        int monto;
        try {
            monto = digitos.isEmpty() ? 0 : Integer.parseInt(digitos);
        } catch (NumberFormatException excepcion) {
            mostrarAlertaRetiro("El valor ingresado supera el límite permitido.");
            return;
        }

        ModeloCajero.ResultadoRetiro resultado = cajero.retirar(monto);
        switch (resultado.obtenerEstado()) {
            case EXITOSO -> {
                etiquetaMensaje.setText("Retiro exitoso: " + formatearDinero(monto)
                        + " | Billetes: " + describirBilletes(resultado.obtenerBilletes()));
                etiquetaMensaje.setForeground(new Color(22, 101, 52));
                campoMonto.setText("$ 0");
                actualizarTablaInventario();
            }
            case MONTO_INVALIDO ->
                    mostrarAlertaRetiro("Ingrese un valor positivo en múltiplos de 10.");
            case SALDO_INSUFICIENTE ->
                    mostrarAlertaRetiro("Fondos insuficientes. Disponible: "
                            + formatearDinero(cajero.obtenerSaldo()));
            case SIN_COMBINACION_EXACTA ->
                    mostrarAlertaRetiro("No hay una combinación exacta de billetes para ese valor.");
        }
    }

    private void mostrarError(String mensaje) {
        etiquetaMensaje.setText(mensaje);
        etiquetaMensaje.setForeground(new Color(185, 28, 28));
    }

    private void mostrarAlertaRetiro(String mensaje) {
        mostrarError(mensaje);
        JOptionPane.showMessageDialog(
                etiquetaMensaje, mensaje, "No se pudo realizar el retiro", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Convierte el plan de billetes en una descripción para mostrar al usuario.
     *
     * @param billetes cantidad de billetes por denominación
     * @return texto con las denominaciones que se entregarán
     */
    private String describirBilletes(Map<Integer, Integer> billetes) {
        StringBuilder descripcion = new StringBuilder();
        for (int denominacion : DENOMINACIONES) {
            int cantidad = billetes.getOrDefault(denominacion, 0);
            if (cantidad > 0) {
                if (!descripcion.isEmpty()) {
                    descripcion.append(", ");
                }
                descripcion.append(cantidad).append(" x ").append(formatearDinero(denominacion));
            }
        }
        return descripcion.toString();
    }

    /**
     * Aplica el formato de moneda colombiana a un monto entero.
     *
     * @param monto cantidad de dinero a presentar
     * @return monto con símbolo de pesos y separadores de miles
     */
    private String formatearDinero(int monto) {
        return "$ " + formatoNumerico.format(monto);
    }

    /**
     * Restablece el campo de entrada y notifica que la pantalla quedó limpia.
     */
    private void limpiarPantalla() {
        campoMonto.setText("$ 0");
        etiquetaMensaje.setText("Pantalla limpiada.");
        etiquetaMensaje.setForeground(new Color(30, 41, 59));
    }

    /**
     * Vuelve a llenar la tabla usando los datos actuales del modelo del cajero.
     */
    private void actualizarTablaInventario() {
        modeloTabla.setRowCount(0);
        Map<Integer, Integer> inventario = cajero.obtenerInventario();
        for (int denominacion : DENOMINACIONES) {
            int cantidad = inventario.get(denominacion);
            modeloTabla.addRow(new Object[]{
                    formatearDinero(denominacion),
                    cantidad,
                    formatearDinero(denominacion * cantidad)
            });
        }
    }
}
