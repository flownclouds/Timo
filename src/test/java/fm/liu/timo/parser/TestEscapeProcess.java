package fm.liu.timo.parser;

import org.junit.Test;

public class TestEscapeProcess {

    String sql =
            "insert  into t_uud_user_account(USER_ID,USER_NAME,PASSWORD,CREATE_TIME,STATUS,NICK_NAME,USER_ICON_URL,USER_ICON_URL2,USER_ICON_URL3,ACCOUNT_TYPE) "
                    + "values (2488899998,'u\\'aa\\'\\'a''aa','af8f9dffa5d420fbc249141645b962ee','2013-12-01 00:00:00',0,NULL,NULL,NULL,NULL,1)";

    String sqlret =
            "insert  into t_uud_user_account(USER_ID,USER_NAME,PASSWORD,CREATE_TIME,STATUS,NICK_NAME,USER_ICON_URL,USER_ICON_URL2,USER_ICON_URL3,ACCOUNT_TYPE) "
                    + "values (2488899998,'u''aa''''a''aa','af8f9dffa5d420fbc249141645b962ee','2013-12-01 00:00:00',0,NULL,NULL,NULL,NULL,1)";

    String starWithEscapeSql =
            "\\insert  into t_uud_user_account(USER_ID,USER_NAME,PASSWORD,CREATE_TIME,STATUS,NICK_NAME,USER_ICON_URL,USER_ICON_URL2,USER_ICON_URL3,ACCOUNT_TYPE) "
                    + "values (2488899998,'u\\'aa\\'\\'a''aa','af8f9dffa5d420fbc249141645b962ee','2013-12-01 00:00:00',0,NULL,NULL,NULL,NULL,1)\\";

    String starWithEscapeSqlret =
            "\\insert  into t_uud_user_account(USER_ID,USER_NAME,PASSWORD,CREATE_TIME,STATUS,NICK_NAME,USER_ICON_URL,USER_ICON_URL2,USER_ICON_URL3,ACCOUNT_TYPE) "
                    + "values (2488899998,'u''aa''''a''aa','af8f9dffa5d420fbc249141645b962ee','2013-12-01 00:00:00',0,NULL,NULL,NULL,NULL,1)\\";

    @Test
    public void testEscapeProcess() {
        // String sqlProcessed = DefaultSqlInterceptor.processEscape(sql);
        // assertEquals(sqlProcessed, sqlret);
        // String sqlProcessed1 = DefaultSqlInterceptor
        // .processEscape(starWithEscapeSql);
        // assertEquals(sqlProcessed1, starWithEscapeSqlret);
    }

}
