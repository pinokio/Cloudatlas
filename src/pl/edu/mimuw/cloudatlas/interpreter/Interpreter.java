/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.edu.mimuw.cloudatlas.interpreter;

/**
 *
 * @author Máté
 */
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import pl.edu.mimuw.cloudatlas.interpreter.query.PrettyPrinter;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.AliasedSelItemC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.AscOrderC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.BasicExpr;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.BasicExprAddC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.BasicExprDivC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.BasicExprModC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.BasicExprMulC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.BasicExprNegC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.BasicExprSubC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.BoolExpr;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.BoolExprBasicExprC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.BoolExprCmpC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.BoolExprRegExpC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.CondExpr;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.CondExprAndC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.CondExprBoolExprC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.CondExprNotC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.CondExprOrC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.DescOrderC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.EBoolC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.ECondExprC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.EDblC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.EFunC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.EIdentC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.EIntC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.EStmtC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.EStrC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.NoNullsC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.NoOrderByC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.NoOrderC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.NoWhereC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.NullFirstsC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.Nulls;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.NullsLastC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.Order;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.OrderBy;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.OrderByC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.OrderItem;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.OrderItemC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.Program;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.ProgramC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.RelOp;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.RelOpEqC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.RelOpGeC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.RelOpGtC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.RelOpLeC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.RelOpLtC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.RelOpNeC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.SelItem;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.SelItemC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.Statement;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.StatementC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.Where;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.WhereC;
import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.TypePrimitive;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueBoolean;
import pl.edu.mimuw.cloudatlas.model.ValueDouble;
import pl.edu.mimuw.cloudatlas.model.ValueInt;
import pl.edu.mimuw.cloudatlas.model.ValueString;
import pl.edu.mimuw.cloudatlas.model.ZMI;

public class Interpreter {
	private static class ValuesPair {
		public final Result left;
		public final Result right;

		public ValuesPair(Result left, Result right) {
			this.left = left;
			this.right = right;
		}
	}
	
	private final ZMI zmi;

	public Interpreter(ZMI zmi) {
		this.zmi = zmi;
	}

	private static Boolean getBoolean(Result result) {
		Value value = result.getValue();
		if(value.getType().isCompatible(TypePrimitive.BOOLEAN)) {
			Boolean b = ((ValueBoolean)value).getValue();
			return b == null? false : b.booleanValue();
		}
		throw new InvalidTypeException(TypePrimitive.BOOLEAN, value.getType());
	}
	
	private static Boolean equal(ValuesPair pair) {
		return getBoolean(pair.left.binaryOperation(BinaryOperation.IS_EQUAL, pair.right));
	}
	
	private static Boolean lower(ValuesPair pair) {
		return getBoolean(pair.left.binaryOperation(BinaryOperation.IS_LOWER_THAN, pair.right));
	}
	
	private static Result not(Result result) {
		return result.unaryOperation(UnaryOperation.NOT);
	}


	public List<QueryResult> interpretProgram(Program program) {
		return program.accept(new ProgramInterpreter(), zmi);
	}

	public List<QueryResult> interpretStatement(Statement statement) {
		return statement.accept(new StatementInterpreter(), zmi);
	}

	public class ProgramInterpreter implements Program.Visitor<List<QueryResult>, ZMI> {
		public List<QueryResult> visit(ProgramC program, ZMI zmi) {
			List<QueryResult> results = new ArrayList<QueryResult>();
			for(Statement s : program.liststatement_) {
				try {
					List<QueryResult> l = s.accept(new StatementInterpreter(), zmi);
					for(QueryResult qr : l)
						if(qr.getName() == null)
							throw new IllegalArgumentException("All items in top-level SELECT must be aliased.");
					results.addAll(l);
				} catch(Exception exception) {
					throw new InsideQueryException(PrettyPrinter.print(s), exception);
				}
			}
			return results;
		}
	}

	public class StatementInterpreter implements Statement.Visitor<List<QueryResult>, ZMI> {
		public List<QueryResult> visit(StatementC statement, ZMI zmi) {
			Table table = new Table(zmi);
			try {
				table = statement.where_.accept(new WhereInterpreter(), table);
			} catch(Exception exception) {
				throw new InsideQueryException(PrettyPrinter.print(statement.where_), exception);
			}
			try {
				table = statement.orderby_.accept(new OrderByInterpreter(), table);
			} catch(Exception exception) {
				throw new InsideQueryException(PrettyPrinter.print(statement.orderby_), exception);
			}
			List<QueryResult> ret = new ArrayList<QueryResult>();

			for(SelItem selItem : statement.listselitem_) {
				try {
					QueryResult qr = selItem.accept(new SelItemInterpreter(), table);
					if(qr.getName() != null) {
						for(QueryResult qrRet : ret)
							if(qr.getName().getName().equals(qrRet.getName().getName()))
								throw new IllegalArgumentException("Alias collision.");
					}
					ret.add(qr);
				} catch(Exception exception) {
					throw new InsideQueryException(PrettyPrinter.print(selItem), exception);
				}
			}

			return ret;
		}
	}

