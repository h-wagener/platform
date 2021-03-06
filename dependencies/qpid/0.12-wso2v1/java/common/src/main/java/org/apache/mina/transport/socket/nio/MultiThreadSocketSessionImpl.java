/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.apache.mina.transport.socket.nio;

import org.apache.mina.common.IoFilter.WriteRequest;
import org.apache.mina.common.IoFilterChain;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoService;
import org.apache.mina.common.IoServiceConfig;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.IoSessionConfig;
import org.apache.mina.common.RuntimeIOException;
import org.apache.mina.common.TransportType;
import org.apache.mina.common.support.BaseIoSessionConfig;
import org.apache.mina.common.support.IoServiceListenerSupport;
import org.apache.mina.util.Queue;

import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An {@link IoSession} for socket transport (TCP/IP).
 *
 * @author The Apache Directory Project (mina-dev@directory.apache.org)
 * @version $Rev: 619823 $, $Date: 2008-02-08 10:09:37 +0000 (Fri, 08 Feb 2008) $
 */
class MultiThreadSocketSessionImpl extends SocketSessionImpl
{
    private final IoService manager;
    private final IoServiceConfig serviceConfig;
    private final SocketSessionConfig config = new SessionConfigImpl();
    private final MultiThreadSocketIoProcessor ioProcessor;
    private final MultiThreadSocketFilterChain filterChain;
    private final SocketChannel ch;
    private final Queue writeRequestQueue;
    private final IoHandler handler;
    private final SocketAddress remoteAddress;
    private final SocketAddress localAddress;
    private final SocketAddress serviceAddress;
    private final IoServiceListenerSupport serviceListeners;
    private SelectionKey readKey, writeKey;
    private int readBufferSize;
    private CountDownLatch registeredReadyLatch = new CountDownLatch(2);
    private AtomicBoolean created = new AtomicBoolean(false);

    /**
     * Creates a new instance.
     */
    MultiThreadSocketSessionImpl( IoService manager,
                       SocketIoProcessor ioProcessor,
                       IoServiceListenerSupport listeners,
                       IoServiceConfig serviceConfig,
                       SocketChannel ch,
                       IoHandler defaultHandler,
                       SocketAddress serviceAddress )
    {
        super(manager, ioProcessor, listeners, serviceConfig, ch,defaultHandler,serviceAddress);
        this.manager = manager;
        this.serviceListeners = listeners;
        this.ioProcessor = (MultiThreadSocketIoProcessor) ioProcessor;
        this.filterChain = new MultiThreadSocketFilterChain(this);
        this.ch = ch;
        this.writeRequestQueue = new Queue();
        this.handler = defaultHandler;
        this.remoteAddress = ch.socket().getRemoteSocketAddress();
        this.localAddress = ch.socket().getLocalSocketAddress();
        this.serviceAddress = serviceAddress;
        this.serviceConfig = serviceConfig;

        // Apply the initial session settings
        IoSessionConfig sessionConfig = serviceConfig.getSessionConfig();
        if( sessionConfig instanceof SocketSessionConfig )
        {
            SocketSessionConfig cfg = ( SocketSessionConfig ) sessionConfig;
            this.config.setKeepAlive( cfg.isKeepAlive() );
            this.config.setOobInline( cfg.isOobInline() );
            this.config.setReceiveBufferSize( cfg.getReceiveBufferSize() );
            this.readBufferSize = cfg.getReceiveBufferSize();
            this.config.setReuseAddress( cfg.isReuseAddress() );
            this.config.setSendBufferSize( cfg.getSendBufferSize() );
            this.config.setSoLinger( cfg.getSoLinger() );
            this.config.setTcpNoDelay( cfg.isTcpNoDelay() );

            if( this.config.getTrafficClass() != cfg.getTrafficClass() )
            {
                this.config.setTrafficClass( cfg.getTrafficClass() );
            }
        }
    }

    void awaitRegistration() throws InterruptedException
    {
        registeredReadyLatch.countDown();

        registeredReadyLatch.await();
    }

    boolean created() throws InterruptedException
    {
        return created.get();
    }

    void doneCreation()
    {
        created.getAndSet(true);
    }

    public IoService getService()
    {
        return manager;
    }

    public IoServiceConfig getServiceConfig()
    {
        return serviceConfig;
    }

    public IoSessionConfig getConfig()
    {
        return config;
    }

    SocketIoProcessor getIoProcessor()
    {
        return ioProcessor;
    }

    public IoFilterChain getFilterChain()
    {
        return filterChain;
    }

    SocketChannel getChannel()
    {
        return ch;
    }

    IoServiceListenerSupport getServiceListeners()
    {
        return serviceListeners;
    }

    SelectionKey getSelectionKey()
    {
        return readKey;
    }

    SelectionKey getReadSelectionKey()
    {
        return readKey;
    }

    SelectionKey getWriteSelectionKey()
    {
        return writeKey;
    }

    void setSelectionKey(SelectionKey key)
    {
        this.readKey = key;
    }

    void setWriteSelectionKey(SelectionKey key)
    {
        this.writeKey = key;
    }

    public IoHandler getHandler()
    {
        return handler;
    }

    protected void close0()
    {
        filterChain.fireFilterClose( this );
    }

    Queue getWriteRequestQueue()
    {
        return writeRequestQueue;
    }

