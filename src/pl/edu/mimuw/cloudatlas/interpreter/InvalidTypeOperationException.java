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
import pl.edu.mimuw.cloudatlas.model.Type;

@SuppressWarnings("serial")
public class InvalidTypeOperationException extends InterpreterException {

	protected InvalidTypeOperationException(String operationName, Type left, Type right) {
		super("Operation '" + operationName + "' is not supported for types " + left.toString() + " and " + right.toString());
	}
	
	protected InvalidTypeOperationException(String operationName, Type type) {
		super("Operation '" + operationName + "' is not supported for type " + type.toString());
	}

}
