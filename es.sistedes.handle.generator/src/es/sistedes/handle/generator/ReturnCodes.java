/*******************************************************************************
* Copyright (c) 2016 Sistedes
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* Abel Gómez - initial API and implementation
*******************************************************************************/
package es.sistedes.handle.generator;

/**
 * Return codes, used to specify abnormal program terminations
 * 
 * @author agomez
 *
 */
public enum ReturnCodes {

	OK(0), ERROR(1);

	private int returnCode;

	private ReturnCodes(int returnCode) {
		this.returnCode = returnCode;
	}

	public int getReturnCode() {
		return returnCode;
	}
}