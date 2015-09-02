package fm.liu.timo.server.session.handler;

import org.pmw.tinylog.Logger;
import fm.liu.timo.net.connection.BackendConnection;

/**
 * @author liuhuanting
 */
public class RollbackHandler extends OKResultHandler {

    @Override
    protected void success(BackendConnection con) {
        con.release();
    }

    @Override
    protected void failed(String err) {
        Logger.warn("rollback failed due to :{}", err);
    }

}
