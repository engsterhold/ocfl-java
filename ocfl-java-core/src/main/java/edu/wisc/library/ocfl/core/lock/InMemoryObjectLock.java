/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 University of Wisconsin Board of Regents
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package edu.wisc.library.ocfl.core.lock;

import edu.wisc.library.ocfl.api.exception.LockException;
import edu.wisc.library.ocfl.api.util.Enforce;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * In-memory implementation of ObjectLock that uses Java's ReentrantReadWriteLock.
 */
public class InMemoryObjectLock implements ObjectLock {

    private Map<String, ReentrantLock> locks;
    private long waitTime;
    private TimeUnit timeUnit;

    /**
     * How long to wait when attempting to acquire a lock.
     *
     * @param waitTime how long to wait for the lock
     * @param timeUnit unit of wait time
     */
    public InMemoryObjectLock(long waitTime, TimeUnit timeUnit) {
        this.locks = new ConcurrentHashMap<>();
        this.waitTime = Enforce.expressionTrue(waitTime >= 0, waitTime, "waitTime must be at least 0");
        this.timeUnit = Enforce.notNull(timeUnit, "timeUnit cannot be null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doInWriteLock(String objectId, Runnable doInLock) {
        doInWriteLock(objectId, () -> {
            doInLock.run();
            return null;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T doInWriteLock(String objectId, Callable<T> doInLock) {
        var lock = locks.computeIfAbsent(objectId, k -> new ReentrantLock());
        return doInLock(objectId, lock, doInLock);
    }

    private <T> T doInLock(String objectId, Lock lock, Callable<T> doInLock) {
        try {
            if (lock.tryLock(waitTime, timeUnit)) {
                try {
                    return doInLock.call();
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    lock.unlock();
                }
            } else {
                throw new LockException("Failed to acquire lock for object " + objectId);
            }
        } catch (InterruptedException e) {
            throw new LockException(e);
        }
    }

}
