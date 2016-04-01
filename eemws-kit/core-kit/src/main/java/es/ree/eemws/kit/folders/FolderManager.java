/*
 * Copyright 2014 Red Eléctrica de España, S.A.U.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 *  by the Free Software Foundation, version 3 of the license.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTIBIILTY or FITNESS FOR A PARTICULAR PURPOSE. See GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see
 * http://www.gnu.org/licenses/.
 *
 * Any redistribution and/or modification of this program has to make
 * reference to Red Eléctrica de España, S.A.U. as the copyright owner of
 * the program.
 */

package es.ree.eemws.kit.folders;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import es.ree.eemws.kit.common.Messages;
import es.ree.eemws.core.utils.config.ConfigException;

/**
 * Main class.
 * Creates all the objects and schedules.
 *
 * @author Red Eléctrica de España S.A.U.
 * @version 1.0 09/05/2014
 *
 */
public final class FolderManager {
   
    /** Logging. */
    private static final Logger LOGGER = Logger.getLogger(FolderManager.class.getName());

    
    /**
     * Private constructor.
     * Utility classes should not have a public constructor.
     */
    private FolderManager() {
        
        /* Utility classes should not have a public constructor. */
    }
    
    /**
     * Main method, creates the application objects and schedules.
     * @param args Command line parameters (ignored)
     */
    public static void main(final String[] args) {
        
    	
    	ScheduledExecutorService scheduler = null;
    	try {
            Configuration config = new Configuration();
            config.readConfiguration();
            config.validateConfiguration();
            
            if (ExecutionControl.isRunning(config.getInstanceID())) {
                if (StatusIcon.isInteractive()) {
                    JOptionPane.showMessageDialog(null, Messages.getString("MF_ALREADY_RUNNING"), //$NON-NLS-1$
                            Messages.getString("MF_TITLE_ERROR"), JOptionPane.ERROR_MESSAGE);  //$NON-NLS-1$
                }
                
                LOGGER.info(Messages.getString("MF_ALREADY_RUNNING"));  //$NON-NLS-1$
                StatusIcon.removeIcon();
            } else {

                /* Status images Initialization. */
                StatusIcon.setIdle();

                /* Creates lock handler. */
                LockHandler lh = new LockHandler(config);

            	scheduler = Executors.newScheduledThreadPool(config.getMaxNumThreads());
                
                /* Creates input detector only if an input folder is set. */
            	for (int k = 0; config.getInputFolder(k) != null; k++) {
                	InputTask it = new InputTask(lh, k, config);
                	long sleep = config.getSleepTimeInput(k);
                	scheduler.scheduleAtFixedRate(it, 0, sleep, TimeUnit.MILLISECONDS);
                }

                /* Create output detector only if an output folder is set. */
            	if (config.getOutputFolder(0) != null) {
            	    OutputTask ot = new OutputTask(lh, config);
                	long sleep = config.getSleepTimeOutput();
                	scheduler.scheduleAtFixedRate(ot, 0, sleep, TimeUnit.MILLISECONDS);
                }

                /* Create deletion / backup folder. */
                if (config.getBackupFolder() != null) {
                	DeleteFilesTask dft = new DeleteFilesTask(lh, config);
                	int numDays = config.getNumOfDaysKept();
                	scheduler.scheduleAtFixedRate(dft, numDays, numDays, TimeUnit.DAYS);
                }

                LOGGER.info(Messages.getString("MF_RUNNING")); //$NON-NLS-1$
            
            }
            
        } catch (ConfigException | MalformedURLException ex) {
        	if (scheduler != null) {
        		scheduler.shutdown();
        	}
        	
        	if (StatusIcon.isInteractive()) {
                JOptionPane.showMessageDialog(null, Messages.getString("INVALID_CONFIGURATION", ex.getMessage()), //$NON-NLS-1$
                        Messages.getString("MF_TITLE_ERROR"), JOptionPane.ERROR_MESSAGE);  //$NON-NLS-1$
            }            
        	LOGGER.log(Level.SEVERE, Messages.getString("INVALID_CONFIGURATION"), ex.getMessage()); //$NON-NLS-1$
        	
        	// Force exit
        	StatusIcon.removeIcon();        	
        	System.exit(0); //NOSONAR Invalid configuration: we need to force application to exit.
        	
        } catch (RemoteException ex) {
        	
        	if (StatusIcon.isInteractive()) {
                JOptionPane.showMessageDialog(null, Messages.getString("INVALID_CONFIGURATION", ex.getMessage()), //$NON-NLS-1$   
                        Messages.getString("MF_TITLE_ERROR"), JOptionPane.ERROR_MESSAGE);   //$NON-NLS-1$
            }
        	LOGGER.log(Level.SEVERE, Messages.getString("MF_CANNOT_REACH_REFERENCES"), ex.getMessage()); //$NON-NLS-1$
        	StatusIcon.removeIcon();
        } 
    }
 
}

