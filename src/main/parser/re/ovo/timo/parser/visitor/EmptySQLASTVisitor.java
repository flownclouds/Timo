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
package re.ovo.timo.parser.visitor;

import java.util.Collection;

import re.ovo.timo.parser.ast.ASTNode;
import re.ovo.timo.parser.ast.expression.BinaryOperatorExpression;
import re.ovo.timo.parser.ast.expression.PolyadicOperatorExpression;
import re.ovo.timo.parser.ast.expression.UnaryOperatorExpression;
import re.ovo.timo.parser.ast.expression.comparison.BetweenAndExpression;
import re.ovo.timo.parser.ast.expression.comparison.ComparisionEqualsExpression;
import re.ovo.timo.parser.ast.expression.comparison.ComparisionIsExpression;
import re.ovo.timo.parser.ast.expression.comparison.ComparisionNullSafeEqualsExpression;
import re.ovo.timo.parser.ast.expression.comparison.InExpression;
import re.ovo.timo.parser.ast.expression.logical.LogicalAndExpression;
import re.ovo.timo.parser.ast.expression.logical.LogicalOrExpression;
import re.ovo.timo.parser.ast.expression.misc.InExpressionList;
import re.ovo.timo.parser.ast.expression.misc.UserExpression;
import re.ovo.timo.parser.ast.expression.primary.CaseWhenOperatorExpression;
import re.ovo.timo.parser.ast.expression.primary.DefaultValue;
import re.ovo.timo.parser.ast.expression.primary.ExistsPrimary;
import re.ovo.timo.parser.ast.expression.primary.Identifier;
import re.ovo.timo.parser.ast.expression.primary.MatchExpression;
import re.ovo.timo.parser.ast.expression.primary.ParamMarker;
import re.ovo.timo.parser.ast.expression.primary.PlaceHolder;
import re.ovo.timo.parser.ast.expression.primary.RowExpression;
import re.ovo.timo.parser.ast.expression.primary.SysVarPrimary;
import re.ovo.timo.parser.ast.expression.primary.UsrDefVarPrimary;
import re.ovo.timo.parser.ast.expression.primary.function.FunctionExpression;
import re.ovo.timo.parser.ast.expression.primary.function.cast.Cast;
import re.ovo.timo.parser.ast.expression.primary.function.cast.Convert;
import re.ovo.timo.parser.ast.expression.primary.function.datetime.Extract;
import re.ovo.timo.parser.ast.expression.primary.function.datetime.GetFormat;
import re.ovo.timo.parser.ast.expression.primary.function.datetime.Timestampadd;
import re.ovo.timo.parser.ast.expression.primary.function.datetime.Timestampdiff;
import re.ovo.timo.parser.ast.expression.primary.function.groupby.Avg;
import re.ovo.timo.parser.ast.expression.primary.function.groupby.Count;
import re.ovo.timo.parser.ast.expression.primary.function.groupby.GroupConcat;
import re.ovo.timo.parser.ast.expression.primary.function.groupby.Max;
import re.ovo.timo.parser.ast.expression.primary.function.groupby.Min;
import re.ovo.timo.parser.ast.expression.primary.function.groupby.Sum;
import re.ovo.timo.parser.ast.expression.primary.function.string.Char;
import re.ovo.timo.parser.ast.expression.primary.function.string.Trim;
import re.ovo.timo.parser.ast.expression.primary.literal.IntervalPrimary;
import re.ovo.timo.parser.ast.expression.primary.literal.LiteralBitField;
import re.ovo.timo.parser.ast.expression.primary.literal.LiteralBoolean;
import re.ovo.timo.parser.ast.expression.primary.literal.LiteralHexadecimal;
import re.ovo.timo.parser.ast.expression.primary.literal.LiteralNull;
import re.ovo.timo.parser.ast.expression.primary.literal.LiteralNumber;
import re.ovo.timo.parser.ast.expression.primary.literal.LiteralString;
import re.ovo.timo.parser.ast.expression.string.LikeExpression;
import re.ovo.timo.parser.ast.expression.type.CollateExpression;
import re.ovo.timo.parser.ast.fragment.GroupBy;
import re.ovo.timo.parser.ast.fragment.Limit;
import re.ovo.timo.parser.ast.fragment.OrderBy;
import re.ovo.timo.parser.ast.fragment.ddl.ColumnDefinition;
import re.ovo.timo.parser.ast.fragment.ddl.TableOptions;
import re.ovo.timo.parser.ast.fragment.ddl.datatype.DataType;
import re.ovo.timo.parser.ast.fragment.ddl.index.IndexColumnName;
import re.ovo.timo.parser.ast.fragment.ddl.index.IndexOption;
import re.ovo.timo.parser.ast.fragment.tableref.Dual;
import re.ovo.timo.parser.ast.fragment.tableref.IndexHint;
import re.ovo.timo.parser.ast.fragment.tableref.InnerJoin;
import re.ovo.timo.parser.ast.fragment.tableref.NaturalJoin;
import re.ovo.timo.parser.ast.fragment.tableref.OuterJoin;
import re.ovo.timo.parser.ast.fragment.tableref.StraightJoin;
import re.ovo.timo.parser.ast.fragment.tableref.SubqueryFactor;
import re.ovo.timo.parser.ast.fragment.tableref.TableRefFactor;
import re.ovo.timo.parser.ast.fragment.tableref.TableReferences;
import re.ovo.timo.parser.ast.stmt.dal.DALSetCharacterSetStatement;
import re.ovo.timo.parser.ast.stmt.dal.DALSetNamesStatement;
import re.ovo.timo.parser.ast.stmt.dal.DALSetStatement;
import re.ovo.timo.parser.ast.stmt.dal.ShowAuthors;
import re.ovo.timo.parser.ast.stmt.dal.ShowBinLogEvent;
import re.ovo.timo.parser.ast.stmt.dal.ShowBinaryLog;
import re.ovo.timo.parser.ast.stmt.dal.ShowCharaterSet;
import re.ovo.timo.parser.ast.stmt.dal.ShowCollation;
import re.ovo.timo.parser.ast.stmt.dal.ShowColumns;
import re.ovo.timo.parser.ast.stmt.dal.ShowContributors;
import re.ovo.timo.parser.ast.stmt.dal.ShowCreate;
import re.ovo.timo.parser.ast.stmt.dal.ShowDatabases;
import re.ovo.timo.parser.ast.stmt.dal.ShowEngine;
import re.ovo.timo.parser.ast.stmt.dal.ShowEngines;
import re.ovo.timo.parser.ast.stmt.dal.ShowErrors;
import re.ovo.timo.parser.ast.stmt.dal.ShowEvents;
import re.ovo.timo.parser.ast.stmt.dal.ShowFunctionCode;
import re.ovo.timo.parser.ast.stmt.dal.ShowFunctionStatus;
import re.ovo.timo.parser.ast.stmt.dal.ShowGrants;
import re.ovo.timo.parser.ast.stmt.dal.ShowIndex;
import re.ovo.timo.parser.ast.stmt.dal.ShowMasterStatus;
import re.ovo.timo.parser.ast.stmt.dal.ShowOpenTables;
import re.ovo.timo.parser.ast.stmt.dal.ShowPlugins;
import re.ovo.timo.parser.ast.stmt.dal.ShowPrivileges;
import re.ovo.timo.parser.ast.stmt.dal.ShowProcedureCode;
import re.ovo.timo.parser.ast.stmt.dal.ShowProcedureStatus;
import re.ovo.timo.parser.ast.stmt.dal.ShowProcesslist;
import re.ovo.timo.parser.ast.stmt.dal.ShowProfile;
import re.ovo.timo.parser.ast.stmt.dal.ShowProfiles;
import re.ovo.timo.parser.ast.stmt.dal.ShowSlaveHosts;
import re.ovo.timo.parser.ast.stmt.dal.ShowSlaveStatus;
import re.ovo.timo.parser.ast.stmt.dal.ShowStatus;
import re.ovo.timo.parser.ast.stmt.dal.ShowTableStatus;
import re.ovo.timo.parser.ast.stmt.dal.ShowTables;
import re.ovo.timo.parser.ast.stmt.dal.ShowTriggers;
import re.ovo.timo.parser.ast.stmt.dal.ShowVariables;
import re.ovo.timo.parser.ast.stmt.dal.ShowWarnings;
import re.ovo.timo.parser.ast.stmt.ddl.DDLAlterTableStatement;
import re.ovo.timo.parser.ast.stmt.ddl.DDLCreateIndexStatement;
import re.ovo.timo.parser.ast.stmt.ddl.DDLCreateTableStatement;
import re.ovo.timo.parser.ast.stmt.ddl.DDLDropIndexStatement;
import re.ovo.timo.parser.ast.stmt.ddl.DDLDropTableStatement;
import re.ovo.timo.parser.ast.stmt.ddl.DDLRenameTableStatement;
import re.ovo.timo.parser.ast.stmt.ddl.DDLTruncateStatement;
import re.ovo.timo.parser.ast.stmt.ddl.DescTableStatement;
import re.ovo.timo.parser.ast.stmt.ddl.DDLAlterTableStatement.AlterSpecification;
import re.ovo.timo.parser.ast.stmt.dml.DMLCallStatement;
import re.ovo.timo.parser.ast.stmt.dml.DMLDeleteStatement;
import re.ovo.timo.parser.ast.stmt.dml.DMLInsertStatement;
import re.ovo.timo.parser.ast.stmt.dml.DMLReplaceStatement;
import re.ovo.timo.parser.ast.stmt.dml.DMLSelectStatement;
import re.ovo.timo.parser.ast.stmt.dml.DMLSelectUnionStatement;
import re.ovo.timo.parser.ast.stmt.dml.DMLUpdateStatement;
import re.ovo.timo.parser.ast.stmt.extension.ExtDDLCreatePolicy;
import re.ovo.timo.parser.ast.stmt.extension.ExtDDLDropPolicy;
import re.ovo.timo.parser.ast.stmt.mts.MTSReleaseStatement;
import re.ovo.timo.parser.ast.stmt.mts.MTSRollbackStatement;
import re.ovo.timo.parser.ast.stmt.mts.MTSSavepointStatement;
import re.ovo.timo.parser.ast.stmt.mts.MTSSetTransactionStatement;
import re.ovo.timo.parser.util.Pair;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class EmptySQLASTVisitor implements SQLASTVisitor {

    @SuppressWarnings({"rawtypes"})
    private void visitInternal(Object obj) {
        if (obj == null)
            return;
        if (obj instanceof ASTNode) {
            ((ASTNode) obj).accept(this);
        } else if (obj instanceof Collection) {
            for (Object o : (Collection) obj) {
                visitInternal(o);
            }
        } else if (obj instanceof Pair) {
            visitInternal(((Pair) obj).getKey());
            visitInternal(((Pair) obj).getValue());
        }
    }

    @Override
    public void visit(BetweenAndExpression node) {
        visitInternal(node.getFirst());
        visitInternal(node.getSecond());
        visitInternal(node.getThird());
    }

    @Override
    public void visit(ComparisionIsExpression node) {
        visitInternal(node.getOperand());
    }

    @Override
    public void visit(InExpressionList node) {
        visitInternal(node.getList());
    }

    @Override
    public void visit(LikeExpression node) {
        visitInternal(node.getFirst());
        visitInternal(node.getSecond());
        visitInternal(node.getThird());
    }

    @Override
    public void visit(CollateExpression node) {
        visitInternal(node.getString());
    }

    @Override
    public void visit(UserExpression node) {}

    @Override
    public void visit(UnaryOperatorExpression node) {
        visitInternal(node.getOperand());
    }

    @Override
    public void visit(BinaryOperatorExpression node) {
        visitInternal(node.getLeftOprand());
        visitInternal(node.getRightOprand());
    }

    @Override
    public void visit(PolyadicOperatorExpression node) {
        for (int i = 0, len = node.getArity(); i < len; ++i) {
            visitInternal(node.getOperand(i));
        }
    }

    @Override
    public void visit(LogicalAndExpression node) {
        visit((PolyadicOperatorExpression) node);
    }

    @Override
    public void visit(LogicalOrExpression node) {
        visit((PolyadicOperatorExpression) node);
    }

    @Override
    public void visit(ComparisionEqualsExpression node) {
        visit((BinaryOperatorExpression) node);
    }

    @Override
    public void visit(ComparisionNullSafeEqualsExpression node) {
        visit((BinaryOperatorExpression) node);
    }

    @Override
    public void visit(InExpression node) {
        visit((BinaryOperatorExpression) node);
    }

    @Override
    public void visit(FunctionExpression node) {
        visitInternal(node.getArguments());
    }

    @Override
    public void visit(Char node) {
        visit((FunctionExpression) node);
    }

    @Override
    public void visit(Convert node) {
        visit((FunctionExpression) node);
    }

    @Override
    public void visit(Trim node) {
        visit((FunctionExpression) node);
        visitInternal(node.getRemainString());
        visitInternal(node.getString());
    }

    @Override
    public void visit(Cast node) {
        visit((FunctionExpression) node);
        visitInternal(node.getExpr());
        visitInternal(node.getTypeInfo1());
        visitInternal(node.getTypeInfo2());

    }

    @Override
    public void visit(Avg node) {
        visit((FunctionExpression) node);
    }

    @Override
    public void visit(Max node) {
        visit((FunctionExpression) node);
    }

    @Override
    public void visit(Min node) {
        visit((FunctionExpression) node);
    }

    @Override
    public void visit(Sum node) {
        visit((FunctionExpression) node);
    }

    @Override
    public void visit(Count node) {
        visit((FunctionExpression) node);
    }

    @Override
    public void visit(GroupConcat node) {
        visit((FunctionExpression) node);
        visitInternal(node.getAppendedColumnNames());
        visitInternal(node.getOrderBy());
    }

    @Override
    public void visit(Timestampdiff node) {}

    @Override
    public void visit(Timestampadd node) {}

    @Override
    public void visit(Extract node) {}

    @Override
    public void visit(GetFormat node) {}

    @Override
    public void visit(IntervalPrimary node) {
        visitInternal(node.getQuantity());
    }

    @Override
    public void visit(LiteralBitField node) {}

    @Override
    public void visit(LiteralBoolean node) {}

    @Override
    public void visit(LiteralHexadecimal node) {}

    @Override
    public void visit(LiteralNull node) {}

    @Override
    public void visit(LiteralNumber node) {}

    @Override
    public void visit(LiteralString node) {}

    @Override
    public void visit(CaseWhenOperatorExpression node) {
        visitInternal(node.getComparee());
        visitInternal(node.getElseResult());
        visitInternal(node.getWhenList());
    }

    @Override
    public void visit(DefaultValue node) {}

    @Override
    public void visit(ExistsPrimary node) {
        visitInternal(node.getSubquery());
    }

    @Override
    public void visit(PlaceHolder node) {}

    @Override
    public void visit(Identifier node) {}

    @Override
    public void visit(MatchExpression node) {
        visitInternal(node.getColumns());
        visitInternal(node.getPattern());
    }

    @Override
    public void visit(ParamMarker node) {}

    @Override
    public void visit(RowExpression node) {
        visitInternal(node.getRowExprList());
    }

    @Override
    public void visit(SysVarPrimary node) {}

    @Override
    public void visit(UsrDefVarPrimary node) {}

    @Override
    public void visit(IndexHint node) {}

    @Override
    public void visit(InnerJoin node) {
        visitInternal(node.getLeftTableRef());
        visitInternal(node.getOnCond());
        visitInternal(node.getRightTableRef());
    }

    @Override
    public void visit(NaturalJoin node) {
        visitInternal(node.getLeftTableRef());
        visitInternal(node.getRightTableRef());
    }

    @Override
    public void visit(OuterJoin node) {
        visitInternal(node.getLeftTableRef());
        visitInternal(node.getOnCond());
        visitInternal(node.getRightTableRef());
    }

    @Override
    public void visit(StraightJoin node) {
        visitInternal(node.getLeftTableRef());
        visitInternal(node.getOnCond());
        visitInternal(node.getRightTableRef());
    }

    @Override
    public void visit(SubqueryFactor node) {
        visitInternal(node.getSubquery());
    }

    @Override
    public void visit(TableReferences node) {
        visitInternal(node.getTableReferenceList());
    }

    @Override
    public void visit(TableRefFactor node) {
        visitInternal(node.getHintList());
        visitInternal(node.getTable());
    }

    @Override
    public void visit(Dual dual) {}

    @Override
    public void visit(GroupBy node) {
        visitInternal(node.getOrderByList());
    }

    @Override
    public void visit(Limit node) {
        visitInternal(node.getOffset());
        visitInternal(node.getSize());
    }

    @Override
    public void visit(OrderBy node) {
        visitInternal(node.getOrderByList());
    }

    @Override
    public void visit(ColumnDefinition columnDefinition) {}

    @Override
    public void visit(IndexOption indexOption) {}

    @Override
    public void visit(IndexColumnName indexColumnName) {}

    @Override
    public void visit(TableOptions node) {}

    @Override
    public void visit(AlterSpecification node) {}

    @Override
    public void visit(DataType node) {}

    @Override
    public void visit(ShowAuthors node) {}

    @Override
    public void visit(ShowBinaryLog node) {}

    @Override
    public void visit(ShowBinLogEvent node) {
        visitInternal(node.getLimit());
        visitInternal(node.getPos());
    }

    @Override
    public void visit(ShowCharaterSet node) {
        visitInternal(node.getWhere());
    }

    @Override
    public void visit(ShowCollation node) {
        visitInternal(node.getWhere());
    }

    @Override
    public void visit(ShowColumns node) {
        visitInternal(node.getTable());
        visitInternal(node.getWhere());
    }

    @Override
    public void visit(ShowContributors node) {}

    @Override
    public void visit(ShowCreate node) {
        visitInternal(node.getId());
    }

    @Override
    public void visit(ShowDatabases node) {
        visitInternal(node.getWhere());
    }

    @Override
    public void visit(ShowEngine node) {}

    @Override
    public void visit(ShowEngines node) {}

    @Override
    public void visit(ShowErrors node) {
        visitInternal(node.getLimit());
    }

    @Override
    public void visit(ShowEvents node) {
        visitInternal(node.getSchema());
        visitInternal(node.getWhere());
    }

    @Override
    public void visit(ShowFunctionCode node) {
        visitInternal(node.getFunctionName());
    }

    @Override
    public void visit(ShowFunctionStatus node) {
        visitInternal(node.getWhere());
    }

    @Override
    public void visit(ShowGrants node) {
        visitInternal(node.getUser());
    }

    @Override
    public void visit(ShowIndex node) {
        visitInternal(node.getTable());
    }

    @Override
    public void visit(ShowMasterStatus node) {}

    @Override
    public void visit(ShowOpenTables node) {
        visitInternal(node.getSchema());
        visitInternal(node.getWhere());
    }

    @Override
    public void visit(ShowPlugins node) {}

    @Override
    public void visit(ShowPrivileges node) {}

    @Override
    public void visit(ShowProcedureCode node) {
        visitInternal(node.getProcedureName());
    }

    @Override
    public void visit(ShowProcedureStatus node) {
        visitInternal(node.getWhere());
    }

    @Override
    public void visit(ShowProcesslist node) {}

    @Override
    public void visit(ShowProfile node) {
        visitInternal(node.getForQuery());
        visitInternal(node.getLimit());
    }

    @Override
    public void visit(ShowProfiles node) {}

    @Override
    public void visit(ShowSlaveHosts node) {}

    @Override
    public void visit(ShowSlaveStatus node) {}

    @Override
    public void visit(ShowStatus node) {
        visitInternal(node.getWhere());
    }

    @Override
    public void visit(ShowTables node) {
        visitInternal(node.getSchema());
        visitInternal(node.getWhere());
    }

    @Override
    public void visit(ShowTableStatus node) {
        visitInternal(node.getDatabase());
        visitInternal(node.getWhere());
    }

    @Override
    public void visit(ShowTriggers node) {
        visitInternal(node.getSchema());
        visitInternal(node.getWhere());
    }

    @Override
    public void visit(ShowVariables node) {
        visitInternal(node.getWhere());
    }

    @Override
    public void visit(ShowWarnings node) {
        visitInternal(node.getLimit());
    }

    @Override
    public void visit(DescTableStatement node) {
        visitInternal(node.getTable());
    }

    @Override
    public void visit(DALSetStatement node) {
        visitInternal(node.getAssignmentList());
    }

    @Override
    public void visit(DALSetNamesStatement node) {}

    @Override
    public void visit(DALSetCharacterSetStatement node) {}

    @Override
    public void visit(DMLCallStatement node) {
        visitInternal(node.getArguments());
        visitInternal(node.getProcedure());
    }

    @Override
    public void visit(DMLDeleteStatement node) {
        visitInternal(node.getLimit());
        visitInternal(node.getOrderBy());
        visitInternal(node.getTableNames());
        visitInternal(node.getTableRefs());
        visitInternal(node.getWhereCondition());
    }

    @Override
    public void visit(DMLInsertStatement node) {
        visitInternal(node.getColumnNameList());
        visitInternal(node.getDuplicateUpdate());
        visitInternal(node.getRowList());
        visitInternal(node.getSelect());
        visitInternal(node.getTable());
    }

    @Override
    public void visit(DMLReplaceStatement node) {
        visitInternal(node.getColumnNameList());
        visitInternal(node.getRowList());
        visitInternal(node.getSelect());
        visitInternal(node.getTable());
    }

    @Override
    public void visit(DMLSelectStatement node) {
        visitInternal(node.getGroup());
        visitInternal(node.getHaving());
        visitInternal(node.getLimit());
        visitInternal(node.getOrder());
        visitInternal(node.getSelectExprList());
        visitInternal(node.getTables());
        visitInternal(node.getWhere());
    }

    @Override
    public void visit(DMLSelectUnionStatement node) {
        visitInternal(node.getLimit());
        visitInternal(node.getOrderBy());
        visitInternal(node.getSelectStmtList());
    }

    @Override
    public void visit(DMLUpdateStatement node) {
        visitInternal(node.getLimit());
        visitInternal(node.getOrderBy());
        visitInternal(node.getTableRefs());
        visitInternal(node.getValues());
        visitInternal(node.getWhere());
    }

    @Override
    public void visit(MTSSetTransactionStatement node) {}

    @Override
    public void visit(MTSSavepointStatement node) {
        visitInternal(node.getSavepoint());
    }

    @Override
    public void visit(MTSReleaseStatement node) {
        visitInternal(node.getSavepoint());
    }

    @Override
    public void visit(MTSRollbackStatement node) {
        visitInternal(node.getSavepoint());
    }

    @Override
    public void visit(DDLTruncateStatement node) {
        visitInternal(node.getTable());
    }

    @Override
    public void visit(DDLAlterTableStatement node) {
        visitInternal(node.getTable());
    }

    @Override
    public void visit(DDLCreateIndexStatement node) {
        visitInternal(node.getIndexName());
        visitInternal(node.getTable());
    }

    @Override
    public void visit(DDLCreateTableStatement node) {
        visitInternal(node.getTable());
    }

    @Override
    public void visit(DDLRenameTableStatement node) {
        visitInternal(node.getList());
    }

    @Override
    public void visit(DDLDropIndexStatement node) {
        visitInternal(node.getIndexName());
        visitInternal(node.getTable());
    }

    @Override
    public void visit(DDLDropTableStatement node) {
        visitInternal(node.getTableNames());
    }

    @Override
    public void visit(ExtDDLCreatePolicy node) {}

    @Override
    public void visit(ExtDDLDropPolicy node) {}

}
