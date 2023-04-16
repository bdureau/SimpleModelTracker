package com.bdureau.simplemodeltracker.nmea;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;

public class Parser {
	public Location l = new Location("RocketTrack");

	public Location parse( String sentence ) {
		if ( sentence.startsWith("$GPGGA") ) {
			if(validateChecksum(sentence)) {
				Log.d("parser", "good sentence");
				String[] p = sentence.split(",");

				// Example:
				String timeString = p[1];
				String latString = p[2];
				String latHemString = p[3];
				String lonString = p[4];
				String lonHemString = p[5];
				String fixString = p[6];
				String satString = p[7];
				String hdopString = p[8];
				String altString = p[9];
				String altUnitString = p[10];
				String geoidHeightString = p[11];
				String geoidHeightUnitString = p[12];
				String checksum = p[14];


				if (validateChecksum(sentence))
					Log.d("parser", "good sentence");
				else
					Log.d("parser", "bad sentence");
				Double altitude = toDoubleMeters(altString, altUnitString);
				if (altitude != null) {
					l.setAltitude(altitude);
				}

				Double latitude = toDecimalDegrees(latString, latHemString);
				if (latitude != null) {
					l.setLatitude(latitude);
				}

				Double longitude = toDecimalDegrees(lonString, lonHemString);
				if (longitude != null) {
					l.setLongitude(longitude);
				}

				// Set accuracy to 30m because it's a number.
				l.setAccuracy(30f);
				try {
					Float accuracy = Float.parseFloat(hdopString);
					l.setAccuracy(accuracy * 10f);
					// Read: http://www.edu-observatory.org/gps/gps_accuracy.html 
				} catch (NumberFormatException ex) {
				}

				// number of sat
				Bundle extras = new Bundle();
				int sat =0;
				if(toInt(satString)!=null)
					sat = toInt(satString);
				extras.putInt("satellites", sat);
				l.setExtras(extras);

				// Check sanity.  If we haven't received a fix, don't use it.
				if ("".equals(fixString) || "0".equals(fixString)) {
					return null;
				}
				return l;
			}
			else {
				Log.d("parser", "bad sentence");
				return null;
			}

		} else {
			return null;
		}
	}

	private static Integer toInt(String strValue){
		try {
			int value = Integer.parseInt( strValue ) ;
			return value;
		} catch (NumberFormatException ex ) {
			return null;
		}
	}
	private static Double toDoubleMeters( String dimension, String unit ) {
		try {
			double value = Double.parseDouble( dimension ) ;
			if ( "M".equals(unit ) ) {
				return value;
			} else {
				throw new RuntimeException("Need to parse unit: " + unit);
			}
		} catch (NumberFormatException ex ) {
			return null;
		}
	}

	private static Double toDecimalDegrees( String ddmm, String dir ) {

		try {

			double value = Double.parseDouble(ddmm);

			double deg = Math.floor( value / 100 );

			double min = value - deg *100;

			double decimalDegrees = deg + min/60.0;

			if( "W".equals(dir) || "S".equals(dir) ) {
				decimalDegrees *= -1.0;
			}

			return decimalDegrees;
		} catch ( NumberFormatException ex ) {
			return null;
		}

	}
	/*public byte computeChecksum(String s){
		byte checksum = 0;
		for (char c : s.toCharArray()){
			checksum ^= (byte)c;
		}
		return checksum;
	}*/

	private static boolean validateChecksum(final String sentence)
	{
		int i;
		int checkSum;
		int xorSum = 0;

		for (i = 1; i < sentence.length(); i++) {
			char c = sentence.charAt(i);

			if (c == '*')
				break;

			xorSum ^= (byte) c;
		}

		try {
			//this may fail...
			checkSum = Integer.parseInt(sentence.substring(i + 1, i + 3), 16);
		} catch (RuntimeException e) {
			e.printStackTrace();
			return false;
		}

		Log.d("parser",String.format("xorSum: %02X checkSum: %02X", xorSum, checkSum));

		return (xorSum - checkSum) == 0;
	}
}
