package fm.liu.timo.manager.response;

import java.util.ArrayList;
import java.util.Collection;
import fm.liu.timo.TimoServer;
import fm.liu.timo.manager.handler.ShowHandler;
import fm.liu.timo.manager.response.ResponseUtil.Head;
import fm.liu.timo.net.NIOProcessor;
import fm.liu.timo.net.connection.BackendConnection;
import fm.liu.timo.net.connection.FrontendConnection;
import fm.liu.timo.server.ServerConnection;

/**
 * @author liuhuanting
 */
public class ShowSession extends ShowHandler {
    private static final ArrayList<Head> heads = new ArrayList<Head>();

    static {
        heads.add(new Head("session"));
        heads.add(new Head("backend_count"));
        heads.add(new Head("backend_list"));
    }

    @Override
    public String getInfo() {
        return "show the status of current sessions";
    }

    @Override
    public ArrayList<Head> getHeads() {
        return heads;
    }

    @Override
    public ArrayList<Object[]> getRows() {
        ArrayList<Object[]> rows = new ArrayList<>();
        NIOProcessor[] processors = TimoServer.getInstance().getProcessors();
        for (NIOProcessor processor : processors) {
            Collection<FrontendConnection> frontends = processor.getFrontends().values();
            for (FrontendConnection frontend : frontends) {
                if (!(frontend instanceof ServerConnection)) {
                    continue;
                }
                ServerConnection con = (ServerConnection) frontend;
                Collection<BackendConnection> connections = con.getSession().getConnections();
                Object[] row = new Object[heads.size()];
                StringBuilder builder = new StringBuilder();
                for (BackendConnection connection : connections) {
                    builder.append(connection).append("\r\n");
                }
                int i = 0;
                row[i++] = con.getID();
                row[i++] = connections.size();
                row[i++] = builder.toString();
                rows.add(row);
            }
        }
        return rows;
    }

}
