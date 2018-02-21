/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.edu.mimuw.cloudatlas.interpreter;

import java.util.List;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueNull;

/**
 *
 * @author Máté
 */
public class EnvironmentRow extends Environment{

   private final TableRow row;

	public EnvironmentRow(TableRow row, List<String> columns) {
		super(columns);
		this.row = row;
	}

	@Override
	public Result getIdent(String ident) {
		try {
			Value value = row.getIth(columns.get(ident));
			return new ResultSingle(value);
		} catch (NullPointerException exception) {
			// We will get here when attribute does not exist.
			// According to specification we should sometimes return
			// NULL and sometimes throw error. On the other hand, 
			// test 6.in suggests we should return NULL even when 
			// attribute does not exist in any child zone - as long as reference to
			// this attribute is in WHERE/ORDER clause. We choose
			// semantics that is consistent with tests and return always NULL.
			return new ResultSingle(ValueNull.getInstance());
		}
	}
    
}
