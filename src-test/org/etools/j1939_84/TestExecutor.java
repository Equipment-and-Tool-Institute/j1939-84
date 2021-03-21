/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84;

import static org.junit.Assert.assertNotNull;

import java.util.concurrent.Executor;

/**
 * {@link Executor} used for tests to capture the command so it can be run when
 * desired.
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class TestExecutor implements Executor {

    private Runnable command;

    @Override
    public void execute(Runnable command) {
        this.command = command;
    }

    public void run() {
        assertNotNull(command);
        command.run();
    }

}
