package fm.liu.timo.server.session.handler;

import fm.liu.timo.heartbeat.Heartbeat;
import fm.liu.timo.net.connection.BackendConnection;

public class HeartbeatHandler extends OKResultHandler {

    private Heartbeat heartbeat;

    public HeartbeatHandler(Heartbeat heartbeat) {
        this.heartbeat = heartbeat;
    }

    @Override
    protected void success(BackendConnection con) {
        if (!heartbeat.isStoped()) {
            heartbeat.update();
        }
    }

    @Override
    protected void failed(String reason) {}

}
