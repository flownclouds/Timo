package fm.liu.timo.server.session.handler;

import java.util.List;
import fm.liu.timo.net.connection.BackendConnection;

/**
 * do nothing
 * @author liuhuanting
 *
 */
public class VirtualHandler implements ResultHandler {

    @Override
    public void ok(byte[] data, BackendConnection con) {}

    @Override
    public void error(byte[] data, BackendConnection con) {}

    @Override
    public void field(byte[] header, List<byte[]> fields, byte[] eof, BackendConnection con) {}

    @Override
    public void row(byte[] row, BackendConnection con) {}

    @Override
    public void eof(byte[] eof, BackendConnection con) {}

    @Override
    public void close(String reason) {}

}
