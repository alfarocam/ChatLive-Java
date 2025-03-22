/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.chatclienteservidor;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

/**
 *
 * @author Camila Alfaro
 */
public class ClienteChat {

    // direccion del server en este caso localhost ya que es para prueba en esta misma compu
    private static final String SERVIDOR = "localhost";
    private static final int PUERTO = 5000;
    //se envia y recibe mensajes
    private PrintWriter salida;
    private BufferedReader entrada;
    //swing
    private JTextArea areaChat;
    private JTextField campoMensaje;

    //contrctor de la interfaz swing y de  la conexion con el server
    public ClienteChat() {
        crearInterfaz();
        conectarServidor();
    }
//metodo de la interfaz grafica

    private void crearInterfaz() {
        //aqui se establece el tamaño de la ventana al abrirse
        JFrame ventana = new JFrame("Chat Cliente");
        ventana.setSize(400, 500);
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setLocationRelativeTo(null);

        //icon personal, tamaño, dimensiones etc
        ImageIcon logoOriginal = new ImageIcon(getClass().getResource("/recursos/ChatLogo.png"));
        Image img = logoOriginal.getImage();
        Image imgGato = img.getScaledInstance(320, 180, Image.SCALE_SMOOTH);
        ImageIcon logo = new ImageIcon(imgGato);

        //un panel para poner el icon anterior
        JPanel panelImagen = new JPanel();
        panelImagen.setBackground(Color.lightGray);
        JLabel logoLabel = new JLabel(logo);
        panelImagen.add(logoLabel);

        //instrucciones del chat y fuente, tamaño
        JTextArea areaInstrucciones = new JTextArea();
        areaInstrucciones.setText("Bienvenido al Camila Alfaro Chat Live!\n\nInstrucciones:\n1. Ingresa tu nombre de usuario.\n2. Escribe tus mensajes y respeta a los demás.\n3. Para salir, simplemente cierra la ventana.\n\nDisfruta del chat!");
        areaInstrucciones.setEditable(false);
        areaInstrucciones.setBackground(new Color(245, 245, 245));
        areaInstrucciones.setFont(new Font("Arial", Font.BOLD, 14));

        //espacio del area del chat
        areaChat = new JTextArea();
        areaChat.setEditable(false);
        areaChat.setBackground(new Color(240, 240, 240));
        areaChat.setFont(new Font("Arial", Font.PLAIN, 14));

        //los componentes en la ventana
        ventana.setLayout(new BorderLayout());
        ventana.add(panelImagen, BorderLayout.NORTH);  // Logo arriba

        //el panel para instrucciones y chat
        JPanel panelCentral = new JPanel();
        panelCentral.setLayout(new BorderLayout());
        panelCentral.add(new JScrollPane(areaInstrucciones), BorderLayout.NORTH);
        panelCentral.add(new JScrollPane(areaChat), BorderLayout.CENTER);
        ventana.add(panelCentral, BorderLayout.CENTER);

        //el espacio para escribir mensaje en la parte inferior de la ventana
        campoMensaje = new JTextField();
        campoMensaje.setFont(new Font("Arial", Font.PLAIN, 14));
        campoMensaje.addActionListener(e -> enviarMensaje());
        ventana.add(campoMensaje, BorderLayout.SOUTH);

        //en caso de que no cargue la imagen mostrar un print informando esto
        if (logoOriginal.getIconWidth() <= 0) {
            System.out.println("No se logro cargar la imagen!");
            logoLabel.setText("Chat Logo");
            logoLabel.setFont(new Font("Arial", Font.BOLD, 16));
        }

        ventana.setVisible(true);
    }

    //metodo que conecta y establece la comunicacion con el server
    private void conectarServidor() {
        try {
            Socket socket = new Socket(SERVIDOR, PUERTO);
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            salida = new PrintWriter(socket.getOutputStream(), true);

            // thread para recibir mensajes
            new Thread(() -> {
                try {
                    String mensaje;
                    while ((mensaje = entrada.readLine()) != null) {
                        areaChat.append(mensaje + "\n");
                    }
                    //en caso de perder conexion se informa en un print
                } catch (IOException e) {
                    areaChat.append("Conexion terminada\n");
                }
            }).start();

            // se pide nombre de usuario
            String nombreUsuario = JOptionPane.showInputDialog("Ingrese su nombre de usuario:");
            salida.println(nombreUsuario);

        } catch (IOException e) {
            //en caso de que el server este apagado o hay algun error
            JOptionPane.showMessageDialog(null, "No se pudo conectar al servidor", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    //metodo que envia el mensaje al servidor una vez que el usuario lo mande o es decir, que dé enter
    private void enviarMensaje() {
        String mensaje = campoMensaje.getText();
        if (!mensaje.isEmpty()) {
            salida.println(mensaje);
            campoMensaje.setText("");
        }
    }

    //metodo main que inicia el cliente en el thread de eventos de swing
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClienteChat::new);
    }
}
