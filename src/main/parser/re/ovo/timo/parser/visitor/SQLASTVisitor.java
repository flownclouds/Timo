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
 * (created at 2011-5-30)
 */
package re.ovo.timo.parser.visitor;

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

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public interface SQLASTVisitor {

    void visit(BetweenAndExpression node);

    void visit(ComparisionIsExpression node);

    void visit(InExpressionList node);

    void visit(LikeExpression node);

    void visit(CollateExpression node);

    void visit(UserExpression node);

    void visit(UnaryOperatorExpression node);

    void visit(BinaryOperatorExpression node);

    void visit(PolyadicOperatorExpression node);

    void visit(LogicalAndExpression node);

    void visit(LogicalOrExpression node);

    void visit(ComparisionEqualsExpression node);

    void visit(ComparisionNullSafeEqualsExpression node);

    void visit(InExpression node);

    // -------------------------------------------------------
    void visit(FunctionExpression node);

    void visit(Char node);

    void visit(Convert node);

    void visit(Trim node);

    void visit(Cast node);

    void visit(Avg node);

    void visit(Max node);

    void visit(Min node);

    void visit(Sum node);

    void visit(Count node);

    void visit(GroupConcat node);

    void visit(Extract node);

    void visit(Timestampdiff node);

    void visit(Timestampadd node);

    void visit(GetFormat node);

    // -------------------------------------------------------
    void visit(IntervalPrimary node);

    void visit(LiteralBitField node);

    void visit(LiteralBoolean node);

    void visit(LiteralHexadecimal node);

    void visit(LiteralNull node);

    void visit(LiteralNumber node);

    void visit(LiteralString node);

    void visit(CaseWhenOperatorExpression node);

    void visit(DefaultValue node);

    void visit(ExistsPrimary node);

    void visit(PlaceHolder node);

    void visit(Identifier node);

    void visit(MatchExpression node);

    void visit(ParamMarker node);

    void visit(RowExpression node);

    void visit(SysVarPrimary node);

    void visit(UsrDefVarPrimary node);

    // -------------------------------------------------------
    void visit(IndexHint node);

    void visit(InnerJoin node);

    void visit(NaturalJoin node);

    void visit(OuterJoin node);

    void visit(StraightJoin node);

    void visit(SubqueryFactor node);

    void visit(TableReferences node);

    void visit(TableRefFactor node);

    void visit(Dual dual);

    void visit(GroupBy node);

    void visit(Limit node);

    void visit(OrderBy node);

    void visit(ColumnDefinition node);

    void visit(IndexOption node);

    void visit(IndexColumnName node);

    void visit(TableOptions node);

    void visit(AlterSpecification node);

    void visit(DataType node);

    // -------------------------------------------------------
    void visit(ShowAuthors node);

    void visit(ShowBinaryLog node);

    void visit(ShowBinLogEvent node);

    void visit(ShowCharaterSet node);

    void visit(ShowCollation node);

    void visit(ShowColumns node);

    void visit(ShowContributors node);

    void visit(ShowCreate node);

    void visit(ShowDatabases node);

    void visit(ShowEngine node);

    void visit(ShowEngines node);

    void visit(ShowErrors node);

    void visit(ShowEvents node);

    void visit(ShowFunctionCode node);

    void visit(ShowFunctionStatus node);

    void visit(ShowGrants node);

    void visit(ShowIndex node);

    void visit(ShowMasterStatus node);

    void visit(ShowOpenTables node);

    void visit(ShowPlugins node);

    void visit(ShowPrivileges node);

    void visit(ShowProcedureCode node);

    void visit(ShowProcedureStatus node);

    void visit(ShowProcesslist node);

    void visit(ShowProfile node);

    void visit(ShowProfiles node);

    void visit(ShowSlaveHosts node);

    void visit(ShowSlaveStatus node);

    void visit(ShowStatus node);

    void visit(ShowTables node);

    void visit(ShowTableStatus node);

    void visit(ShowTriggers node);

    void visit(ShowVariables node);

    void visit(ShowWarnings node);

    void visit(DescTableStatement node);

    void visit(DALSetStatement node);

    void visit(DALSetNamesStatement node);

    void visit(DALSetCharacterSetStatement node);

    // -------------------------------------------------------
    void visit(DMLCallStatement node);

    void visit(DMLDeleteStatement node);

    void visit(DMLInsertStatement node);

    void visit(DMLReplaceStatement node);

    void visit(DMLSelectStatement node);

    void visit(DMLSelectUnionStatement node);

    void visit(DMLUpdateStatement node);

    void visit(MTSSetTransactionStatement node);

    void visit(MTSSavepointStatement node);

    void visit(MTSReleaseStatement node);

    void visit(MTSRollbackStatement node);

    void visit(DDLTruncateStatement node);

    void visit(DDLAlterTableStatement node);

    void visit(DDLCreateIndexStatement node);

    void visit(DDLCreateTableStatement node);

    void visit(DDLRenameTableStatement node);

    void visit(DDLDropIndexStatement node);

    void visit(DDLDropTableStatement node);

    void visit(ExtDDLCreatePolicy node);

    void visit(ExtDDLDropPolicy node);

}
