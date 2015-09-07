package fm.liu.timo.mysql;

import fm.liu.timo.config.Isolations;
import fm.liu.timo.exception.UnknownTxIsolationException;

/**
 * @author liuhuanting
 */
public class Sync {
    private static final String READ_UNCOMMITTEDStr =
            "SET SESSION TRANSACTION ISOLATION LEVEL READ UNCOMMITTED";
    private static final String READ_COMMITTEDStr   =
            "SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED";
    private static final String REPEATED_READStr    =
            "SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ";
    private static final String SERIALIZABLEStr     =
            "SET SESSION TRANSACTION ISOLATION LEVEL SERIALIZABLE";
    private static final String AUTOCOMMIT_ONStr    = "SET autocommit=1";
    private static final String AUTOCOMMIT_OFFStr   = "SET autocommit=0";

    public static String getTxIsolationCommandStr(int txIsolation) {
        switch (txIsolation) {
            case Isolations.READ_UNCOMMITTED:
                return READ_UNCOMMITTEDStr;
            case Isolations.READ_COMMITTED:
                return READ_COMMITTEDStr;
            case Isolations.REPEATED_READ:
                return REPEATED_READStr;
            case Isolations.SERIALIZABLE:
                return SERIALIZABLEStr;
            default:
                throw new UnknownTxIsolationException("txIsolation:" + txIsolation);
        }
    }

    public static String getCharsetCommandStr(int ci) {
        String charset = CharsetUtil.getCharset(ci);
        StringBuilder s = new StringBuilder();
        s.append("SET names ").append(charset);
        return s.toString();
    }

    public static String getAutoCommitCommandStr(boolean autocommit) {
        if (autocommit)
            return AUTOCOMMIT_ONStr;
        else
            return AUTOCOMMIT_OFFStr;
    }

}