    /**
     @return int Number of write scheduled write requests
     @deprecated
     */
    public int getScheduledWriteMessages()
    {
        return getScheduledWriteRequests();
    }

    public int getScheduledWriteRequests()
    {
        synchronized( writeRequestQueue )
        {
            return writeRequestQueue.size();
        }
    }

    public int getScheduledWriteBytes()
    {
        synchronized( writeRequestQueue )
        {
            return writeRequestQueue.byteSize();
        }
    }

    protected void write0( WriteRequest writeRequest )
    {
        filterChain.fireFilterWrite( this, writeRequest );
    }

    public TransportType getTransportType()
    {
        return TransportType.SOCKET;
    }

    public SocketAddress getRemoteAddress()
    {
        //This is what I had previously
//        return ch.socket().getRemoteSocketAddress();
        return remoteAddress;
    }

    public SocketAddress getLocalAddress()
    {
        //This is what I had previously
//        return ch.socket().getLocalSocketAddress();
        return localAddress;
    }

    public SocketAddress getServiceAddress()
    {
        return serviceAddress;
    }

    protected void updateTrafficMask()
    {
        this.ioProcessor.updateTrafficMask( this );
    }

    int getReadBufferSize()
    {
        return readBufferSize;
    }

    private class SessionConfigImpl extends BaseIoSessionConfig implements SocketSessionConfig
    {
        public boolean isKeepAlive()
        {
            try
            {
                return ch.socket().getKeepAlive();
            }
            catch( SocketException e )
            {
                throw new RuntimeIOException( e );
            }
        }

        public void setKeepAlive( boolean on )
        {
            try
            {
                ch.socket().setKeepAlive( on );
            }
            catch( SocketException e )
            {
                throw new RuntimeIOException( e );
            }
        }

        public boolean isOobInline()
        {
            try
            {
                return ch.socket().getOOBInline();
            }
            catch( SocketException e )
            {
                throw new RuntimeIOException( e );
            }
        }

        public void setOobInline( boolean on )
        {
            try
            {
                ch.socket().setOOBInline( on );
            }
            catch( SocketException e )
            {
                throw new RuntimeIOException( e );
            }
        }

        public boolean isReuseAddress()
        {
            try
            {
                return ch.socket().getReuseAddress();
            }
            catch( SocketException e )
            {
                throw new RuntimeIOException( e );
            }
        }

        public void setReuseAddress( boolean on )
        {
            try
            {
                ch.socket().setReuseAddress( on );
            }
            catch( SocketException e )
            {
                throw new RuntimeIOException( e );
            }
        }

        public int getSoLinger()
        {
            try
            {
                return ch.socket().getSoLinger();
            }
            catch( SocketException e )
            {
                throw new RuntimeIOException( e );
            }
        }

        public void setSoLinger( int linger )
        {
            try
            {
                if( linger < 0 )
                {
                    ch.socket().setSoLinger( false, 0 );
                }
                else
                {
                    ch.socket().setSoLinger( true, linger );
                }
            }
            catch( SocketException e )
            {
                throw new RuntimeIOException( e );
            }
        }

        public boolean isTcpNoDelay()
        {
            try
            {
                return ch.socket().getTcpNoDelay();
            }
            catch( SocketException e )
            {
                throw new RuntimeIOException( e );
            }
        }

        public void setTcpNoDelay( boolean on )
        {
            try
            {
                ch.socket().setTcpNoDelay( on );
            }
            catch( SocketException e )
            {
                throw new RuntimeIOException( e );
            }
        }

        public int getTrafficClass()
        {
            if( SocketSessionConfigImpl.isGetTrafficClassAvailable() )
            {
                try
                {
                    return ch.socket().getTrafficClass();
                }
                catch( SocketException e )
                {
                    // Throw an exception only when setTrafficClass is also available.
                    if( SocketSessionConfigImpl.isSetTrafficClassAvailable() )
                    {
                        throw new RuntimeIOException( e );
                    }
                }
            }

            return 0;
        }

        public void setTrafficClass( int tc )
        {
            if( SocketSessionConfigImpl.isSetTrafficClassAvailable() )
            {
                try
                {
                    ch.socket().setTrafficClass( tc );
                }
                catch( SocketException e )
                {
                    throw new RuntimeIOException( e );
                }
            }
        }

        public int getSendBufferSize()
        {
            try
            {
                return ch.socket().getSendBufferSize();
            }
            catch( SocketException e )
            {
                throw new RuntimeIOException( e );
            }
        }

        public void setSendBufferSize( int size )
        {
            if( SocketSessionConfigImpl.isSetSendBufferSizeAvailable() )
            {
                try
                {
                    ch.socket().setSendBufferSize( size );
                }
                catch( SocketException e )
                {
                    throw new RuntimeIOException( e );
                }
            }
        }

        public int getReceiveBufferSize()
        {
            try
            {
                return ch.socket().getReceiveBufferSize();
            }
            catch( SocketException e )
            {
                throw new RuntimeIOException( e );
            }
        }

        public void setReceiveBufferSize( int size )
        {
            if( SocketSessionConfigImpl.isSetReceiveBufferSizeAvailable() )
            {
                try
                {
                    ch.socket().setReceiveBufferSize( size );
                    MultiThreadSocketSessionImpl.this.readBufferSize = size;
                }
                catch( SocketException e )
                {
                    throw new RuntimeIOException( e );
                }
            }
        }
    }
}
