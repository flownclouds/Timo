package fm.liu.timo.server.session.handler;

import fm.liu.timo.net.connection.BackendConnection;

/**
 * @author liuhuanting
 */
public class SyncHandler extends OKResultHandler {
    private final ResultHandler handler;
    private final String        sql;
    private final Runnable      updater;

    public SyncHandler(ResultHandler handler, String sql, Runnable updater) {
        this.handler = handler;
        this.sql = sql;
        this.updater = updater;
    }

    @Override
    protected void success(BackendConnection con) {
        updater.run();
        con.query(sql, handler);
    }

    @Override
    protected void failed(String err) {
        handler.close(err);
    }

}
