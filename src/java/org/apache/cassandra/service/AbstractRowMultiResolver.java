/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cassandra.service;

import java.nio.ByteBuffer;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.ReadMultiResponse;
import org.apache.cassandra.db.ReadResponse;
import org.apache.cassandra.db.Row;
import org.apache.cassandra.net.MessageIn;
import org.apache.cassandra.utils.concurrent.Accumulator;

public abstract class AbstractRowMultiResolver implements IResponseResolver<ReadMultiResponse, List<Row>>
{
    protected static final Logger logger = LoggerFactory.getLogger(AbstractRowResolver.class);

    protected final String keyspaceName;
    // Accumulator gives us non-blocking thread-safety with optimal algorithmic constraints
    protected final Accumulator<MessageIn<ReadMultiResponse>> replies;
    protected final DecoratedKey key;

    public AbstractRowMultiResolver(ByteBuffer key, String keyspaceName, int maxResponseCount)
    {
        this.key = StorageService.getPartitioner().decorateKey(key);
        this.keyspaceName = keyspaceName;
        this.replies = new Accumulator<>(maxResponseCount);
    }

    public void preprocess(MessageIn<ReadMultiResponse> message)
    {
        replies.add(message);
    }

    public Iterable<MessageIn<ReadMultiResponse>> getMessages()
    {
        return replies;
    }
}
