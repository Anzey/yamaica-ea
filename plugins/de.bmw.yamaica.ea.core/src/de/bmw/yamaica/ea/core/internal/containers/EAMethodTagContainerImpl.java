/* Copyright (C) 2013-2015 BMW Group
 * Author: Manfred Bathelt (manfred.bathelt@bmw.de)
 * Author: Juergen Gehring (juergen.gehring@bmw.de)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package de.bmw.yamaica.ea.core.internal.containers;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.Assert;
import org.sparx.MethodTag;

import de.bmw.yamaica.ea.core.EAInstance;
import de.bmw.yamaica.ea.core.IRunnableWithArguments;
import de.bmw.yamaica.ea.core.containers.EAMethodContainer;
import de.bmw.yamaica.ea.core.containers.EAMethodTagContainer;
import de.bmw.yamaica.ea.core.exceptions.EAException;
import de.bmw.yamaica.ea.core.exceptions.ReferencedElementNotFoundException;

public class EAMethodTagContainerImpl extends EAContainerImpl implements EAMethodTagContainer
{
    private static final Logger LOGGER = Logger.getLogger(EAMethodTagContainerImpl.class.getName());

    protected final MethodTag eaMethodTag;

    protected EAMethodTagContainerImpl(final EAInstance eaInstance, final MethodTag eaMethodTag)
    {
        super(eaInstance, eaInstance.getRepository().getEAObjectId(eaMethodTag));

        Assert.isNotNull(eaMethodTag);

        this.eaMethodTag = eaMethodTag;
    }

    // BEGIN Implementation of interface EAContainer //

    @Override
    public Object getEAObject()
    {
        return eaMethodTag;
    }

    @Override
    public String getName()
    {
        return (String) getOrCreateCachedValue(CACHED_NAME, new IRunnableWithArguments()
        {
            @Override
            public Object run(final Object... arguments)
            {
                return eaMethodTag.GetName();
            }
        });
    }

    @Override
    public void setName(final String name)
    {
        clearCachedValue(CACHED_NAME, new IRunnableWithArguments()
        {
            @Override
            public Object run(final Object... arguments)
            {
                eaMethodTag.SetName((String) arguments[0]);
                eaMethodTag.Update();

                return null;
            }
        }, name);
    }

    @Override
    public String getNotes()
    {
        return (String) getOrCreateCachedValue(CACHED_NOTES, new IRunnableWithArguments()
        {
            @Override
            public Object run(final Object... arguments)
            {
                return eaMethodTag.GetNotes();
            }
        });
    }

    @Override
    public void setNotes(final String notes)
    {
        clearCachedValue(CACHED_NOTES, new IRunnableWithArguments()
        {
            @Override
            public Object run(final Object... arguments)
            {
                eaMethodTag.SetNotes((String) arguments[0]);
                eaMethodTag.Update();

                return null;
            }
        }, notes);
    }

    @Override
    public boolean update()
    {
        clearCache();

        return (Boolean) eaInstance.syncExecution(new IRunnableWithArguments()
        {
            @Override
            public Object run(final Object... arguments)
            {
                return eaMethodTag.Update();
            }
        });
    }

    @Override
    public void delete()
    {
        final EAMethodContainer method = getMethod();

        if (null != method)
        {
            method.deleteTaggedValue(this);
        }
    }

    // END Implementation of interface EAContainer //

    // BEGIN Implementation of interface EAMethodTagContainer //

    @Override
    public String getValue()
    {
        return (String) getOrCreateCachedValue(CACHED_VALUE, new IRunnableWithArguments()
        {
            @Override
            public Object run(final Object... arguments)
            {
                return eaMethodTag.GetValue();
            }
        });
    }

    @Override
    public void setValue(final String value)
    {
        clearCachedValue(CACHED_VALUE, new IRunnableWithArguments()
        {
            @Override
            public Object run(final Object... arguments)
            {
                eaMethodTag.SetValue((String) arguments[0]);
                eaMethodTag.Update();

                return null;
            }
        }, value);
    }

    @Override
    public EAMethodContainer getMethod()
    {
        final int methodId = (Integer) getOrCreateCachedValue(CACHED_METHOD_ID, new IRunnableWithArguments()
        {
            @Override
            public Object run(final Object... arguments)
            {
                return eaMethodTag.GetMethodID();
            }
        });

        try
        {
            return getRepository().getOrCreateEAObjectContainerById(methodId, EAMethodContainer.class);
        }
        catch (EAException e)
        {
            final ReferencedElementNotFoundException referencedElementNotFoundException = createReferencedElementNotFoundException(e, this);
            LOGGER.log(Level.SEVERE, referencedElementNotFoundException.getMessage());
            throw referencedElementNotFoundException;
        }
    }

    // END Implementation of interface EAMethodTagContainer //
}
