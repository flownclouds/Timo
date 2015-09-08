/*
 * Copyright 1999-2012 Alibaba Group.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package fm.liu.timo.mysql;

/**
 * @author xianmao.hexm 2012-8-28
 */
public class PreparedStatement {

    private long     id;
    private int      columnsNumber;
    private int      parametersNumber;
    private int[]    parametersType;
    private String[] statements;
    private boolean  endsWithQuestionMark;

    public PreparedStatement(long id, String statement) {
        this.id = id;
        if (statement.endsWith("?")) {
            endsWithQuestionMark = true;
        }
        this.statements = statement.split("\\?");
        this.parametersNumber = endsWithQuestionMark ? statements.length : statements.length + 1;
        if (!statement.contains("?")) {
            this.parametersNumber = 0;
        }
        this.parametersType = new int[parametersNumber];
    }

    public long getId() {
        return id;
    }

    public int getColumnsNumber() {
        return columnsNumber;
    }

    public int getParametersNumber() {
        return parametersNumber;
    }

    public int[] getParametersType() {
        return parametersType;
    }

    public String[] getStatements() {
        return statements;
    }

    public boolean isEndsWithQuestionMark() {
        return endsWithQuestionMark;
    }

}
