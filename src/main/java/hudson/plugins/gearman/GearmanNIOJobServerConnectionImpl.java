package hudson.plugins.gearman;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.channels.SocketChannel;

import org.gearman.common.GearmanNIOJobServerConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GearmanNIOJobServerConnectionImpl extends GearmanNIOJobServerConnection {

    private static final Logger logger = LoggerFactory
            .getLogger(Constants.PLUGIN_LOGGER_NAME);

    public GearmanNIOJobServerConnectionImpl(String hostname, int port)
            throws IllegalArgumentException {
        super(hostname, port);
    }

    @Override
    public void open() throws IOException {
        super.open();

        try {
            // Ugly hack using class reflection, since
            // serverConnection is declared private in parent class
            Field socketField = this.getClass().getSuperclass().getDeclaredField("serverConnection");
            socketField.setAccessible(true);
            SocketChannel serverConnection = (SocketChannel) socketField.get(this);
            serverConnection.socket().setKeepAlive(true);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            logger.warn("Failed to enable keep-alive on connection " + this);
        }
    }
}
