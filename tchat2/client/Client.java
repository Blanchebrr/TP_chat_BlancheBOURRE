package univ.nc.fx.network.tcp.tchat.client;

import univ.nc.fx.network.tcp.tchat.ITchat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

/**
 * Client de tchat
 */
public class Client extends Thread implements ITchat {

	private SocketChannel clientSocketChannel;
	private Selector selector;

	public Client() throws IOException {
		// TODO: Créé le SocketChannel et le Selector
		clientSocketChannel = SocketChannel.open();
		clientSocketChannel.configureBlocking(false);
		selector = Selector.open();
	}

	public void run() {
		// TODO: Écrie la boucle principale du client
		try {
			clientSocketChannel.connect(new InetSocketAddress("localhost", 6699));
			clientSocketChannel.register(selector, SelectionKey.OP_CONNECT);

			while (true) {
				selector.select();

				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> iterator = selectedKeys.iterator();

				while (iterator.hasNext()) {
					SelectionKey key = iterator.next();
					iterator.remove();

					if (key.isConnectable()) {
						// TODO: Termine la connexion
						if (clientSocketChannel.finishConnect()) {
							key.interestOps(SelectionKey.OP_READ);
							clientUI.setConnectedState();
							sendLogToUI("Connecté au serveur.");
						}
					} else if (key.isReadable()) {
						// TODO: Lis les messages du serveur et affichez-les dans l'IHM
						SocketChannel channel = (SocketChannel) key.channel();
						ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
						int bytesRead = channel.read(buffer);
						if (bytesRead == -1) {
							key.cancel();
							channel.close();
						} else {
							String message = new String(buffer.array(), 0, bytesRead);
							clientUI.appendMessage(message);
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendLogToUI(String message) {
		clientUI.setStatus(message);
	}
}