	public class WhereInterpreter implements Where.Visitor<Table, Table> {
		public Table visit(NoWhereC where, Table table) {
			return table;
		}

		public Table visit(WhereC where, Table table) {
			Table result = new Table(table);
			for(TableRow row : table) {
				Environment env = new EnvironmentRow(row, table.getColumns());
				Result condition = where.condexpr_.accept(new CondExprInterpreter(), env);
				if(getBoolean(condition))
					result.appendRow(row);
			}
			return result;
		}
	}

	public class OrderByInterpreter implements OrderBy.Visitor<Table, Table> {
		public Table visit(NoOrderByC orderBy, Table table) {
			return table;
		}

		public Table visit(OrderByC orderBy, Table table) {
			for(OrderItem item : orderBy.listorderitem_) {
				try {
					table = item.accept(new OrderItemInterpreter(), table);
				} catch(Exception exception) {
					throw new InsideQueryException(PrettyPrinter.print(item), exception);
				}
			}
			return table;
		}
	}

	public class OrderItemInterpreter implements OrderItem.Visitor<Table, Table> {
		public Table visit(final OrderItemC orderItem, final Table table) {
			Comparator<TableRow> comparator = new Comparator<TableRow>() {
				@Override
				public int compare(TableRow row1, TableRow row2) {
					Environment env1 = new EnvironmentRow(row1, table.getColumns());
					Result expr1 = orderItem.condexpr_.accept(new CondExprInterpreter(), env1);
					Environment env2 = new EnvironmentRow(row2, table.getColumns());
					Result expr2 = orderItem.condexpr_.accept(new CondExprInterpreter(), env2);
					ValuesPair pair = new ValuesPair(expr1, expr2);
					int result = orderItem.nulls_.accept(new NullsInterpreter(), pair);
					if(result == 0)
						result = orderItem.order_.accept(new OrderInterpreter(), pair);
					return result;
				}
			};
			table.sort(comparator);
			return table;
		}
	}

	public class OrderInterpreter implements Order.Visitor<Integer, ValuesPair> {
		private int compareAsc(ValuesPair pair) {
			if(equal(pair))
				return 0;
			if(lower(pair))
				return -1;
			return 1;
		}

		public Integer visit(AscOrderC order, ValuesPair pair) {
			return compareAsc(pair);
		}

		public Integer visit(DescOrderC order, ValuesPair pair) {
			return -compareAsc(pair);
		}

		public Integer visit(NoOrderC order, ValuesPair pair) {
			return compareAsc(pair);
		}
	}

	public class NullsInterpreter implements Nulls.Visitor<Integer, ValuesPair> {
		private Integer nullsFirst(ValuesPair pair) {
			if(pair.left.getValue().isNull()) {
				if(pair.right.getValue().isNull())
					return 0;
				return -1;
			}
			if(pair.right.getValue().isNull())
				return 1;
			return 0;
		}

		public Integer visit(NoNullsC nulls, ValuesPair pair) {
			return nullsFirst(pair);
		}

		public Integer visit(NullFirstsC nulls, ValuesPair pair) {
			return nullsFirst(pair);
		}

		public Integer visit(NullsLastC nulls, ValuesPair pair) {
			return -nullsFirst(pair);
		}
	}

	public class SelItemInterpreter implements SelItem.Visitor<QueryResult, Table> {
		public QueryResult visit(SelItemC selItem, Table table) {
			Environment env = new EnvironmentTable(table);
			Result result = selItem.condexpr_.accept(new CondExprInterpreter(), env);
			return new QueryResult(result.getValue());
		}

		public QueryResult visit(AliasedSelItemC selItem, Table table) {
			Environment env = new EnvironmentTable(table);
			Result result = selItem.condexpr_.accept(new CondExprInterpreter(), env);
                        if (result instanceof ResultSingle){
                            return new QueryResult(new Attribute(selItem.qident_),result.getValue());
                        }
                        else
                            return new QueryResult(new Attribute(selItem.qident_),result.getValues());                    
            }
	}

