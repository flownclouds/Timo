package fm.liu.timo.mysql;

import fm.liu.timo.config.Isolations;
import fm.liu.timo.exception.UnknownTxIsolationException;
import fm.liu.timo.mysql.connection.MySQLConnection;
import fm.liu.timo.mysql.packet.CommandPacket;
import fm.liu.timo.net.connection.Variables;

/**
 * @author liuhuanting
 */
public class Sync {

    private static final CommandPacket _READ_UNCOMMITTED    =
            new CommandPacket(CommandPacket.COM_QUERY);
    private static final CommandPacket _READ_COMMITTED      =
            new CommandPacket(CommandPacket.COM_QUERY);
    private static final CommandPacket _REPEATED_READ       =
            new CommandPacket(CommandPacket.COM_QUERY);
    private static final CommandPacket _SERIALIZABLE        =
            new CommandPacket(CommandPacket.COM_QUERY);
    private static final CommandPacket _AUTOCOMMIT_ON       =
            new CommandPacket(CommandPacket.COM_QUERY);
    private static final CommandPacket _AUTOCOMMIT_OFF      =
            new CommandPacket(CommandPacket.COM_QUERY);
    private static final CommandPacket _COMMIT              =
            new CommandPacket(CommandPacket.COM_QUERY);
    private static final CommandPacket _ROLLBACK            =
            new CommandPacket(CommandPacket.COM_QUERY);
    private static final String        _READ_UNCOMMITTEDStr =
            "SET SESSION TRANSACTION ISOLATION LEVEL READ UNCOMMITTED";
    private static final String        _READ_COMMITTEDStr   =
            "SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED";
    private static final String        _REPEATED_READStr    =
            "SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ";
    private static final String        _SERIALIZABLEStr     =
            "SET SESSION TRANSACTION ISOLATION LEVEL SERIALIZABLE";
    private static final String        _AUTOCOMMIT_ONStr    = "SET autocommit=1";
    private static final String        _AUTOCOMMIT_OFFStr   = "SET autocommit=0";

    static {
        _READ_UNCOMMITTED.arg =
                "SET SESSION TRANSACTION ISOLATION LEVEL READ UNCOMMITTED".getBytes();
        _READ_COMMITTED.arg = "SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED".getBytes();
        _REPEATED_READ.arg = "SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ".getBytes();
        _SERIALIZABLE.arg = "SET SESSION TRANSACTION ISOLATION LEVEL SERIALIZABLE".getBytes();
        _AUTOCOMMIT_ON.arg = "SET autocommit=1".getBytes();
        _AUTOCOMMIT_OFF.arg = "SET autocommit=0".getBytes();

        _COMMIT.arg = "commit".getBytes();
        _ROLLBACK.arg = "rollback".getBytes();
    }

    private final MySQLConnection conn;
    private CommandPacket         charCmd;
    private CommandPacket         isoCmd;
    private CommandPacket         acCmd;
    private int                   charIndex;   // new
    private int                   txIsolation; // new
    private boolean               autocommit;  // new
    private volatile boolean      executed;

    public Sync(MySQLConnection conn, String sql, Variables requiredConnVars) {
        this.conn = conn;
        if (requiredConnVars == null)
            return;
        // 先赋值，防止frontendVariables有改动
        charIndex = requiredConnVars.getCharsetIndex();
        txIsolation = requiredConnVars.getIsolationLevel();
        autocommit = requiredConnVars.isAutocommit();
        this.charCmd = conn.getVariables().getCharsetIndex() != charIndex
                ? getCharsetCommand(charIndex) : null;
        this.isoCmd = conn.getVariables().getIsolationLevel() != txIsolation
                ? getTxIsolationCommand(txIsolation) : null;
        this.acCmd = conn.getVariables().isAutocommit() != autocommit
                ? getAutoCommitCommand(autocommit) : null;
    }

    private Runnable updater;

    public boolean isExecuted() {
        return executed;
    }

    public boolean isSync() {
        return charCmd == null && isoCmd == null && acCmd == null;
    }

    public void update() {
        if (updater != null) {
            updater.run();
            updater = null;
        }
    }

    public void sync() {
        if (charCmd != null) {

            updater = new Runnable() {
                @Override
                public void run() {
                    conn.getVariables().setCharsetIndex(charIndex);
                }
            };
            charCmd.write(conn);
            charCmd = null;
            return;
        }
        if (isoCmd != null) {
            updater = new Runnable() {
                @Override
                public void run() {
                    conn.getVariables().setIsolationLevel(txIsolation);
                }
            };
            isoCmd.write(conn);
            isoCmd = null;
            return;
        }
        if (acCmd != null) {
            updater = new Runnable() {
                @Override
                public void run() {
                    conn.getVariables().setAutocommit(autocommit);
                }
            };
            acCmd.write(conn);
            acCmd = null;
            return;
        }
    }

    @Override
    public String toString() {
        return "StatusSync [charCmd=" + charCmd + ", isoCmd=" + isoCmd + ", acCmd=" + acCmd
                + ", executed=" + executed + "]";
    }

    private static CommandPacket getTxIsolationCommand(int txIsolation) {
        switch (txIsolation) {
            case Isolations.READ_UNCOMMITTED:
                return _READ_UNCOMMITTED;
            case Isolations.READ_COMMITTED:
                return _READ_COMMITTED;
            case Isolations.REPEATED_READ:
                return _REPEATED_READ;
            case Isolations.SERIALIZABLE:
                return _SERIALIZABLE;
            default:
                throw new UnknownTxIsolationException("txIsolation:" + txIsolation);
        }
    }

    public static String getTxIsolationCommandStr(int txIsolation) {
        switch (txIsolation) {
            case Isolations.READ_UNCOMMITTED:
                return _READ_UNCOMMITTEDStr;
            case Isolations.READ_COMMITTED:
                return _READ_COMMITTEDStr;
            case Isolations.REPEATED_READ:
                return _REPEATED_READStr;
            case Isolations.SERIALIZABLE:
                return _SERIALIZABLEStr;
            default:
                throw new UnknownTxIsolationException("txIsolation:" + txIsolation);
        }
    }

    private static CommandPacket getCharsetCommand(int ci) {
        String charset = CharsetUtil.getCharset(ci);
        StringBuilder s = new StringBuilder();
        s.append("SET names ").append(charset);
        CommandPacket cmd = new CommandPacket(CommandPacket.COM_QUERY);
        cmd.arg = s.toString().getBytes();
        return cmd;
    }

    public static String getCharsetCommandStr(int ci) {
        String charset = CharsetUtil.getCharset(ci);
        StringBuilder s = new StringBuilder();
        s.append("SET names ").append(charset);
        // CommandPacket cmd = new CommandPacket(CommandPacket.COM_QUERY);
        // cmd.arg = s.toString().getBytes();
        return s.toString();
    }

    public static CommandPacket getAutoCommitCommand(boolean autocommit) {
        if (autocommit)
            return _AUTOCOMMIT_ON;
        else
            return _AUTOCOMMIT_OFF;
    }

    public static String getAutoCommitCommandStr(boolean autocommit) {
        if (autocommit)
            return _AUTOCOMMIT_ONStr;
        else
            return _AUTOCOMMIT_OFFStr;
    }

}
