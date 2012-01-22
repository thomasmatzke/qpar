/*
Copyright (c) 2011 Thomas Matzke

This file is part of qpar.

qpar is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package qpar.slave;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;

public class BoundedExecutor {  
    private final Executor exec;  
    private final Semaphore semaphore;  
    public BoundedExecutor(Executor exec, int bound) {  
        this.exec = exec;  
        this.semaphore = new Semaphore(bound);  
    }  
    public void submitTask(final Runnable command)  
            throws InterruptedException {  
        semaphore.acquire();  
        try {  
            exec.execute(new Runnable() {  
                public void run() {  
                    try {  
                        command.run();  
                    } finally {  
                        semaphore.release();  
                    }  
                }  
            });  
        } catch (RejectedExecutionException e) {  
            semaphore.release();  
        }  
    }  
}  