	public class BoolExprInterpreter implements BoolExpr.Visitor<Result, Environment> {
		public Result visit(BoolExprCmpC expr, Environment env) {
			try {
				Result left = expr.basicexpr_1.accept(new BasicExprInterpreter(), env);
				Result right = expr.basicexpr_2.accept(new BasicExprInterpreter(), env);
				return expr.relop_.accept(new RelOpInterpreter(), new ValuesPair(left, right));
			} catch(Exception exception) {
				throw new InsideQueryException(PrettyPrinter.print(expr), exception);
			}
		}

		public Result visit(BoolExprRegExpC expr, Environment env) {
			try {
				Result left = expr.basicexpr_.accept(new BasicExprInterpreter(), env);
				return left.unaryOperation(new UnaryOperation.RegexpOperation(expr.string_));
			} catch(Exception exception) {
				throw new InsideQueryException(PrettyPrinter.print(expr), exception);
			}
		}

		public Result visit(BoolExprBasicExprC expr, Environment env) {
			return expr.basicexpr_.accept(new BasicExprInterpreter(), env);
		}
	}

	public class CondExprInterpreter implements CondExpr.Visitor<Result, Environment> {
		public Result visit(CondExprOrC expr, Environment env) {
			try {
				Result left = expr.condexpr_1.accept(new CondExprInterpreter(), env);
				Result right = expr.condexpr_2.accept(new CondExprInterpreter(), env);
				return left.binaryOperation(BinaryOperation.OR, right);
			} catch(Exception exception) {
				throw new InsideQueryException(PrettyPrinter.print(expr), exception);
			}
		}

		public Result visit(CondExprAndC expr, Environment env) {
			try {
				Result left = expr.condexpr_1.accept(new CondExprInterpreter(), env);
				Result right = expr.condexpr_2.accept(new CondExprInterpreter(), env);
				return left.binaryOperation(BinaryOperation.AND, right);
			} catch(Exception exception) {
				throw new InsideQueryException(PrettyPrinter.print(expr), exception);
			}
		}

		public Result visit(CondExprNotC expr, Environment env) {
			try {
				return not(expr.condexpr_.accept(new CondExprInterpreter(), env));
			} catch(Exception exception) {
				throw new InsideQueryException(PrettyPrinter.print(expr), exception);
			}
		}

		public Result visit(CondExprBoolExprC expr, Environment env) {
			return expr.boolexpr_.accept(new BoolExprInterpreter(), env);
		}
	}

	public class BasicExprInterpreter implements BasicExpr.Visitor<Result, Environment> {
		public Result visit(BasicExprAddC expr, Environment env) {
			try {
				Result left = expr.basicexpr_1.accept(new BasicExprInterpreter(), env);
				Result right = expr.basicexpr_2.accept(new BasicExprInterpreter(), env);
				return left.binaryOperation(BinaryOperation.ADD_VALUE, right);
			} catch(Exception exception) {
				throw new InsideQueryException(PrettyPrinter.print(expr), exception);
			}
		}

		public Result visit(BasicExprSubC expr, Environment env) {
			try {
				Result left = expr.basicexpr_1.accept(new BasicExprInterpreter(), env);
				Result right = expr.basicexpr_2.accept(new BasicExprInterpreter(), env);
				return left.binaryOperation(BinaryOperation.SUBTRACT, right);
			} catch(Exception exception) {
				throw new InsideQueryException(PrettyPrinter.print(expr), exception);
			}
		}

		public Result visit(BasicExprMulC expr, Environment env) {
			try {
				Result left = expr.basicexpr_1.accept(new BasicExprInterpreter(), env);
				Result right = expr.basicexpr_2.accept(new BasicExprInterpreter(), env);
				return left.binaryOperation(BinaryOperation.MULTIPLY, right);
			} catch(Exception exception) {
				throw new InsideQueryException(PrettyPrinter.print(expr), exception);
			}
		}

		public Result visit(BasicExprDivC expr, Environment env) {
			try {
				Result left = expr.basicexpr_1.accept(new BasicExprInterpreter(), env);
				Result right = expr.basicexpr_2.accept(new BasicExprInterpreter(), env);
				return left.binaryOperation(BinaryOperation.DIVIDE, right);
			} catch(Exception exception) {
				throw new InsideQueryException(PrettyPrinter.print(expr), exception);
			}
		}

