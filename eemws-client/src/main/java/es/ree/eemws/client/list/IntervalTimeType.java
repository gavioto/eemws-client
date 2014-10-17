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

package es.ree.eemws.client.list;

/**
 * Possible values for the interval time type.
 *
 * @author Red Eléctrica de España S.A.U.
 * @version 1.0 13/06/2014
 */

public enum IntervalTimeType {

	Application, // Application time interval-
	Server;      // Server timestap time interval.
	
   /** Default interval type. */
    public static final IntervalTimeType DEFAULT_INTERVAL_TYPE = IntervalTimeType.Application;

}
