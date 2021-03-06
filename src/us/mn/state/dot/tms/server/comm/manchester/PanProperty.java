/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  Minnesota Department of Transportation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package us.mn.state.dot.tms.server.comm.manchester;

/**
 * A property to command a camera.
 *
 * @author Douglas Lau
 */
public class PanProperty extends ManchesterProperty {

	/** Pan value (-7 to 7) (8 means turbo) */
	private final int pan;

	/** Create a new pan property */
	public PanProperty(int p) {
		pan = p;
	}

	/** Get a string representation of the property */
	@Override
	public String toString() {
		return "pan: " + pan;
	}

	/** Get command bits */
	@Override
	protected byte commandBits() {
		return isExtended() ? extendedCommandBits()
		                    : basicCommandBits();
	}

	/** Get extended command bits */
	private byte extendedCommandBits() {
		return (pan < 0) ? EX_PAN_LEFT_FULL
		                 : EX_PAN_RIGHT_FULL;
	}

	/** Get basic command bits */
	private byte basicCommandBits() {
		return (byte)(basicCommandFlag() | encodeSpeed(pan));
	}

	/** Get basic command flag */
	private byte basicCommandFlag() {
		return (pan < 0) ? PT_PAN_LEFT
		                 : PT_PAN_RIGHT;
	}

	/** Check if packet is extended function */
	@Override
	protected boolean isExtended() {
		return Math.abs(pan) >= 8;
	}
}
