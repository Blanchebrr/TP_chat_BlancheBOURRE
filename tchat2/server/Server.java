package univ.nc.fx.network.tcp.tchat.server;

import javafx.application.Platform;
import univ.nc.fx.network.tcp.tchat.ITchat;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Processus serveur qui ecoute les connexion entrantes,
 * les messages entrant et les rediffuse au clients connectes
 *
 * @author mathieu.fabre
 */
public class Server extends Thread implements ITchat {

    private ServerSocketChannel serverSocketChannel;
    private Selector selector;

    public Server() throws IOException {
        // TODO: Créez le ServerSocketChannel et le Selector
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress("localhost", 6699));
        selector = Selector.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void run() {
        // TODO: Écrivez la boucle principale du serveur
        try {
            while (true) {
                selector.select();

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if (key.isAcceptable()) {
                        // TODO: Acceptez la nouvelle connexion
                        SocketChannel clientChannel = serverSocketChannel.accept();
                        clientChannel.configureBlocking(false);
                        clientChannel.register(selector, SelectionKey.OP_READ);
                    } else if (key.isReadable()) {
                        // TODO: Lisez et diffusez le message aux clients
                        SocketChannel clientChannel = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                        int bytesRead = clientChannel.read(buffer);
                        if (bytesRead == -1) {
                            // La connexion a été fermée par le client
                            key.cancel();
                            clientChannel.close();
                        } else {
                            // Diffusez le message à tous les clients
                            String message = new String(buffer.array(), 0, bytesRead);
                            broadcastMessage(message);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broadcastMessage(String message) {
        // Parcourir tous les canaux clients enregistrés auprès du sélecteur
        for (SelectionKey key : selector.keys()) {
            if (key.isValid() && key.channel() instanceof SocketChannel) {
                SocketChannel clientChannel = (SocketChannel) key.channel();

                try {
                    // Écrire le message dans le canal du client
                    ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
                    while (buffer.hasRemaining()) {
                        clientChannel.write(buffer);
                    }
                } catch (IOException e) {
                    // En cas d'erreur lors de l'écriture vers un client, vous pouvez gérer
                    // l'exception ici
                    // Par exemple, vous pouvez retirer le canal de la sélection ou fermer le canal.
                    key.cancel();
                    try {
                        clientChannel.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Envoi un message de log a l'IHM
     */
    public void sendLogToUI(String message) {
        Platform.runLater(() -> serverUI.log(message));
    }

}
