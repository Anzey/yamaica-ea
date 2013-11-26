/* Copyright (C) 2013 BMW Group
 * Author: Manfred Bathelt (manfred.bathelt@bmw.de)
 * Author: Juergen Gehring (juergen.gehring@bmw.de)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package de.bmw.yamaica.ea.core.franca.exceptions;

public class ErrorEnumTypeException extends EA2FrancaTransformationException
{
    private static final long serialVersionUID = -7355963699214226080L;

    public ErrorEnumTypeException()
    {
    }

    public ErrorEnumTypeException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ErrorEnumTypeException(String message)
    {
        super(message);
    }

    public ErrorEnumTypeException(Throwable cause)
    {
        super(cause);
    }
}