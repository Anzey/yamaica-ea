/* Copyright (C) 2013-2015 BMW Group
 * Author: Manfred Bathelt (manfred.bathelt@bmw.de)
 * Author: Juergen Gehring (juergen.gehring@bmw.de)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package de.bmw.yamaica.ea.core;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.Assert;
import org.sparx.Repository;

import de.bmw.yamaica.ea.core.containers.EARepositoryContainer;
import de.bmw.yamaica.ea.core.exceptions.EAException;
import de.bmw.yamaica.ea.core.internal.containers.EARepositoryContainerImpl;

public class EAInstance implements Runnable
{
    private static final Logger LOGGER = Logger.getLogger(EAInstance.class.getName());

    private final String          DLL_NOT_FOUND_MESSAGE = "No SSJavaCOM DLL in \"java.library.path\".";
    private final String          EA_NOT_FOUND_MESSAGE  = "No Enterpise Architect installation was found.";

    private Thread                thread                = null;
    private State                 state                 = State.NEW;
    private final Lock            lock                  = new ReentrantLock();
    private final Condition       condition             = lock.newCondition();
    private EARepositoryContainer repository            = null;
    private String                errorMessage          = null;
    private RunData               runData               = null;

    private class RunData
    {
        private IRunnableWithArguments runnable    = null;
        private Object[]               arguments   = null;
        private Object                 returnValue = null;
        private Exception              exception   = null;

        public RunData(IRunnableWithArguments runnable, Object[] arguments)
        {
            Assert.isNotNull(runnable);

            this.runnable = runnable;
            this.arguments = arguments;
        }

        public IRunnableWithArguments getRunnable()
        {
            return runnable;
        }

        public Object[] getArguments()
        {
            return arguments;
        }

        public Object getReturnValue()
        {
            return returnValue;
        }

        public void setReturnValue(Object returnValue)
        {
            this.returnValue = returnValue;
        }

        public Exception getException()
        {
            return exception;
        }

        public void setException(Exception exception)
        {
            this.exception = exception;
        }
    }

    public enum State
    {
        NEW, STARTING, STARTED, TERMINATING, TERMINATED
    }

    public EAInstance()
    {
        thread = new Thread(this, "EA instance thread");
        thread.start();
    }

    public Thread getThread()
    {
        return thread;
    }

    public EARepositoryContainer getRepository()
    {
        return repository;
    }

    @Override
    public void run()
    {
        try
        {
            state = State.STARTING;
            repository = new EARepositoryContainerImpl(this, new Repository());
            state = State.STARTED;

            lock.lock();

            while (null != thread)
            {
                try
                {
                    condition.await();

                    if (null != runData)
                    {
                        runData.setReturnValue(runData.getRunnable().run(runData.getArguments()));
                    }
                }
                catch (InterruptedException e)
                {
                    LOGGER.log(Level.SEVERE, "InterruptedException occured! " + e.getMessage());
                    syncClose();
                    e.printStackTrace();
                }
                catch (Exception e)
                {
                    LOGGER.log(Level.SEVERE, "Exception occured! " + e.getMessage());
                    runData.setException(e);
                }
                finally
                {
                    condition.signal();
                }
            }

            lock.unlock();
        }
        catch (UnsatisfiedLinkError e)
        {
            // SSJavaCOM.dll not available
            e.printStackTrace();

            errorMessage = DLL_NOT_FOUND_MESSAGE;
            LOGGER.log(Level.SEVERE, errorMessage + " " + e.getMessage());
        }
        catch (NoClassDefFoundError e)
        {
            // SSJavaCOM.dll not available
            e.printStackTrace();

            errorMessage = DLL_NOT_FOUND_MESSAGE;
            LOGGER.log(Level.SEVERE, errorMessage + " " + e.getMessage());
        }
        catch (Exception e)
        {
            // EA.exe not available / installed
            e.printStackTrace();

            errorMessage = EA_NOT_FOUND_MESSAGE;
            LOGGER.log(Level.SEVERE, errorMessage + " " + e.getMessage());
        }
        finally
        {
            state = State.TERMINATING;

            if (null != repository)
            {
                ((Repository) repository.getEAObject()).Exit();
                repository = null;
            }

            state = State.TERMINATED;
        }
    }

    public State getState()
    {
        return state;
    }

    public void syncClose()
    {
        thread = null;

        lock.lock();
        condition.signal();
        lock.unlock();
    }

    public void close()
    {
        thread = null;

        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                syncClose();
            }
        }, "EA closing thread");
        thread.start();
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    public Object syncExecution(IRunnableWithArguments runnable, Object... arguments)
    {
        if (null == thread || State.STARTED != state)
        {
            LOGGER.log(Level.SEVERE,String.format("Illegal state: %s", null == thread? "Thread is null!" : "State 'STARTED' expected!") );
            throw  new IllegalStateException();
        }

        // Directly run runnable if our thread calls syncExecution
        if (Thread.currentThread().equals(thread))
        {
            return runnable.run(arguments);
        }

        runData = new RunData(runnable, arguments);

        try
        {
            lock.lock();
            condition.signal();
            condition.await();
        }
        catch (InterruptedException e)
        {
            LOGGER.log(Level.FINE, "InterruptedException occured! " + e.getMessage());
        }
        finally
        {
            lock.unlock();
        }

        final Exception exception = runData.getException();
        final Object returnValue = runData.getReturnValue();

        try
        {
            if (null != exception)
            {
                final EAException eaException = new EAException(exception);
                LOGGER.log(Level.SEVERE, eaException.getMessage());
                throw eaException;
            }
        }
        finally
        {
            runData = null;
        }

        return returnValue;
    }

    public Object syncExecution(IRunnableWithArguments runnable)
    {
        return syncExecution(runnable, (Object[]) null);
    }
}
