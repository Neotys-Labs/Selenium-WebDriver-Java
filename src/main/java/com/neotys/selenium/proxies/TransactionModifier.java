/*
 * Copyright (c) 2016, Neotys
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Neotys nor the names of its contributors may be
 *       used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL NEOTYS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.neotys.selenium.proxies;

/**
 * Created by anouvel on 03/11/2016.
 */
public interface TransactionModifier {
	/**
	 * In Design mode, set the name of the current Transaction in NeoLoad.
	 * In EndUserExperience mode:
	 * <ul>
	 * <li>a timer is started, it will be stopped and sent to NeoLoad at next call of method <code>stopTransaction</code> or <code>startTransaction</code>.
	 * <li>the name of the transaction is added to the path of next entries send to NeoLoad.
	 * </ul>
	 *
	 * @param name the name of the Transaction.
	 * @see TransactionModifier#stopTransaction()
	 */
	void startTransaction(String name);

	/**
	 * In Design mode, no operation is performed.
	 * In EndUserExperience mode:
	 * <ul>
	 * <li>a timer started with <code>startTransaction</code> is stopped and value is sent to NeoLoad.
	 * <li>the name of the transaction is not added to the path of next entries send to NeoLoad.
	 * <ul>
	 *
	 * @see TransactionModifier#startTransaction(String)
	 */
	void stopTransaction();
}
