/*
 * Copyright 2016 Red Eléctrica de España, S.A.U.
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
import java.util.List;
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
 * @version 1.1 27/04/2016
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

            	List<InputConfigurationSet> ics = config.getInputConfigurationSet();
            	for (InputConfigurationSet ic : ics) {
                    scheduler.scheduleAtFixedRate(new InputTask(lh, ic), 0, ic.getSleepTime(), TimeUnit.MILLISECONDS);
               	}
 
            	for (List<OutputConfigurationSet> lst : config.getOutputConfigurationSet()) {
            	    long sleep = Long.MAX_VALUE;
            	    StringBuffer outSetIds = new StringBuffer();
            	    for (OutputConfigurationSet ocs : lst) {
            	        long curr = ocs.getSleepTime();
            	        if (sleep > curr) {
            	            sleep = curr;
            	        }
            	        outSetIds.append(ocs.getIndex());
            	        outSetIds.append("-"); //$NON-NLS-1$
            	    }
            	    
            	    outSetIds.setLength(outSetIds.length() - 1);
            	    
            	    String setIds = outSetIds.toString();
            	    if (setIds.equals("0")) { //$NON-NLS-1$
            	        setIds = ""; //$NON-NLS-1$
            	    } else {
            	        setIds = "-" + setIds; //$NON-NLS-1$
            	    }
            	                	    
            	    LOGGER.info(Messages.getString("MF_CONFIG_DELAY_TIME_O", setIds, sleep));   //$NON-NLS-1$
            	    scheduler.scheduleAtFixedRate(new OutputTask(lh, lst, setIds), 0, sleep, TimeUnit.MILLISECONDS);
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

