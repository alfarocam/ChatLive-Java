/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.chatclienteservidor;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

/**
 *
 * @author Camila Alfaro
 */
public class ServidorChat {

    //puerto del server
    private static final int PUERTO = 5000;
    //máximo de clientes permitidos
    private static final int MAX_CLIENTES = 5;
    //array list de los clientes que estan conectdos
    private static final List<PrintWriter> clientes = new ArrayList<>();
    //este hashmap guarda la relacion que tiene entre el cliente y su nombre de usuario, básicamente aquí se identifica los usuario de la conversacion  
    private static final Map<PrintWriter, String> nombresUsuarios = new HashMap<>();

    public static void main(String[] args) {
        //pool de hilos para los clientes concurrentemente y manejo
        ExecutorService ejecutor = Executors.newFixedThreadPool(MAX_CLIENTES);
        //creacion del socket de servidor
        try (ServerSocket servidor = new ServerSocket(PUERTO)) {
            System.out.println("Servidor iniciado en el puerto: " + PUERTO);

            while (true) {
                //if en caso de no alcanzar el numero maximo de clientes
                if (clientes.size() < MAX_CLIENTES) {
                    //acepta la conexion del cliente
                    Socket socketCliente = servidor.accept();
                    System.out.println("Nuevo cliente conectado: " + socketCliente.getInetAddress());
                    //se le asigna un hilo al cliente para manejarlo
                    ejecutor.execute(new ManejadorCliente(socketCliente));
                } else {
                    //si el servidor esta lleno no se da la conexion
                    Socket socketCliente = servidor.accept();
                    PrintWriter salida = new PrintWriter(socketCliente.getOutputStream(), true);
                    salida.println("El servidor está lleno. Intente más tarde.");
                    socketCliente.close();
                }
            }
        } catch (IOException e) {
            //aquí se maneja los errores de conexión
            e.printStackTrace();
        } finally {
            //aqupi se termina el pool de hilos cuando el servidor termina
            ejecutor.shutdown();
        }
    }

    //clase manejador cliente es la que controla la conexion que hay de cada cliente con el servidor, el runnable es para que cada instancia que se cree pueda tener o ejecutar en un hilo independiente
    public static class ManejadorCliente implements Runnable {

        //el socket para la conexion con el cliente
        private Socket socket;
        //el print writer para enviar mensajes al cliente
        private PrintWriter salida;
        //nombre-usuario del cliente
        private String nombreUsuario;
        //array list para guardar el historial de los mensajes del chat
        private List<String> historialMensajes = new ArrayList<>();

        //contrctor del socket del cliente
        public ManejadorCliente(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            //un bufferedreader para leer mensajes y un printewriter para enviar mensajes
            try (
                    BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream())); PrintWriter salida = new PrintWriter(socket.getOutputStream(), true)) {
                //la salida para poder enviar mensajes a este cliente
                this.salida = salida;

                // se pide el nombre de usuario, se guarda y se imprime en el chat
                salida.println("Ingrese su nombre de usuario: ");
                nombreUsuario = entrada.readLine();
                System.out.println(nombreUsuario + " se ha unido al chat :D");
                //se "sincroniza" para agregar el cliente al arraylist de clientes y al hashmap de nombres de usuarios
                synchronized (clientes) {
                    clientes.add(salida);
                    nombresUsuarios.put(salida, nombreUsuario);
                }

                //print de aviso de usuario se ha unido
                enviarATodos("--- " + nombreUsuario + " se ha unido al chat ---");

                String mensaje;
                // bucle while para leer y manejar los mensajes del cliente
                while ((mensaje = entrada.readLine()) != null) {
                    String mensajeConNombre = nombreUsuario + ": " + mensaje;
                    System.out.println(mensajeConNombre);
                    //aqui se guarda el mensaje en el historial de mensajes
                    historialMensajes.add(mensajeConNombre);
                    //se envia el mensaje a los demás clientes conectados
                    enviarATodos(mensajeConNombre);
                }
            } catch (IOException e) {
                //si hay algún error se muestra en mensaje
                System.out.println(nombreUsuario + " se ha desconectado");
            } finally {
                //se actualiza la lista de clientes y se eliminan tanto del hashmap como del arraylist
                synchronized (clientes) {
                    clientes.remove(salida);
                    nombresUsuarios.remove(salida);
                }
                //se llama al metodo que le informa a todos los usuario para decir que ** se ha salido del chat
                enviarATodos("--- " + nombreUsuario + " ha salido del chat ---");

                //se guarda el historial de mensajes en un archivo
                guardarHistorial();

                try {
                    //aqui se cierra el socket o  la conexion con el usuario/cliente
                    socket.close();
                } catch (IOException e) {
                    //y en caso de que pase algun error se imprime
                    e.printStackTrace();
                }
            }
        }

        //***MÉTODOS***
        //metodo que envia los mensajes a los demas usuarios conectados
        private void enviarATodos(String mensaje) {
            synchronized (clientes) {
                for (PrintWriter cliente : clientes) {
                    cliente.println(mensaje);
                }
            }
        }

        //metodo que guarda el historial de mensajes es decir la conversacion en un archivo txt
        private void guardarHistorial() {
            try {
                File carpeta = new File("backups");
                if (!carpeta.exists()) {
                    carpeta.mkdir();
                }
                //se crea un nombre de archivo usando la fecha y hora actual
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                File archivo = new File("backups/Conversacion_" + timestamp + ".txt");
                // se guarda-escribe el historial de mensajes en el archivo
                try (PrintWriter escritor = new PrintWriter(new FileWriter(archivo))) {
                    for (String mensaje : historialMensajes) {
                        escritor.println(mensaje);
                    }
                }
            } catch (IOException e) {
                //en caso de error en este ultimo proceso se informa
                System.out.println("Error al guardar el historial");
            }
        }
    }
}
