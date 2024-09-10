/*
 *
 * Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package hudson.plugins.gearman;

import java.util.Date;
import java.util.Set;

import org.gearman.common.GearmanNIOJobServerConnection;
import org.gearman.worker.GearmanFunctionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Base object for gearman worker threads
 *
 *
 * @author Khai Do
 */

public abstract class AbstractWorkerThread implements Runnable {

    private static final Logger logger = LoggerFactory
            .getLogger(Constants.PLUGIN_LOGGER_NAME);

    protected String host;
    protected int port;
    protected String name;
    protected MyGearmanWorkerImpl worker;
    protected GearmanNIOJobServerConnection conn;
    protected AvailabilityMonitor availability;
    private Thread thread;
    private boolean running = false;

    public AbstractWorkerThread(String host, int port, String name,
                                AvailabilityMonitor availability) {
        setHost(host);
        setPort(port);
        setName(name);
        setAvailability(availability);
    }

    protected void initWorker() {
        synchronized(this) {
            if (running) {
                worker = new MyGearmanWorkerImpl(getAvailability());
                // Use custom-built child class to enable keep-alive
                conn = new GearmanNIOJobServerConnectionImpl(host, port);
            }
        }
    }

    // Only for unit tests:
    protected void testInitWorker() {
        running = true;
        initWorker();
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AvailabilityMonitor getAvailability() {
        return availability;
    }

    public void setAvailability(AvailabilityMonitor availability) {
        this.availability = availability;
    }

    /*
     * Register jobs with the gearman worker.
     * This method should be overriden.
     */
    public void registerJobs() {

        logger.debug("---- AbstractorWorker registerJobs function ----");

    }

    public void updateJobs(Set<GearmanFunctionFactory> functions) {
        worker.setFunctions(functions);
    }


    /*
     * Start the thread
     */
    public void start() {
        running = true;
        thread = new Thread(this, "Gearman worker " + name);
        thread.start();
    }

    /*
     * Stop the thread
     */
    public void stop() {

        logger.info("---- " + getName() + " Request to stop AWT: " + this);
        logger.debug("---- " + getName() + "   Thread: " + thread + " name: " + thread.getName());
        logger.debug("---- " + getName() + "   Worker: " + worker);
        synchronized(this) {
            running = false;
            if (worker != null) {
                worker.stop();
            }
        }

        logger.debug("---- " + getName() + "   Interrupting worker");
        // Interrupt the thread so it unblocks any blocking call
        thread.interrupt();

        logger.debug("---- " + getName() + " Stop request done");
    }

    /*
     * Execute the thread (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        initWorker();
        while (running) {
            try {
                logger.info("---- Starting Worker "+ getName() +" ("+new Date().toString()+")");
                synchronized(this) {
                    worker.addServer(conn);
                }
                worker.setWorkerID(name);
                worker.setJobUniqueIdRequired(true);
                registerJobs();
                worker.work();
            } catch (Exception e) {
                logger.error("---- Exception while running worker " + getName(), e);
                if (!running) continue;
                worker.shutdown();
                if (!running) continue;
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e2) {
                    logger.error("---- Exception while waiting to restart worker " + getName(), e2);
                }
                if (!running) continue;
                initWorker();
            }
        }

        // Thread exits
    }

    public boolean isAlive() {
        return thread.isAlive();
    }

}
