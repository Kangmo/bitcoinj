/*
 * Copyright 2012 the original author or authors.
 * Copyright 2014 Kangmo Kim 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.nhnsoft.bitcoin.uri;

/**
 * <p>Exception to provide the following to {@link org.multibit.qrcode.BitcoinURI}:</p>
 * <ul>
 * <li>Provision of parsing error messages</li>
 * </ul>
 * <p>This exception occurs when an optional field is detected (under the Bitcoin URI scheme) and fails
 * to pass the associated test (such as {@code amount} not being a valid number).</p>
 *
 * @since 0.3.0
 *         
 */
public class OptionalFieldValidationException extends BitcoinURIParseException {

    public OptionalFieldValidationException(String s) {
        super(s);
    }

    public OptionalFieldValidationException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