		public Result visit(BasicExprModC expr, Environment env) {
			try {
				Result left = expr.basicexpr_1.accept(new BasicExprInterpreter(), env);
				Result right = expr.basicexpr_2.accept(new BasicExprInterpreter(), env);
				return left.binaryOperation(BinaryOperation.MODULO, right);
			} catch(Exception exception) {
				throw new InsideQueryException(PrettyPrinter.print(expr), exception);
			}
		}

		public Result visit(BasicExprNegC expr, Environment env) {
			try {
				return expr.basicexpr_.accept(new BasicExprInterpreter(), env).unaryOperation(UnaryOperation.NEGATE);
			} catch(Exception exception) {
				throw new InsideQueryException(PrettyPrinter.print(expr), exception);
			}
		}

		public Result visit(EIdentC expr, Environment env) {
			return env.getIdent(expr.qident_);
		}

		public Result visit(EFunC expr, Environment env) {
			try {
				List<Result> arguments = new ArrayList<Result>(expr.listcondexpr_.size());
				for(CondExpr arg : expr.listcondexpr_)
					arguments.add(arg.accept(new CondExprInterpreter(), env));

				return Functions.getInstance().evaluate(expr.qident_, arguments);
			} catch(Exception exception) {
				throw new InsideQueryException(PrettyPrinter.print(expr), exception);
			}
		}

		public ResultSingle visit(EStrC expr, Environment env) {
			return new ResultSingle(new ValueString(expr.string_));
		}

		public ResultSingle visit(EBoolC expr, Environment env) {
			ValueBoolean value;
			if(expr.qbool_.compareTo("true") == 0)
				value = new ValueBoolean(true);
			else if(expr.qbool_.compareTo("false") == 0)
				value = new ValueBoolean(false);
			else
				throw new InternalInterpreterException("Incorrect boolean constant: " + PrettyPrinter.print(expr));
			return new ResultSingle(value);
		}

		public ResultSingle visit(EIntC expr, Environment env) {
			try {
				return new ResultSingle(new ValueInt(Long.parseLong(expr.qinteger_)));
			} catch(NumberFormatException exception) {
				throw new InternalInterpreterException(exception.getMessage());
			}
		}

		public ResultSingle visit(EDblC expr, Environment env) {
			try {
				return new ResultSingle(new ValueDouble(Double.parseDouble(expr.qdouble_)));
			} catch(NumberFormatException exception) {
				throw new InternalInterpreterException(exception.getMessage());
			}
		}

		public Result visit(ECondExprC expr, Environment env) {
			return expr.condexpr_.accept(new CondExprInterpreter(), env);
		}

		public ResultSingle visit(EStmtC expr, Environment env) {
			try {
				List<QueryResult> l = expr.statement_.accept(new StatementInterpreter(), zmi);
				if(l.size() != 1)
					throw new IllegalArgumentException("Nested queries must SELECT exactly one item.");
				return new ResultSingle(l.get(0).getValue());
			} catch(Exception exception) {
				throw new InsideQueryException(PrettyPrinter.print(expr), exception);
			}
		}
	}

	public class RelOpInterpreter implements RelOp.Visitor<Result, ValuesPair> {
		public Result visit(RelOpGtC op, ValuesPair pair) {
			Result greaterEqual = not(pair.left.binaryOperation(BinaryOperation.IS_LOWER_THAN, pair.right));
			Result notEqual = not(pair.left.binaryOperation(BinaryOperation.IS_EQUAL, pair.right));
			return greaterEqual.binaryOperation(BinaryOperation.AND, notEqual);
		}

		public Result visit(RelOpEqC op, ValuesPair pair) {
			return pair.left.binaryOperation(BinaryOperation.IS_EQUAL, pair.right);
		}

		public Result visit(RelOpNeC op, ValuesPair pair) {
			return not(pair.left.binaryOperation(BinaryOperation.IS_EQUAL, pair.right));
		}

		public Result visit(RelOpLtC op, ValuesPair pair) {
			return pair.left.binaryOperation(BinaryOperation.IS_LOWER_THAN, pair.right);
		}

		public Result visit(RelOpLeC op, ValuesPair pair) {
			Result lowerThan = pair.left.binaryOperation(BinaryOperation.IS_LOWER_THAN, pair.right);
			Result equal = pair.left.binaryOperation(BinaryOperation.IS_EQUAL, pair.right);
			return lowerThan.binaryOperation(BinaryOperation.OR, equal);
		}

		public Result visit(RelOpGeC op, ValuesPair pair) {
			return not(pair.left.binaryOperation(BinaryOperation.IS_LOWER_THAN, pair.right));
		}
	}
}
