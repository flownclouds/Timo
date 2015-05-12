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
/**
 * (created at 2011-11-9)
 */
package fm.liu.timo.parser.visitor;

import java.util.Collection;

import fm.liu.timo.parser.ast.ASTNode;
import fm.liu.timo.parser.ast.expression.BinaryOperatorExpression;
import fm.liu.timo.parser.ast.expression.PolyadicOperatorExpression;
import fm.liu.timo.parser.ast.expression.UnaryOperatorExpression;
import fm.liu.timo.parser.ast.expression.comparison.BetweenAndExpression;
import fm.liu.timo.parser.ast.expression.comparison.ComparisionEqualsExpression;
import fm.liu.timo.parser.ast.expression.comparison.ComparisionIsExpression;
import fm.liu.timo.parser.ast.expression.comparison.ComparisionNullSafeEqualsExpression;
import fm.liu.timo.parser.ast.expression.comparison.InExpression;
import fm.liu.timo.parser.ast.expression.logical.LogicalAndExpression;
import fm.liu.timo.parser.ast.expression.logical.LogicalOrExpression;
import fm.liu.timo.parser.ast.expression.misc.InExpressionList;
import fm.liu.timo.parser.ast.expression.misc.UserExpression;
import fm.liu.timo.parser.ast.expression.primary.CaseWhenOperatorExpression;
import fm.liu.timo.parser.ast.expression.primary.DefaultValue;
import fm.liu.timo.parser.ast.expression.primary.ExistsPrimary;
import fm.liu.timo.parser.ast.expression.primary.Identifier;
import fm.liu.timo.parser.ast.expression.primary.MatchExpression;
import fm.liu.timo.parser.ast.expression.primary.ParamMarker;
import fm.liu.timo.parser.ast.expression.primary.PlaceHolder;
import fm.liu.timo.parser.ast.expression.primary.RowExpression;
import fm.liu.timo.parser.ast.expression.primary.SysVarPrimary;
import fm.liu.timo.parser.ast.expression.primary.UsrDefVarPrimary;
import fm.liu.timo.parser.ast.expression.primary.function.FunctionExpression;
import fm.liu.timo.parser.ast.expression.primary.function.cast.Cast;
import fm.liu.timo.parser.ast.expression.primary.function.cast.Convert;
import fm.liu.timo.parser.ast.expression.primary.function.datetime.Extract;
import fm.liu.timo.parser.ast.expression.primary.function.datetime.GetFormat;
import fm.liu.timo.parser.ast.expression.primary.function.datetime.Timestampadd;
import fm.liu.timo.parser.ast.expression.primary.function.datetime.Timestampdiff;
import fm.liu.timo.parser.ast.expression.primary.function.groupby.Avg;
import fm.liu.timo.parser.ast.expression.primary.function.groupby.Count;
import fm.liu.timo.parser.ast.expression.primary.function.groupby.GroupConcat;
import fm.liu.timo.parser.ast.expression.primary.function.groupby.Max;
import fm.liu.timo.parser.ast.expression.primary.function.groupby.Min;
import fm.liu.timo.parser.ast.expression.primary.function.groupby.Sum;
import fm.liu.timo.parser.ast.expression.primary.function.string.Char;
import fm.liu.timo.parser.ast.expression.primary.function.string.Trim;
import fm.liu.timo.parser.ast.expression.primary.literal.IntervalPrimary;
import fm.liu.timo.parser.ast.expression.primary.literal.LiteralBitField;
import fm.liu.timo.parser.ast.expression.primary.literal.LiteralBoolean;
import fm.liu.timo.parser.ast.expression.primary.literal.LiteralHexadecimal;
import fm.liu.timo.parser.ast.expression.primary.literal.LiteralNull;
import fm.liu.timo.parser.ast.expression.primary.literal.LiteralNumber;
import fm.liu.timo.parser.ast.expression.primary.literal.LiteralString;
import fm.liu.timo.parser.ast.expression.string.LikeExpression;
import fm.liu.timo.parser.ast.expression.type.CollateExpression;
import fm.liu.timo.parser.ast.fragment.GroupBy;
import fm.liu.timo.parser.ast.fragment.Limit;
import fm.liu.timo.parser.ast.fragment.OrderBy;
import fm.liu.timo.parser.ast.fragment.ddl.ColumnDefinition;
import fm.liu.timo.parser.ast.fragment.ddl.TableOptions;
import fm.liu.timo.parser.ast.fragment.ddl.datatype.DataType;
import fm.liu.timo.parser.ast.fragment.ddl.index.IndexColumnName;
import fm.liu.timo.parser.ast.fragment.ddl.index.IndexOption;
import fm.liu.timo.parser.ast.fragment.tableref.Dual;
import fm.liu.timo.parser.ast.fragment.tableref.IndexHint;
import fm.liu.timo.parser.ast.fragment.tableref.InnerJoin;
import fm.liu.timo.parser.ast.fragment.tableref.NaturalJoin;
import fm.liu.timo.parser.ast.fragment.tableref.OuterJoin;
import fm.liu.timo.parser.ast.fragment.tableref.StraightJoin;
import fm.liu.timo.parser.ast.fragment.tableref.SubqueryFactor;
import fm.liu.timo.parser.ast.fragment.tableref.TableRefFactor;
import fm.liu.timo.parser.ast.fragment.tableref.TableReferences;
import fm.liu.timo.parser.ast.stmt.dal.DALSetCharacterSetStatement;
import fm.liu.timo.parser.ast.stmt.dal.DALSetNamesStatement;
import fm.liu.timo.parser.ast.stmt.dal.DALSetStatement;
import fm.liu.timo.parser.ast.stmt.dal.ShowAuthors;
import fm.liu.timo.parser.ast.stmt.dal.ShowBinLogEvent;
import fm.liu.timo.parser.ast.stmt.dal.ShowBinaryLog;
import fm.liu.timo.parser.ast.stmt.dal.ShowCharaterSet;
import fm.liu.timo.parser.ast.stmt.dal.ShowCollation;
import fm.liu.timo.parser.ast.stmt.dal.ShowColumns;
import fm.liu.timo.parser.ast.stmt.dal.ShowContributors;
import fm.liu.timo.parser.ast.stmt.dal.ShowCreate;
import fm.liu.timo.parser.ast.stmt.dal.ShowDatabases;
import fm.liu.timo.parser.ast.stmt.dal.ShowEngine;
import fm.liu.timo.parser.ast.stmt.dal.ShowEngines;
import fm.liu.timo.parser.ast.stmt.dal.ShowErrors;
import fm.liu.timo.parser.ast.stmt.dal.ShowEvents;
import fm.liu.timo.parser.ast.stmt.dal.ShowFunctionCode;
import fm.liu.timo.parser.ast.stmt.dal.ShowFunctionStatus;
import fm.liu.timo.parser.ast.stmt.dal.ShowGrants;
import fm.liu.timo.parser.ast.stmt.dal.ShowIndex;
import fm.liu.timo.parser.ast.stmt.dal.ShowMasterStatus;
import fm.liu.timo.parser.ast.stmt.dal.ShowOpenTables;
import fm.liu.timo.parser.ast.stmt.dal.ShowPlugins;
import fm.liu.timo.parser.ast.stmt.dal.ShowPrivileges;
import fm.liu.timo.parser.ast.stmt.dal.ShowProcedureCode;
import fm.liu.timo.parser.ast.stmt.dal.ShowProcedureStatus;
import fm.liu.timo.parser.ast.stmt.dal.ShowProcesslist;
import fm.liu.timo.parser.ast.stmt.dal.ShowProfile;
import fm.liu.timo.parser.ast.stmt.dal.ShowProfiles;
import fm.liu.timo.parser.ast.stmt.dal.ShowSlaveHosts;
import fm.liu.timo.parser.ast.stmt.dal.ShowSlaveStatus;
import fm.liu.timo.parser.ast.stmt.dal.ShowStatus;
import fm.liu.timo.parser.ast.stmt.dal.ShowTableStatus;
import fm.liu.timo.parser.ast.stmt.dal.ShowTables;
import fm.liu.timo.parser.ast.stmt.dal.ShowTriggers;
import fm.liu.timo.parser.ast.stmt.dal.ShowVariables;
import fm.liu.timo.parser.ast.stmt.dal.ShowWarnings;
import fm.liu.timo.parser.ast.stmt.ddl.DDLAlterTableStatement;
import fm.liu.timo.parser.ast.stmt.ddl.DDLCreateIndexStatement;
import fm.liu.timo.parser.ast.stmt.ddl.DDLCreateTableStatement;
import fm.liu.timo.parser.ast.stmt.ddl.DDLDropIndexStatement;
import fm.liu.timo.parser.ast.stmt.ddl.DDLDropTableStatement;
import fm.liu.timo.parser.ast.stmt.ddl.DDLRenameTableStatement;
import fm.liu.timo.parser.ast.stmt.ddl.DDLTruncateStatement;
import fm.liu.timo.parser.ast.stmt.ddl.DescTableStatement;
import fm.liu.timo.parser.ast.stmt.ddl.DDLAlterTableStatement.AlterSpecification;
import fm.liu.timo.parser.ast.stmt.dml.DMLCallStatement;
import fm.liu.timo.parser.ast.stmt.dml.DMLDeleteStatement;
import fm.liu.timo.parser.ast.stmt.dml.DMLInsertStatement;
import fm.liu.timo.parser.ast.stmt.dml.DMLReplaceStatement;
import fm.liu.timo.parser.ast.stmt.dml.DMLSelectStatement;
import fm.liu.timo.parser.ast.stmt.dml.DMLSelectUnionStatement;
import fm.liu.timo.parser.ast.stmt.dml.DMLUpdateStatement;
import fm.liu.timo.parser.ast.stmt.extension.ExtDDLCreatePolicy;
import fm.liu.timo.parser.ast.stmt.extension.ExtDDLDropPolicy;
import fm.liu.timo.parser.ast.stmt.mts.MTSReleaseStatement;
import fm.liu.timo.parser.ast.stmt.mts.MTSRollbackStatement;
import fm.liu.timo.parser.ast.stmt.mts.MTSSavepointStatement;
import fm.liu.timo.parser.ast.stmt.mts.MTSSetTransactionStatement;
import fm.liu.timo.parser.util.Pair;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class Visitor {

    @SuppressWarnings({"rawtypes"})
    protected void visitChild(Object obj) {
        if (obj == null)
            return;
        if (obj instanceof ASTNode) {
            ((ASTNode) obj).accept(this);
        } else if (obj instanceof Collection) {
            for (Object o : (Collection) obj) {
                visitChild(o);
            }
        } else if (obj instanceof Pair) {
            visitChild(((Pair) obj).getKey());
            visitChild(((Pair) obj).getValue());
        }
    }

    public void visit(BetweenAndExpression node) {
        visitChild(node.getFirst());
        visitChild(node.getSecond());
        visitChild(node.getThird());
    }

    public void visit(ComparisionIsExpression node) {
        visitChild(node.getOperand());
    }

    public void visit(InExpressionList node) {
        visitChild(node.getList());
    }

    public void visit(LikeExpression node) {
        visitChild(node.getFirst());
        visitChild(node.getSecond());
        visitChild(node.getThird());
    }

    public void visit(CollateExpression node) {
        visitChild(node.getString());
    }

    public void visit(UserExpression node) {}

    public void visit(UnaryOperatorExpression node) {
        visitChild(node.getOperand());
    }

    public void visit(BinaryOperatorExpression node) {
        visitChild(node.getLeftOprand());
        visitChild(node.getRightOprand());
    }

    public void visit(PolyadicOperatorExpression node) {
        for (int i = 0, len = node.getArity(); i < len; ++i) {
            visitChild(node.getOperand(i));
        }
    }

    public void visit(LogicalAndExpression node) {
        visit((PolyadicOperatorExpression) node);
    }

    public void visit(LogicalOrExpression node) {
        visit((PolyadicOperatorExpression) node);
    }

    public void visit(ComparisionEqualsExpression node) {
        visit((BinaryOperatorExpression) node);
    }

    public void visit(ComparisionNullSafeEqualsExpression node) {
        visit((BinaryOperatorExpression) node);
    }

    public void visit(InExpression node) {
        visit((BinaryOperatorExpression) node);
    }

    public void visit(FunctionExpression node) {
        visitChild(node.getArguments());
    }

    public void visit(Char node) {
        visit((FunctionExpression) node);
    }

    public void visit(Convert node) {
        visit((FunctionExpression) node);
    }

    public void visit(Trim node) {
        visit((FunctionExpression) node);
        visitChild(node.getRemainString());
        visitChild(node.getString());
    }

    public void visit(Cast node) {
        visit((FunctionExpression) node);
        visitChild(node.getExpr());
        visitChild(node.getTypeInfo1());
        visitChild(node.getTypeInfo2());
    }

    public void visit(Avg node) {
        visit((FunctionExpression) node);
    }

    public void visit(Max node) {
        visit((FunctionExpression) node);
    }

    public void visit(Min node) {
        visit((FunctionExpression) node);
    }

    public void visit(Sum node) {
        visit((FunctionExpression) node);
    }

    public void visit(Count node) {
        visit((FunctionExpression) node);
    }

    public void visit(GroupConcat node) {
        visit((FunctionExpression) node);
        visitChild(node.getAppendedColumnNames());
        visitChild(node.getOrderBy());
    }

    public void visit(Timestampdiff node) {}

    public void visit(Timestampadd node) {}

    public void visit(Extract node) {}

    public void visit(GetFormat node) {}

    public void visit(IntervalPrimary node) {
        visitChild(node.getQuantity());
    }

    public void visit(LiteralBitField node) {}

    public void visit(LiteralBoolean node) {}

    public void visit(LiteralHexadecimal node) {}

    public void visit(LiteralNull node) {}

    public void visit(LiteralNumber node) {}

    public void visit(LiteralString node) {}

    public void visit(CaseWhenOperatorExpression node) {
        visitChild(node.getComparee());
        visitChild(node.getElseResult());
        visitChild(node.getWhenList());
    }

    public void visit(DefaultValue node) {}

    public void visit(ExistsPrimary node) {
        visitChild(node.getSubquery());
    }

    public void visit(PlaceHolder node) {}

    public void visit(Identifier node) {}

    public void visit(MatchExpression node) {
        visitChild(node.getColumns());
        visitChild(node.getPattern());
    }

    public void visit(ParamMarker node) {}

    public void visit(RowExpression node) {
        visitChild(node.getRowExprList());
    }

    public void visit(SysVarPrimary node) {}

    public void visit(UsrDefVarPrimary node) {}

    public void visit(IndexHint node) {}

    public void visit(InnerJoin node) {
        visitChild(node.getLeftTableRef());
        visitChild(node.getOnCond());
        visitChild(node.getRightTableRef());
    }

    public void visit(NaturalJoin node) {
        visitChild(node.getLeftTableRef());
        visitChild(node.getRightTableRef());
    }

    public void visit(OuterJoin node) {
        visitChild(node.getLeftTableRef());
        visitChild(node.getOnCond());
        visitChild(node.getRightTableRef());
    }

    public void visit(StraightJoin node) {
        visitChild(node.getLeftTableRef());
        visitChild(node.getOnCond());
        visitChild(node.getRightTableRef());
    }

    public void visit(SubqueryFactor node) {
        visitChild(node.getSubquery());
    }

    public void visit(TableReferences node) {
        visitChild(node.getTableReferenceList());
    }

    public void visit(TableRefFactor node) {
        visitChild(node.getHintList());
        visitChild(node.getTable());
    }

    public void visit(Dual dual) {}

    public void visit(GroupBy node) {
        visitChild(node.getOrderByList());
    }

    public void visit(Limit node) {
        visitChild(node.getOffset());
        visitChild(node.getSize());
    }

    public void visit(OrderBy node) {
        visitChild(node.getOrderByList());
    }

    public void visit(ColumnDefinition columnDefinition) {}

    public void visit(IndexOption indexOption) {}

    public void visit(IndexColumnName indexColumnName) {}

    public void visit(TableOptions node) {}

    public void visit(AlterSpecification node) {}

    public void visit(DataType node) {}

    public void visit(ShowAuthors node) {}

    public void visit(ShowBinaryLog node) {}

    public void visit(ShowBinLogEvent node) {
        visitChild(node.getLimit());
        visitChild(node.getPos());
    }

    public void visit(ShowCharaterSet node) {
        visitChild(node.getWhere());
    }

    public void visit(ShowCollation node) {
        visitChild(node.getWhere());
    }

    public void visit(ShowColumns node) {
        visitChild(node.getTable());
        visitChild(node.getWhere());
    }

    public void visit(ShowContributors node) {}

    public void visit(ShowCreate node) {
        visitChild(node.getId());
    }

    public void visit(ShowDatabases node) {
        visitChild(node.getWhere());
    }

    public void visit(ShowEngine node) {}

    public void visit(ShowEngines node) {}

    public void visit(ShowErrors node) {
        visitChild(node.getLimit());
    }

    public void visit(ShowEvents node) {
        visitChild(node.getSchema());
        visitChild(node.getWhere());
    }

    public void visit(ShowFunctionCode node) {
        visitChild(node.getFunctionName());
    }

    public void visit(ShowFunctionStatus node) {
        visitChild(node.getWhere());
    }

    public void visit(ShowGrants node) {
        visitChild(node.getUser());
    }

    public void visit(ShowIndex node) {
        visitChild(node.getTable());
    }

    public void visit(ShowMasterStatus node) {}

    public void visit(ShowOpenTables node) {
        visitChild(node.getSchema());
        visitChild(node.getWhere());
    }

    public void visit(ShowPlugins node) {}

    public void visit(ShowPrivileges node) {}

    public void visit(ShowProcedureCode node) {
        visitChild(node.getProcedureName());
    }

    public void visit(ShowProcedureStatus node) {
        visitChild(node.getWhere());
    }

    public void visit(ShowProcesslist node) {}

    public void visit(ShowProfile node) {
        visitChild(node.getForQuery());
        visitChild(node.getLimit());
    }

    public void visit(ShowProfiles node) {}

    public void visit(ShowSlaveHosts node) {}

    public void visit(ShowSlaveStatus node) {}

    public void visit(ShowStatus node) {
        visitChild(node.getWhere());
    }

    public void visit(ShowTables node) {
        visitChild(node.getSchema());
        visitChild(node.getWhere());
    }

    public void visit(ShowTableStatus node) {
        visitChild(node.getDatabase());
        visitChild(node.getWhere());
    }

    public void visit(ShowTriggers node) {
        visitChild(node.getSchema());
        visitChild(node.getWhere());
    }

    public void visit(ShowVariables node) {
        visitChild(node.getWhere());
    }

    public void visit(ShowWarnings node) {
        visitChild(node.getLimit());
    }

    public void visit(DescTableStatement node) {
        visitChild(node.getTable());
    }

    public void visit(DALSetStatement node) {
        visitChild(node.getAssignmentList());
    }

    public void visit(DALSetNamesStatement node) {}

    public void visit(DALSetCharacterSetStatement node) {}

    public void visit(DMLCallStatement node) {
        visitChild(node.getArguments());
        visitChild(node.getProcedure());
    }

    public void visit(DMLDeleteStatement node) {
        visitChild(node.getLimit());
        visitChild(node.getOrderBy());
        visitChild(node.getTableNames());
        visitChild(node.getTableRefs());
        visitChild(node.getWhereCondition());
    }

    public void visit(DMLInsertStatement node) {
        visitChild(node.getColumnNameList());
        visitChild(node.getDuplicateUpdate());
        visitChild(node.getRowList());
        visitChild(node.getSelect());
        visitChild(node.getTable());
    }

    public void visit(DMLReplaceStatement node) {
        visitChild(node.getColumnNameList());
        visitChild(node.getRowList());
        visitChild(node.getSelect());
        visitChild(node.getTable());
    }

    public void visit(DMLSelectStatement node) {
        visitChild(node.getGroup());
        visitChild(node.getHaving());
        visitChild(node.getLimit());
        visitChild(node.getOrder());
        visitChild(node.getSelectExprList());
        visitChild(node.getTables());
        visitChild(node.getWhere());
    }

    public void visit(DMLSelectUnionStatement node) {
        visitChild(node.getLimit());
        visitChild(node.getOrderBy());
        visitChild(node.getSelectStmtList());
    }

    public void visit(DMLUpdateStatement node) {
        visitChild(node.getLimit());
        visitChild(node.getOrderBy());
        visitChild(node.getTableRefs());
        visitChild(node.getValues());
        visitChild(node.getWhere());
    }

    public void visit(MTSSetTransactionStatement node) {}

    public void visit(MTSSavepointStatement node) {
        visitChild(node.getSavepoint());
    }

    public void visit(MTSReleaseStatement node) {
        visitChild(node.getSavepoint());
    }

    public void visit(MTSRollbackStatement node) {
        visitChild(node.getSavepoint());
    }

    public void visit(DDLTruncateStatement node) {
        visitChild(node.getTable());
    }

    public void visit(DDLAlterTableStatement node) {
        visitChild(node.getTable());
    }

    public void visit(DDLCreateIndexStatement node) {
        visitChild(node.getIndexName());
        visitChild(node.getTable());
    }

    public void visit(DDLCreateTableStatement node) {
        visitChild(node.getTable());
    }

    public void visit(DDLRenameTableStatement node) {
        visitChild(node.getList());
    }

    public void visit(DDLDropIndexStatement node) {
        visitChild(node.getIndexName());
        visitChild(node.getTable());
    }

    public void visit(DDLDropTableStatement node) {
        visitChild(node.getTableNames());
    }

    public void visit(ExtDDLCreatePolicy node) {}

    public void visit(ExtDDLDropPolicy node) {}

}
