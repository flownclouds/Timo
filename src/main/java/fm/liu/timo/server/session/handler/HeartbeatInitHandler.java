package fm.liu.timo.server.session.handler;

import org.pmw.tinylog.Logger;
import fm.liu.timo.heartbeat.Heartbeat;
import fm.liu.timo.heartbeat.Heartbeat.HeartbeatStatus;
import fm.liu.timo.net.connection.BackendConnection;

public class HeartbeatInitHandler extends OKResultHandler {
    private static final String INIT   =
            "CREATE TABLE IF NOT EXISTS timo_heartbeat(id TINYINT UNSIGNED NOT NULL,last_heartbeat_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,master_record BIGINT,PRIMARY KEY(id))";
    private HeartbeatInitStatus status = HeartbeatInitStatus.UNINITED;
    private Heartbeat           heartbeat;

    enum HeartbeatInitStatus {
        UNINITED, INITED, FINISHED
    }

    public HeartbeatInitHandler(Heartbeat heartbeat) {
        this.heartbeat = heartbeat;
    }

    @Override
    protected void success(BackendConnection con) {
        switch (status) {
            case UNINITED:
                status = HeartbeatInitStatus.INITED;
                con.query(INIT, this);
                break;
            case INITED:
                status = HeartbeatInitStatus.FINISHED;
                heartbeat.updateStatus(HeartbeatStatus.IDLE);
                heartbeat.updateConnection(con);
                break;
            default:
                break;
        }
    }

    @Override
    protected void failed(String reason) {
        Logger.warn("datasource {} init heartbeat failed due to {}.",
                heartbeat.getSource().getConfig().getID(), reason);
    }

}
